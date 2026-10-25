package tr.kolay.namazvakti;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Kıble Pusulası: seccade üzerinde Kâbe yönünü gösteren ok. İnternet gerekmez.
 *
 * Yön hesabı:
 * 1. Önce "dönüş vektörü" sensörü (jiroskoplu telefonlar), yoksa "jeomanyetik dönüş vektörü",
 *    o da yoksa ivmeölçer + manyetik alan sensörü kullanılır.
 * 2. Telefon düz, eğik veya dik tutulsa da doğru sonuç için hem ekranın üst kenarının hem de
 *    telefonun arka yüzünün baktığı yön birlikte hesaba katılır (bkz. headingFromMatrix).
 * 3. Manyetik kuzey, bulunulan yerin sapma açısıyla gerçek kuzeye çevrilir.
 */
final class QiblaPage extends Page implements SensorEventListener {
    private static final float SMOOTH = 0.25f;

    private final SensorManager sm;
    private final Sensor rotation, accel, magnetic;
    private final QiblaView qiblaView;
    private final TextView angleText, stateText, headingText;
    private final float[] matrix = new float[9];
    private final float[] gravity = new float[3];
    private final float[] geomag = new float[3];
    private boolean haveGravity, haveGeomag;
    private float qibla;
    private float declination;
    private double sx, sy;           // yumuşatılmış yön (birim vektör)
    private boolean first = true;
    private int accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;

    QiblaPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_qibla);
        qiblaView = (QiblaView) view.findViewById(R.id.qibla_view);
        angleText = (TextView) view.findViewById(R.id.qibla_angle);
        stateText = (TextView) view.findViewById(R.id.qibla_state);
        headingText = (TextView) view.findViewById(R.id.qibla_heading);
        sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
        Sensor rv = null, acc = null, mag = null;
        if (sm != null) {
            rv = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (rv == null) rv = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); // ayar (kalibrasyon) durumu için
            if (rv == null) {
                acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (acc == null || mag == null) acc = mag = null;
            }
        }
        rotation = rv;
        accel = acc;
        magnetic = mag;
    }

    boolean hasCompass() { return rotation != null || accel != null; }

    @Override
    void onShow() {
        first = true;
        haveGravity = haveGeomag = false;
        accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
        headingText.setText("");
        if (!Prefs.hasLocation(activity)) {
            Ui.header(view, "Kolay Namaz Vakti", "Kıble Pusulası");
            angleText.setText("Önce Ana Sayfa'dan şehrinizi seçin.");
            stateText.setText("");
            qiblaView.set(0, 0, false);
            return;
        }
        Ui.header(view, Prefs.placeName(activity), "Kıble Pusulası");
        double lat = Prefs.lat(activity), lon = Prefs.lon(activity);
        qibla = (float) PrayerCalc.qibla(lat, lon);
        declination = new GeomagneticField((float) lat, (float) lon, 0, Clock.now()).getDeclination();
        angleText.setText(Prefs.placeName(activity) + " için Kıble açısı: " + Math.round(qibla)
                + "° (kuzeyden saat yönünde)");
        qiblaView.set(qibla, 0, false);
        if (!hasCompass()) {
            stateText.setText("Telefonunuzda pusula bulunamadı. Ok, kuzey yukarıdayken geçerlidir.");
            return;
        }
        stateText.setText("Pusula hazırlanıyor…");
        if (rotation != null) sm.registerListener(this, rotation, SensorManager.SENSOR_DELAY_UI);
        if (accel != null) sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        if (magnetic != null) sm.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    void onHide() {
        if (sm != null) sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        int type = e.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            lowPass(e.values, gravity, haveGravity);
            haveGravity = true;
            if (!haveGeomag) return;
            if (!SensorManager.getRotationMatrix(matrix, null, gravity, geomag)) return;
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            accuracy = e.accuracy;
            if (rotation != null) return; // dönüş sensörü varken yalnızca ayar durumu için dinlenir
            lowPass(e.values, geomag, haveGeomag);
            haveGeomag = true;
            if (!haveGravity) return;
            if (!SensorManager.getRotationMatrix(matrix, null, gravity, geomag)) return;
        } else {
            SensorManager.getRotationMatrixFromVector(matrix, e.values);
        }
        float magneticHeading = headingFromMatrix(matrix, displayRotation());
        if (Float.isNaN(magneticHeading)) return;
        update(magneticHeading + declination);
    }

    private static void lowPass(float[] in, float[] out, boolean have) {
        for (int i = 0; i < 3; i++) out[i] = have ? out[i] + 0.2f * (in[i] - out[i]) : in[i];
    }

    @SuppressWarnings("deprecation")
    private int displayRotation() {
        try {
            return activity.getWindowManager().getDefaultDisplay().getRotation();
        } catch (RuntimeException ex) {
            return Surface.ROTATION_0;
        }
    }

    /** Yeni ölçülen yönü (gerçek kuzeyden derece) yumuşatarak ekrana yansıtır. */
    void update(float trueHeading) {
        double a = Math.toRadians(trueHeading);
        if (first) {
            sx = Math.sin(a);
            sy = Math.cos(a);
            first = false;
        } else {
            sx += SMOOTH * (Math.sin(a) - sx);
            sy += SMOOTH * (Math.cos(a) - sy);
        }
        float h = (float) ((Math.toDegrees(Math.atan2(sx, sy)) + 360.0) % 360.0);
        qiblaView.set(qibla, h, true);
        String line = "Telefonun baktığı yön: " + Math.round(h) % 360 + "°";
        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            String warn = "Pusula ayarsız olabilir: telefonu havada 8 çizer gibi birkaç kez sallayın.";
            SpannableString w = new SpannableString(line + "\n" + warn);
            w.setSpan(new ForegroundColorSpan(activity.getColor(R.color.warn)), line.length() + 1,
                    w.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            headingText.setText(w);
        } else {
            headingText.setText(line);
        }
        boolean ok = qiblaView.aligned();
        if (ok) {
            SpannableString s = new SpannableString("✓ Kıble yönündesiniz.");
            s.setSpan(new ForegroundColorSpan(activity.getColor(R.color.accent_text)), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stateText.setText(s);
            stateText.setTextColor(activity.getColor(R.color.text_main));
        } else {
            stateText.setText(turnHint(qiblaView.arrowAngle()));
            stateText.setTextColor(activity.getColor(R.color.text_main));
        }
    }

    /**
     * Telefonun baktığı yön (manyetik kuzeyden, saat yönünde derece).
     * R: cihaz eksenlerini dünya eksenlerine (Doğu, Kuzey, Yukarı) çeviren 3x3 matris.
     * Ekranın üst kenarının yatay izdüşümü (telefon düzken) ile telefonun arka yüzünün yatay
     * izdüşümü (telefon dikken) toplanır; eğik tutuşta ikisi aynı yönü gösterir.
     * Telefon yan çevrilmişse (ekran döndüyse) "ekranın üstü" ona göre seçilir.
     * Hesaplanamıyorsa NaN döner.
     */
    static float headingFromMatrix(float[] r, int displayRotation) {
        float ux = 0, uy = 1; // ekranın üstü, cihaz ekseninde
        switch (displayRotation) {
            case Surface.ROTATION_90: ux = 1; uy = 0; break;
            case Surface.ROTATION_180: ux = 0; uy = -1; break;
            case Surface.ROTATION_270: ux = -1; uy = 0; break;
            default: break;
        }
        double east = r[0] * ux + r[1] * uy - r[2];
        double north = r[3] * ux + r[4] * uy - r[5];
        if (Math.hypot(east, north) < 1e-3) return Float.NaN;
        return (float) ((Math.toDegrees(Math.atan2(east, north)) + 360.0) % 360.0);
    }

    /** Okun ekrandaki açısına göre: kaç derece, hangi yöne dönülmeli. */
    static String turnHint(float arrow) {
        if (arrow <= 180f) return "Sağa doğru dönün ↻ (" + Math.round(arrow) + "°)";
        return "Sola doğru dönün ↺ (" + Math.round(360f - arrow) + "°)";
    }

    @Override
    public void onAccuracyChanged(Sensor s, int acc) {
        // Bazı telefonlar dönüş sensörünün doğruluğunu hep "güvenilmez" bildirir; bu yüzden
        // yalnızca manyetik alan sensörünün ayar durumuna bakılır.
        if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD) accuracy = acc;
    }
}
