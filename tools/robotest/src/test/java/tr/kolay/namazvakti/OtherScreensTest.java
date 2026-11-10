package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.SensorEventBuilder;
import org.robolectric.shadows.ShadowAppWidgetManager;
import org.robolectric.shadows.ShadowSensor;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class OtherScreensTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        TestUtil.setCity(app, "Ankara");
        Prefs.clearAdhanSound(app); // eski davranış: bildirim sesi
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    private MainActivity open(int tab) {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.findViewById(MainActivity.TAB_IDS[tab]).performClick();
        assertEquals(tab, a.currentTab());
        return a;
    }

    @Test
    public void ezanSayfasiVakitleriAcipKapatir() throws Exception {
        MainActivity a = open(MainActivity.ADHAN);
        LinearLayout rows = (LinearLayout) a.findViewById(R.id.adhan_rows);
        assertEquals(5, rows.getChildCount()); // Güneş bir namaz vakti değil
        Button ogle = (Button) rows.getChildAt(1).findViewById(R.id.adhan_toggle);
        assertEquals("Öğle", ((TextView) rows.getChildAt(1).findViewById(R.id.adhan_name))
                .getText().toString());
        assertEquals("AÇIK ✔", ogle.getText().toString());
        TestUtil.screenshot(a, "5-ezan-ve-izinler");
        ogle.performClick();
        assertEquals("KAPALI", ogle.getText().toString());
        assertFalse(Prefs.adhanOn(app, PrayerCalc.OGLE));

        Button remind = (Button) a.findViewById(R.id.btn_remind);
        remind.performClick();
        assertEquals("Vakitten önce hatırlat: 10 dakika", remind.getText().toString());
        assertEquals(10, Prefs.remindBefore(app));
    }

    @Test
    public void ezanSayfasiIzinDurumlariniGosterir() throws Exception {
        MainActivity a = open(MainActivity.ADHAN);
        LinearLayout perms = (LinearLayout) a.findViewById(R.id.perm_rows);
        for (int i = 0; i < perms.getChildCount(); i++) {
            View row = perms.getChildAt(i);
            if (row.getVisibility() != View.VISIBLE) continue;
            System.out.println("İzin: " + ((TextView) row.findViewById(R.id.perm_name)).getText()
                    + " -> " + ((TextView) row.findViewById(R.id.perm_state)).getText());
        }
        // Dosya seçilmeden ses seviyesi ve dosya satırları gizli.
        assertEquals(View.GONE, perms.getChildAt(AdhanPage.P_VOLUME).getVisibility());
        assertEquals(View.GONE, a.findViewById(R.id.btn_test_sound).getVisibility());

        // Pil satırındaki düğme pil ayarlarını açar.
        View battery = perms.getChildAt(AdhanPage.P_BATTERY);
        Button fix = (Button) battery.findViewById(R.id.perm_button);
        if (fix.getVisibility() == View.VISIBLE) {
            fix.performClick();
            Intent next = shadowOf(a).getNextStartedActivity();
            assertEquals(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    next.getAction());
        }

        // Ezan dosyası seçme düğmesi dosya seçiciyi açar; seçilince kaydedilir.
        a.findViewById(R.id.btn_pick_sound).performClick();
        Intent pick = shadowOf(a).getNextStartedActivityForResult().intent;
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, pick.getAction());
        assertEquals("audio/*", pick.getType());
        a.page(MainActivity.ADHAN).onActivityResult(AdhanPage.REQ_SOUND, android.app.Activity.RESULT_OK,
                new Intent().setData(android.net.Uri.parse("content://deneme/ezan.mp3")));
        assertEquals("content://deneme/ezan.mp3", Prefs.adhanUri(app));
        assertEquals(View.VISIBLE, a.findViewById(R.id.btn_test_sound).getVisibility());
        assertEquals(View.VISIBLE, perms.getChildAt(AdhanPage.P_VOLUME).getVisibility());
    }

    @Test
    public void dakikaDuzeltmesiAnaEkranaYansir() {
        long before = Times.forDay(app, Clock.now(), 0)[PrayerCalc.AKSAM];
        SettingsActivity s = Robolectric.buildActivity(SettingsActivity.class).setup().get();
        View row = ((LinearLayout) s.findViewById(R.id.offset_rows)).getChildAt(PrayerCalc.AKSAM);
        row.findViewById(R.id.off_plus).performClick();
        row.findViewById(R.id.off_plus).performClick();
        assertEquals("+2", ((TextView) row.findViewById(R.id.off_value)).getText().toString());
        try {
            TestUtil.screenshot(s, "10-diger-ayarlar");
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        MainActivity m = Robolectric.buildActivity(MainActivity.class).setup().get();
        TextView t = (TextView) ((LinearLayout) m.findViewById(R.id.rows)).getChildAt(PrayerCalc.AKSAM)
                .findViewById(R.id.row_time);
        assertEquals(TestUtil.hhmm(before + 2 * 60000L), t.getText().toString());
    }

    /** Telefon düz dururken pusulanın "gerçek kuzeye göre" verdiği yönü taklit eden olay. */
    private static SensorEvent headingEvent(Sensor rv, double trueHeading, double declination) {
        double theta = -Math.toRadians(trueHeading - declination); // z ekseni etrafında dönüş
        float[] q = {0f, 0f, (float) Math.sin(theta / 2), (float) Math.cos(theta / 2)};
        return SensorEventBuilder.newBuilder().setSensor(rv).setValues(q).build();
    }

    @Test
    public void kibleSayfasiAciVeSeccadeyiGosterir() throws Exception {
        SensorManager sm = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        Sensor rv = ShadowSensor.newInstance(Sensor.TYPE_ROTATION_VECTOR);
        shadowOf(sm).addSensor(rv);
        MainActivity a = open(MainActivity.QIBLA);
        String text = ((TextView) a.findViewById(R.id.qibla_angle)).getText().toString();
        Ref r = Ref.find(Ref.load(), "Ankara", 2026, 10, 15);
        System.out.println(text + " | adhan: " + r.qibla);
        assertTrue(text, text.contains(Math.round(r.qibla) + "°"));
        assertTrue("pusula dinlenmeli", shadowOf(sm).hasListener((QiblaPage) a.page(MainActivity.QIBLA)));

        double decl = new GeomagneticField((float) Cities.LAT[Cities.indexOf("Ankara")],
                (float) Cities.LON[Cities.indexOf("Ankara")], 0, Clock.now()).getDeclination();
        QiblaPage page = (QiblaPage) a.page(MainActivity.QIBLA);
        QiblaView v = (QiblaView) a.findViewById(R.id.qibla_view);
        TextView state = (TextView) a.findViewById(R.id.qibla_state);

        // Telefon Kıble'nin 30° solunu gösteriyor: sağa dönmesi söylenir.
        page.onSensorChanged(headingEvent(rv, r.qibla - 30, decl));
        assertFalse(v.aligned());
        assertEquals("Sağa doğru dönün ↻ (30°)", state.getText().toString());
        TestUtil.screenshot(a, "6-kible");

        // Yeni sayfa açılışında yumuşatma sıfırlanır; telefon Kıble'ye dönük.
        page.onHide();
        page.onShow();
        page.onSensorChanged(headingEvent(rv, r.qibla + 2, decl));
        assertTrue("5 dereceden yakınsa hizalı sayılır", v.aligned());
        assertEquals("✓ Kıble yönündesiniz.", state.getText().toString());
        TestUtil.screenshot(a, "6b-kible-hizali");
        assertEquals("Sola doğru dönün ↺ (60°)", QiblaPage.turnHint(300f));

        a.findViewById(R.id.tab_home).performClick();
        assertFalse("sayfadan çıkınca pusula kapanmalı", shadowOf(sm).hasListener(page));
    }

    @Test
    public void diniGunlerSiraliVeDokununcaBilgiAcilir() throws Exception {
        MainActivity a = open(MainActivity.DAYS);
        LinearLayout list = (LinearLayout) a.findViewById(R.id.days_list);
        List<DiniGunler.Gun> expected = DiniGunler.yaklasanlar(Clock.now(), Times.tz(app));
        assertEquals(expected.size(), list.getChildCount());
        View first = list.getChildAt(0);
        assertEquals("Üç Ayların Başlangıcı", ((TextView) first.findViewById(R.id.day_name))
                .getText().toString());
        assertEquals("10", ((TextView) first.findViewById(R.id.day_num)).getText().toString());
        assertEquals("Aralık", ((TextView) first.findViewById(R.id.day_month)).getText().toString());
        assertEquals("56 gün kaldı", ((TextView) first.findViewById(R.id.day_left)).getText().toString());
        TestUtil.screenshot(a, "7-dini-gunler");

        list.getChildAt(2).performClick(); // Miraç Kandili
        Intent next = shadowOf(a).getNextStartedActivity();
        assertEquals(DayDetailActivity.class.getName(), next.getComponent().getClassName());
        DayDetailActivity d = Robolectric.buildActivity(DayDetailActivity.class, next).setup().get();
        assertEquals("MİRAÇ KANDİLİ", ((TextView) d.findViewById(R.id.header_title)).getText().toString());
        d.findViewById(R.id.header_action).performClick();
        assertTrue("Geri düğmesi sayfayı kapatmalı", d.isFinishing());
        String date = ((TextView) d.findViewById(R.id.detail_date)).getText().toString();
        String info = ((TextView) d.findViewById(R.id.detail_text)).getText().toString();
        System.out.println("Detay: " + date + " | " + info.substring(0, 80));
        assertEquals("4 Ocak 2027, Pazartesi akşamı", date);
        assertTrue(info.contains("İsrâ"));
        TestUtil.screenshot(d, "8-dini-gun-detay");
    }

    @Test
    public void bayramBirdenFazlaGunSurer() {
        DiniGunler.Gun bayram = null;
        for (DiniGunler.Gun g : DiniGunler.LISTE) {
            if (g.tur == DiniGunler.KURBAN_BAYRAMI && g.yil == 2027) bayram = g;
        }
        assertNotNull(bayram);
        assertEquals("16 Mayıs 2027, Pazar –\n19 Mayıs 2027, Çarşamba (4 gün)",
                DayDetailActivity.dateText(bayram));
        // Bayramın 3. günü hâlâ listede, "Devam ediyor" yazar.
        long third = TestUtil.istanbul(2027, 5, 18, 10, 0);
        assertTrue(DiniGunler.yaklasanlar(third, Times.tz(app)).contains(bayram));
        assertEquals("Devam ediyor", DaysPage.leftText(bayram, third, Times.tz(app)));
    }

    @Test
    public void dualarFatihaIleBaslarVeHerBiriEksiksiz() throws Exception {
        MainActivity a = open(MainActivity.DUAS);
        LinearLayout list = (LinearLayout) a.findViewById(R.id.duas_list);
        assertEquals("Namazda okunanlar", ((TextView) list.getChildAt(0)).getText().toString());
        View fatiha = list.getChildAt(1);
        assertEquals("Fâtiha Sûresi", ((TextView) fatiha.findViewById(R.id.dua_name)).getText().toString());
        int duas = 0;
        for (int i = 0; i < list.getChildCount(); i++) {
            View v = list.getChildAt(i);
            if (v.findViewById(R.id.dua_name) == null) continue;
            duas++;
            for (int id : new int[] {R.id.dua_arabic, R.id.dua_reading, R.id.dua_meaning}) {
                assertTrue(((TextView) v.findViewById(id)).getText().length() > 5);
            }
        }
        assertEquals(Dualar.LISTE.length, duas);
        TestUtil.screenshot(a, "9-dualar");
    }

    @Test
    public void widgetSiradakiVaktiGosterir() {
        ShadowAppWidgetManager w = shadowOf(AppWidgetManager.getInstance(app));
        int id = w.createWidget(NextPrayerWidget.class, R.layout.widget_next);
        NextPrayerWidget.updateAll(app);
        View v = w.getViewFor(id);
        String next = ((TextView) v.findViewById(R.id.w_next)).getText().toString();
        String line2 = ((TextView) v.findViewById(R.id.w_line2)).getText().toString();
        System.out.println("Widget: " + next + " | " + ((TextView) v.findViewById(R.id.w_line1)).getText()
                + " | " + line2);
        assertTrue(next, next.startsWith("Sıradaki: ÖĞLE"));
        assertTrue(line2, line2.contains("Yatsı"));
    }
}
