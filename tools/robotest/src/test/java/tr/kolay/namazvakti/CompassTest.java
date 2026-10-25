package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.Surface;
import android.widget.TextView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.SensorEventBuilder;
import org.robolectric.shadows.ShadowSensor;

/** Kıble pusulasının yön hesabı: düz, eğik, dik tutuş, yan çevrilmiş ekran ve jiroskopsuz telefon. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CompassTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        TestUtil.setCity(app, "Ankara");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    /** Cihaz eksenlerinin (X sağ, Y üst, Z ekrandan dışarı) dünya karşılıklarından matris (Doğu, Kuzey, Yukarı). */
    private static float[] matrix(double[] x, double[] y, double[] z) {
        return new float[] {
            (float) x[0], (float) y[0], (float) z[0],
            (float) x[1], (float) y[1], (float) z[1],
            (float) x[2], (float) y[2], (float) z[2]};
    }

    private static double[] dir(double headingDeg, double up) {
        double h = Math.toRadians(headingDeg);
        return new double[] {Math.sin(h), Math.cos(h), up};
    }

    private static void near(String what, double expected, float actual) {
        double d = Math.abs(((actual - expected) % 360 + 540) % 360 - 180);
        assertTrue(what + ": beklenen " + expected + ", bulunan " + actual, d < 0.5);
    }

    @Test
    public void masadaDuzDururken() {
        for (double h : new double[] {0, 40, 160, 275}) {
            double r = Math.toRadians(h);
            float[] m = matrix(new double[] {Math.cos(r), -Math.sin(r), 0}, dir(h, 0), new double[] {0, 0, 1});
            near("düz " + h, h, QiblaPage.headingFromMatrix(m, Surface.ROTATION_0));
        }
    }

    @Test
    public void dikTutulurkenArkaYuzunBaktigiYon() {
        // Telefon dik: ekranın üstü gökyüzüne, arka yüzü (kamera) baktığımız yöne döner.
        for (double h : new double[] {20, 160, 200, 300}) {
            double r = Math.toRadians(h);
            double[] y = {0, 0, 1};
            double[] z = {-Math.sin(r), -Math.cos(r), 0};
            double[] x = {Math.cos(r), -Math.sin(r), 0};
            near("dik " + h, h, QiblaPage.headingFromMatrix(matrix(x, y, z), Surface.ROTATION_0));
        }
    }

    @Test
    public void egikTutulurken() {
        for (double tilt : new double[] {20, 45, 70, 85}) {
            double h = 120, r = Math.toRadians(h), t = Math.toRadians(tilt);
            double[] y = {Math.cos(t) * Math.sin(r), Math.cos(t) * Math.cos(r), Math.sin(t)};
            double[] z = {-Math.sin(t) * Math.sin(r), -Math.sin(t) * Math.cos(r), Math.cos(t)};
            double[] x = {Math.cos(r), -Math.sin(r), 0};
            near("eğik " + tilt, h, QiblaPage.headingFromMatrix(matrix(x, y, z), Surface.ROTATION_0));
        }
    }

    @Test
    public void ekranYanCevrilmisse() {
        // Ekran 90° dönmüş: ekranın üstü cihazın sağ kenarı (+X) olur.
        double h = 75, r = Math.toRadians(h);
        double[] x = {Math.sin(r), Math.cos(r), 0};
        double[] y = {-Math.cos(r), Math.sin(r), 0};
        near("yan", h, QiblaPage.headingFromMatrix(matrix(x, y, new double[] {0, 0, 1}), Surface.ROTATION_90));
    }

    @Test
    public void jiroskopYoksaIvmeolcerVeManyetikSensorleCalisir() {
        SensorManager sm = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = ShadowSensor.newInstance(Sensor.TYPE_MAGNETIC_FIELD);
        shadowOf(sm).addSensor(acc);
        shadowOf(sm).addSensor(mag);
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.findViewById(R.id.tab_qibla).performClick();
        QiblaPage page = (QiblaPage) a.page(MainActivity.QIBLA);
        assertTrue(page.hasCompass());
        assertTrue(shadowOf(sm).hasListener(page));

        double decl = new GeomagneticField((float) Cities.LAT[Cities.indexOf("Ankara")],
                (float) Cities.LON[Cities.indexOf("Ankara")], 0, Clock.now()).getDeclination();
        double qibla = PrayerCalc.qibla(Cities.LAT[Cities.indexOf("Ankara")], Cities.LON[Cities.indexOf("Ankara")]);
        double hm = Math.toRadians(qibla - decl); // telefon (manyetik) Kıble yönüne dönük, masada düz
        float[] magDev = {(float) (-25 * Math.sin(hm)), (float) (25 * Math.cos(hm)), -40f};
        page.onSensorChanged(SensorEventBuilder.newBuilder().setSensor(acc)
                .setValues(new float[] {0f, 0f, 9.81f}).build());
        page.onSensorChanged(SensorEventBuilder.newBuilder().setSensor(mag).setValues(magDev)
                .setAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_HIGH).build());
        String heading = ((TextView) a.findViewById(R.id.qibla_heading)).getText().toString();
        String state = ((TextView) a.findViewById(R.id.qibla_state)).getText().toString();
        System.out.println("Jiroskopsuz: " + heading + " | " + state + " | Kıble " + qibla);
        assertEquals("✓ Kıble yönündesiniz.", state);
        assertEquals("Telefonun baktığı yön: " + Math.round(qibla) % 360 + "°", heading);
    }

    @Test
    public void pusulaAyarsizsaUyariGosterirAmaYonuGizlemez() {
        SensorManager sm = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        Sensor rv = ShadowSensor.newInstance(Sensor.TYPE_ROTATION_VECTOR);
        Sensor mag = ShadowSensor.newInstance(Sensor.TYPE_MAGNETIC_FIELD);
        shadowOf(sm).addSensor(rv);
        shadowOf(sm).addSensor(mag);
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.findViewById(R.id.tab_qibla).performClick();
        QiblaPage page = (QiblaPage) a.page(MainActivity.QIBLA);
        page.onAccuracyChanged(mag, SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        // Dönüş sensörü "güvenilmez" dese bile bu dikkate alınmaz (bazı telefonlar hep böyle bildirir).
        page.onAccuracyChanged(rv, SensorManager.SENSOR_STATUS_UNRELIABLE);
        page.onSensorChanged(SensorEventBuilder.newBuilder().setSensor(rv)
                .setValues(new float[] {0f, 0f, 0f, 1f}).setAccuracy(SensorManager.SENSOR_STATUS_UNRELIABLE)
                .build());
        String heading = ((TextView) a.findViewById(R.id.qibla_heading)).getText().toString();
        String state = ((TextView) a.findViewById(R.id.qibla_state)).getText().toString();
        assertTrue(heading, heading.contains("Pusula ayarsız olabilir"));
        assertTrue("yön ipucu yine görünmeli: " + state, state.contains("dönün"));

        page.onAccuracyChanged(mag, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        page.onSensorChanged(SensorEventBuilder.newBuilder().setSensor(rv)
                .setValues(new float[] {0f, 0f, 0f, 1f}).build());
        heading = ((TextView) a.findViewById(R.id.qibla_heading)).getText().toString();
        assertFalse(heading, heading.contains("ayarsız"));

        a.findViewById(R.id.tab_home).performClick();
        assertFalse("sayfadan çıkınca pusula kapanmalı", shadowOf(sm).hasListener(page));
    }
}
