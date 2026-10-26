package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.ListView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CityActivityTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    @Test
    public void listede81IlVarVeAramaCalisir() throws Exception {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        ListView list = (ListView) a.findViewById(R.id.city_list);
        assertEquals(81, list.getAdapter().getCount());
        TestUtil.screenshot(a, "4-sehir-secimi");

        ((EditText) a.findViewById(R.id.city_search)).setText("izmir");
        ShadowLooper.idleMainLooper();
        assertEquals("İzmir" + CityActivity.ARROW, list.getAdapter().getItem(0));

        ((EditText) a.findViewById(R.id.city_search)).setText("sanliurfa");
        ShadowLooper.idleMainLooper();
        assertEquals("Şanlıurfa" + CityActivity.ARROW, list.getAdapter().getItem(0));
    }

    @Test
    public void sehreDokununcaKaydedilirVeAlarmKurulur() {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        ((EditText) a.findViewById(R.id.city_search)).setText("Trabzon");
        ShadowLooper.idleMainLooper();
        ListView list = (ListView) a.findViewById(R.id.city_list);
        shadowOf(list).performItemClick(0); // ile dokununca ilçeleri açılır
        assertEquals("Trabzon (il merkezi)", list.getAdapter().getItem(0));
        assertTrue(list.getAdapter().getCount() > 15);
        assertTrue(!a.isFinishing());
        shadowOf(list).performItemClick(0);
        assertEquals("Trabzon", Prefs.placeName(app));
        assertEquals(41.005, Prefs.lat(app), 0.01);
        assertTrue(a.isFinishing());
        assertTrue(shadowOf((android.app.AlarmManager) app.getSystemService(Context.ALARM_SERVICE))
                .peekNextScheduledAlarm() != null);
    }

    @Test
    public void konumIzniVerilirseEnYakinIlBulunur() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        LocationManager lm = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        shadowOf(lm).setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        Location l = new Location(LocationManager.NETWORK_PROVIDER);
        l.setLatitude(37.88);   // Konya merkez yakını
        l.setLongitude(32.49);
        l.setTime(Clock.now() - 60000);
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, l);

        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        a.findViewById(R.id.btn_find_me).performClick();
        System.out.println("Konum adı: " + Prefs.placeName(app));
        assertTrue(Prefs.placeName(app), Prefs.placeName(app).endsWith("Konya (konumum)"));
        assertEquals(37.88, Prefs.lat(app), 1e-9);
        assertTrue(a.isFinishing());
    }

    @Test
    public void konumIzniYoksaIzinIstenir() {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        a.findViewById(R.id.btn_find_me).performClick();
        String[] asked = shadowOf(a).getLastRequestedPermission().requestedPermissions;
        assertEquals(Manifest.permission.ACCESS_COARSE_LOCATION, asked[0]);
        a.onRequestPermissionsResult(7, asked, new int[] {-1, -1});
        String status = ((android.widget.TextView) a.findViewById(R.id.city_status)).getText().toString();
        assertTrue(status, status.contains("izni verilmedi"));
    }

    @Test
    public void ilceSecilinceVakitIlceyeGoreHesaplanir() throws Exception {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        ListView list = (ListView) a.findViewById(R.id.city_list);
        // İl → ilçe: Mersin'e dokun, Anamur'u seç
        ((EditText) a.findViewById(R.id.city_search)).setText("mersin");
        ShadowLooper.idleMainLooper();
        shadowOf(list).performItemClick(0);
        int anamur = -1;
        for (int i = 0; i < list.getAdapter().getCount(); i++) {
            if ("Anamur".equals(list.getAdapter().getItem(i))) anamur = i;
        }
        assertTrue("Anamur listede olmalı", anamur > 0);
        TestUtil.screenshot(a, "4b-ilce-secimi");
        shadowOf(list).performItemClick(anamur);
        assertEquals("Anamur, Mersin", Prefs.placeName(app));
        long[] anamurTimes = Times.forDay(app, Clock.now(), 0);

        TestUtil.setCity(app, "Mersin");
        long[] mersinTimes = Times.forDay(app, Clock.now(), 0);
        long diffMin = (anamurTimes[PrayerCalc.OGLE] - mersinTimes[PrayerCalc.OGLE]) / 60000;
        System.out.println("Öğle: Anamur, Mersin merkezden " + diffMin + " dakika sonra");
        assertTrue("Anamur 180 km batıda: öğle birkaç dakika geç olmalı", diffMin >= 6 && diffMin <= 9);
    }

    @Test
    public void aramaIlceleriDeBulur() {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        ((EditText) a.findViewById(R.id.city_search)).setText("kadikoy");
        ShadowLooper.idleMainLooper();
        ListView list = (ListView) a.findViewById(R.id.city_list);
        int k = -1;
        for (int i = 0; i < list.getAdapter().getCount(); i++) {
            if ("Kadıköy – İstanbul".equals(list.getAdapter().getItem(i))) k = i;
        }
        assertTrue(k >= 0);
        shadowOf(list).performItemClick(k);
        assertEquals("Kadıköy, İstanbul", Prefs.placeName(app));
        assertEquals(40.99, Prefs.lat(app), 0.02);
        assertEquals(29.04, Prefs.lon(app), 0.02);
        assertTrue(a.isFinishing());
    }

    @Test
    public void ilceVerisiTutarli() {
        assertEquals(973, Ilceler.AD.length);
        assertEquals(973, Ilceler.LAT.length);
        int[] count = new int[Cities.NAMES.length];
        for (int k = 0; k < Ilceler.AD.length; k++) {
            int il = Ilceler.IL[k];
            count[il]++;
            assertTrue(Ilceler.AD[k], Ilceler.LAT[k] > 35.8 && Ilceler.LAT[k] < 42.2);
            assertTrue(Ilceler.AD[k], Ilceler.LON[k] > 25.6 && Ilceler.LON[k] < 44.9);
            double d = Cities.distanceKm(Ilceler.LAT[k], Ilceler.LON[k], Cities.LAT[il], Cities.LON[il]);
            assertTrue(Cities.NAMES[il] + "/" + Ilceler.AD[k] + " " + d + " km", d < 200);
        }
        for (int i = 0; i < count.length; i++) assertTrue(Cities.NAMES[i], count[i] >= 3);
        assertEquals(39, count[Cities.indexOf("İstanbul")]);
        assertEquals(25, count[Cities.indexOf("Ankara")]);
    }
}
