package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.widget.TextView;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowToast;

/** Otomatik konum: başka şehre gidince uygulama açıldığında vakitler kendiliğinden güncellenir. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AutoLocationTest {
    private Application app;
    private LocationManager lm;
    private TimeZone savedTz;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
        savedTz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
        lm = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        shadowOf(lm).setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowOf(lm).setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        Prefs.setAskedFirstRun(app);
    }

    @After
    public void tearDown() {
        Clock.fixedNow = 0;
        TimeZone.setDefault(savedTz);
    }

    private Location loc(double lat, double lon, long ageMs) {
        Location l = new Location(LocationManager.NETWORK_PROVIDER);
        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setTime(Clock.now() - ageMs);
        return l;
    }

    private String title(MainActivity a) {
        return ((TextView) a.page(MainActivity.HOME).view.findViewById(R.id.header_title)).getText().toString();
    }

    private String caption(MainActivity a) {
        return ((TextView) a.page(MainActivity.HOME).view.findViewById(R.id.header_caption)).getText().toString();
    }

    @Test
    public void baskaSehreGidinceUygulamaAcilincaKendiliğindenGuncellenir() throws Exception {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        AutoLocation.apply(app, 39.92, 32.85); // Ankara'da otomatik konum açıldı
        assertTrue(Prefs.placeName(app), Prefs.placeName(app).endsWith("Ankara"));
        assertTrue(Prefs.autoLocation(app));

        // Kullanıcı İzmir'e gitti; telefon yeni konumu biliyor.
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, loc(38.42, 27.14, 60000));
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertTrue(Prefs.placeName(app), Prefs.placeName(app).endsWith("İzmir"));
        assertEquals(38.42, Prefs.lat(app), 1e-9);
        assertTrue(title(a), title(a).endsWith("İZMİR"));
        assertEquals("OTOMATİK KONUM", caption(a));
        assertTrue(ShadowToast.getTextOfLatestToast().contains("İzmir"));
        String ogle = ((TextView) ((android.view.ViewGroup) a.findViewById(R.id.rows)).getChildAt(2)
                .findViewById(R.id.row_time)).getText().toString();
        assertTrue(ogle, ogle.startsWith("13:0")); // İzmir öğle vakti 13:02 dolayı (Ankara'da 12:39)
        TestUtil.screenshot(a, "1d-ana-ekran-otomatik-konum");
    }

    @Test
    public void sonKonumEskiyseYenisiIstenirVeGelinceUygulanir() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        AutoLocation.apply(app, 39.92, 32.85);
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER,
                loc(39.92, 32.85, 3 * 24 * 3600 * 1000L)); // 3 gün önce
        ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity a = c.get();
        assertTrue(title(a), title(a).endsWith("ANKARA"));
        assertTrue("yeni konum bekleniyor olmalı", a.autoLocation().isWaiting());

        shadowOf(lm).simulateLocation(loc(41.01, 28.97, 0)); // İstanbul
        ShadowLooper.idleMainLooper();
        assertTrue(Prefs.placeName(app), Prefs.placeName(app).endsWith("İstanbul"));
        assertTrue(title(a), title(a).endsWith("İSTANBUL"));
        assertFalse(a.autoLocation().isWaiting());

        // Uygulama arka plana gidince konum dinlenmez.
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER,
                loc(41.01, 28.97, 3 * 24 * 3600 * 1000L));
        shadowOf(lm).setLastKnownLocation(LocationManager.PASSIVE_PROVIDER,
                loc(41.01, 28.97, 3 * 24 * 3600 * 1000L));
        c.pause().resume();
        assertTrue(a.autoLocation().isWaiting());
        c.pause();
        assertFalse(a.autoLocation().isWaiting());
        assertTrue(shadowOf(lm).getLocationUpdateListeners().isEmpty());
    }

    @Test
    public void ayniYerdeKucukHareketteHicbirSeyDegismez() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        assertTrue(AutoLocation.apply(app, 39.92, 32.85));
        assertFalse("2 km'lik hareket vakitleri değiştirmez", AutoLocation.apply(app, 39.935, 32.86));
        assertEquals(39.92, Prefs.lat(app), 1e-9);
        assertTrue("30 km'lik hareket güncellenir", AutoLocation.apply(app, 39.92, 33.20));
    }

    @Test
    public void elleSecilenSehirKendiliğindenDegismez() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        TestUtil.setCity(app, "Ankara"); // otomatik konum kapalı
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, loc(38.42, 27.14, 60000));
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertEquals("Ankara", Prefs.placeName(app));
        assertEquals("TÜRKİYE", caption(a));
    }

    @Test
    public void izinYoksaKonumOkunmaz() {
        AutoLocation.apply(app, 39.92, 32.85);
        shadowOf(lm).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, loc(38.42, 27.14, 60000));
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertTrue(Prefs.placeName(app).endsWith("Ankara"));
        assertFalse(a.autoLocation().isWaiting());
    }

    @Test
    public void yurtDisindaSaatDilimiVeAdDogruOlur() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        AutoLocation.apply(app, 52.52, 13.40); // Berlin
        assertEquals("Konumum", Prefs.placeName(app));
        assertEquals("Europe/Berlin", Prefs.tzId(app));
        // Türkiye sınırına yakın ama başka saat diliminde: il adı yazılmaz.
        // Bazı telefonlar Türkiye saat dilimini eski adıyla bildirir.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Istanbul"));
        AutoLocation.apply(app, 37.88, 32.49);
        assertTrue(Prefs.placeName(app), Prefs.placeName(app).endsWith("Konya"));
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tbilisi"));
        AutoLocation.apply(app, 41.65, 41.64); // Batum (Artvin'e yakın)
        assertEquals("Konumum", Prefs.placeName(app));
    }

    @Test
    public void saatDilimiDegisinceOtomatikKonumdaVakitlerUyar() {
        AutoLocation.apply(app, 39.92, 32.85);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        new BootReceiver().onReceive(app, new Intent(Intent.ACTION_TIMEZONE_CHANGED));
        assertEquals("Europe/Berlin", Prefs.tzId(app));

        // Elle seçilmiş şehirde saat dilimi değişse de şehir Türkiye saatinde kalır.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
        TestUtil.setCity(app, "Ankara");
        Prefs.setAutoLocation(app, false);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        new BootReceiver().onReceive(app, new Intent(Intent.ACTION_TIMEZONE_CHANGED));
        assertEquals("Europe/Istanbul", Prefs.tzId(app));
    }
}
