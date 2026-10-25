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
        assertEquals(1, list.getAdapter().getCount());
        assertEquals("İzmir", list.getAdapter().getItem(0));

        ((EditText) a.findViewById(R.id.city_search)).setText("sanliurfa");
        ShadowLooper.idleMainLooper();
        assertEquals("Şanlıurfa", list.getAdapter().getItem(0));
    }

    @Test
    public void sehreDokununcaKaydedilirVeAlarmKurulur() {
        CityActivity a = Robolectric.buildActivity(CityActivity.class).setup().get();
        ((EditText) a.findViewById(R.id.city_search)).setText("Trabzon");
        ShadowLooper.idleMainLooper();
        ListView list = (ListView) a.findViewById(R.id.city_list);
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
        assertEquals("Konya (konumum)", Prefs.placeName(app));
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
}
