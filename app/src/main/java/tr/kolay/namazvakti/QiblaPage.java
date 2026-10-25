package tr.kolay.namazvakti;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.ViewGroup;
import android.widget.TextView;

/** Kıble Pusulası: seccade üzerinde Kâbe yönünü gösteren ok. İnternet gerekmez. */
final class QiblaPage extends Page implements SensorEventListener {
    private final SensorManager sm;
    private final Sensor rotation;
    private final QiblaView qiblaView;
    private final TextView angleText, stateText;
    private float qibla;
    private float declination;
    private float smoothed = Float.NaN;
    private final float[] matrix = new float[9];
    private final float[] orientation = new float[3];

    QiblaPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_qibla);
        qiblaView = (QiblaView) view.findViewById(R.id.qibla_view);
        angleText = (TextView) view.findViewById(R.id.qibla_angle);
        stateText = (TextView) view.findViewById(R.id.qibla_state);
        sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
        rotation = sm == null ? null : sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    void onShow() {
        smoothed = Float.NaN;
        if (!Prefs.hasLocation(activity)) {
            angleText.setText("Önce Ana Sayfa'dan şehrinizi seçin.");
            stateText.setText("");
            qiblaView.set(0, 0, false);
            return;
        }
        double lat = Prefs.lat(activity), lon = Prefs.lon(activity);
        qibla = (float) PrayerCalc.qibla(lat, lon);
        declination = new GeomagneticField((float) lat, (float) lon, 0, Clock.now()).getDeclination();
        angleText.setText(Prefs.placeName(activity) + " için Kıble açısı: " + Math.round(qibla)
                + "° (kuzeyden saat yönünde)");
        qiblaView.set(qibla, 0, false);
        if (rotation == null) {
            stateText.setText("Telefonunuzda pusula bulunamadı. Ok, kuzey yukarıdayken geçerlidir.");
        } else {
            stateText.setText("Pusula hazırlanıyor…");
            sm.registerListener(this, rotation, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    void onHide() {
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
        qiblaView.set(qibla, smoothed, true);
        boolean ok = qiblaView.aligned();
        stateText.setText(ok ? "✔ Kıble yönündesiniz." : turnHint(qiblaView.arrowAngle()));
        stateText.setTextColor(activity.getColor(ok ? R.color.accent : R.color.text_main));
    }

    static String turnHint(float arrow) {
        return arrow <= 180f ? "Sağa doğru dönün ↻" : "Sola doğru dönün ↺";
    }

    @Override
    public void onAccuracyChanged(Sensor s, int accuracy) {
        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            stateText.setText("Pusula ayarsız: telefonu havada 8 çizer gibi birkaç kez sallayın.");
        }
    }
}
