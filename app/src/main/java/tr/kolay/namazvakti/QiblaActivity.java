package tr.kolay.namazvakti;

import android.app.Activity;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/** Kıble pusulası. İnternet gerekmez; telefonun pusula sensörünü kullanır. */
public class QiblaActivity extends Activity implements SensorEventListener {
    private SensorManager sm;
    private Sensor rotation;
    private QiblaView view;
    private TextView angleText, stateText;
    private float qibla;
    private float declination;
    private float smoothed = Float.NaN;
    private final float[] matrix = new float[9];
    private final float[] orientation = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qibla);
        view = (QiblaView) findViewById(R.id.qibla_view);
        angleText = (TextView) findViewById(R.id.qibla_angle);
        stateText = (TextView) findViewById(R.id.qibla_state);
        findViewById(R.id.btn_qibla_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotation = sm == null ? null : sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Ui.scaleText(findViewById(R.id.qibla_root), Prefs.textScale(this));
        update();
    }

    private void update() {
        if (!Prefs.hasLocation(this)) {
            angleText.setText("Önce şehrinizi seçin.");
            stateText.setText("");
            view.set(0, 0, false);
            return;
        }
        double lat = Prefs.lat(this), lon = Prefs.lon(this);
        qibla = (float) PrayerCalc.qibla(lat, lon);
        declination = new GeomagneticField((float) lat, (float) lon, 0, Clock.now()).getDeclination();
        angleText.setText("Kıble açısı: " + Math.round(qibla) + "° (kuzeyden saat yönünde)");
        if (rotation == null) {
            stateText.setText("Telefonunuzda pusula bulunamadı. Okun yönü, kuzey yukarıdayken geçerlidir.");
        }
        view.set(qibla, 0, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotation != null) sm.registerListener(this, rotation, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sm != null) sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        SensorManager.getRotationMatrixFromVector(matrix, e.values);
        SensorManager.getOrientation(matrix, orientation);
        float az = (float) Math.toDegrees(orientation[0]) + declination; // gerçek kuzey
        az = (az + 360f) % 360f;
        if (Float.isNaN(smoothed)) {
            smoothed = az;
        } else {
            float diff = ((az - smoothed + 540f) % 360f) - 180f;
            smoothed = (smoothed + diff * 0.15f + 360f) % 360f;
        }
        view.set(qibla, smoothed, true);
        float off = Math.abs(((qibla - smoothed + 540f) % 360f) - 180f);
        stateText.setText(off <= 5 ? "Kıble yönündesiniz." : "Oku izleyerek dönün.");
    }

    @Override
    public void onAccuracyChanged(Sensor s, int accuracy) {
        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            stateText.setText("Pusula ayarsız: telefonu havada 8 çizer gibi birkaç kez sallayın.");
        }
    }
}
