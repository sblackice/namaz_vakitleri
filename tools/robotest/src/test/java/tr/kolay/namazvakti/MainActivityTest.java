package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.robolectric.RuntimeEnvironment;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MainActivityTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    private MainActivity start() {
        ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
        return c.get();
    }

    @Test
    public void ilkAcilistaSehirSecimiAcilir() {
        MainActivity a = start();
        Intent next = shadowOf(a).getNextStartedActivity();
        assertEquals(CityActivity.class.getName(), next.getComponent().getClassName());
        assertEquals(View.VISIBLE, a.findViewById(R.id.no_place_box).getVisibility());
    }

    @Test
    public void bildirimIzniIstenir() {
        MainActivity a = start();
        assertTrue(shadowOf(a).getLastRequestedPermission().requestedPermissions[0]
                .equals("android.permission.POST_NOTIFICATIONS"));
    }

    @Test
    public void ankaraVakitleriAdhanIleUyumluVeBuyukYazili() throws Exception {
        TestUtil.setCity(app, "Ankara");
        MainActivity a = start();
        List<Ref> refs = Ref.load();
        Ref r = Ref.find(refs, "Ankara", 2026, 10, 15);

        LinearLayout rows = (LinearLayout) a.findViewById(R.id.rows);
        assertEquals(6, rows.getChildCount());
        for (int i = 0; i < 6; i++) {
            View row = rows.getChildAt(i);
            TextView name = (TextView) row.findViewById(R.id.row_name);
            TextView time = (TextView) row.findViewById(R.id.row_time);
            assertEquals(Times.NAMES[i], name.getText().toString());
            String shown = time.getText().toString();
            String ref = TestUtil.hhmm(r.t[i]);
            String refMinus = TestUtil.hhmm(r.t[i] - 60000), refPlus = TestUtil.hhmm(r.t[i] + 60000);
            assertTrue(Times.NAMES[i] + ": ekranda " + shown + ", adhan " + ref,
                    shown.equals(ref) || shown.equals(refMinus) || shown.equals(refPlus));
            System.out.println("Ankara 15.10.2026 " + Times.NAMES[i] + ": ekranda " + shown
                    + " | adhan: " + ref);
            assertTrue("vakit yazısı büyük olmalı", TestUtil.sp(time) >= 40f);
            assertTrue("vakit adı büyük olmalı", TestUtil.sp(name) >= 30f);
        }
        String next = ((TextView) a.findViewById(R.id.next_label)).getText().toString();
        String count = ((TextView) a.findViewById(R.id.countdown)).getText().toString();
        System.out.println("Sıradaki kutusu: " + next + " / " + count);
        assertTrue(next, next.contains("ÖĞLE"));
        assertTrue("saat:dakika:saniye olmalı: " + count, count.matches("\\d\\d:\\d\\d:\\d\\d"));
        assertEquals(Times.hms(r.t[PrayerCalc.OGLE] - Clock.now()).substring(0, 5), count.substring(0, 5));
        assertTrue("geri sayım büyük olmalı", TestUtil.sp((TextView) a.findViewById(R.id.countdown)) >= 60f);
        assertEquals("Ankara", ((TextView) a.findViewById(R.id.place)).getText().toString());
        String hijri = ((TextView) a.findViewById(R.id.hijri)).getText().toString();
        System.out.println("Tarih: " + ((TextView) a.findViewById(R.id.date)).getText() + " / " + hijri);
        assertTrue(hijri, hijri.contains("Hicri"));
        TestUtil.screenshot(a, "1-ana-ekran");
    }

    @Test
    public void yatsidanSonraYarininImsakiGosterilir() throws Exception {
        TestUtil.setCity(app, "İstanbul");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 23, 30);
        MainActivity a = start();
        String next = ((TextView) a.findViewById(R.id.next_label)).getText().toString();
        assertTrue(next, next.contains("İMSAK") && next.contains("Yarın"));
        TestUtil.screenshot(a, "2-ana-ekran-gece");
    }

    @Test
    public void yaziBuyutmeAyariCalisir() throws Exception {
        TestUtil.setCity(app, "İzmir");
        Prefs.setTextScale(app, 1.4f);
        MainActivity a = start();
        TextView time = (TextView) ((LinearLayout) a.findViewById(R.id.rows)).getChildAt(0)
                .findViewById(R.id.row_time);
        assertTrue(TestUtil.sp(time) >= 55f);
        TestUtil.layout(a);
        assertEquals("geri sayım tek satır olmalı", 1,
                ((TextView) a.findViewById(R.id.countdown)).getLineCount());
        TestUtil.screenshot(a, "3-ana-ekran-en-buyuk-yazi");
    }

    @Test
    public void geriSayimHerSaniyeAzalir() {
        TestUtil.setCity(app, "Ankara");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0) + 7000; // 12:00:07
        MainActivity a = start();
        TextView count = (TextView) a.findViewById(R.id.countdown);
        String first = count.getText().toString();
        Clock.fixedNow += 1000;
        a.page(MainActivity.HOME).onTick(Clock.now());
        String second = count.getText().toString();
        System.out.println("Geri sayım: " + first + " -> " + second);
        int s1 = Integer.parseInt(first.substring(6)), s2 = Integer.parseInt(second.substring(6));
        assertEquals((s1 + 59) % 60, s2);
    }

    @Test
    public void vakitGirinceSiradakiVakteGecer() {
        TestUtil.setCity(app, "Ankara");
        long ogle = Times.forDay(app, Clock.now(), 0)[PrayerCalc.OGLE];
        Clock.fixedNow = ogle - 1000;
        MainActivity a = start();
        assertTrue(((TextView) a.findViewById(R.id.next_label)).getText().toString().contains("ÖĞLE"));
        assertEquals("00:00:01", ((TextView) a.findViewById(R.id.countdown)).getText().toString());
        Clock.fixedNow = ogle + 1;
        a.page(MainActivity.HOME).onTick(Clock.now());
        String next = ((TextView) a.findViewById(R.id.next_label)).getText().toString();
        assertTrue(next, next.contains("İKİNDİ"));
    }

    @Test
    public void gununHadisiGosterilirVeErtesiGunDegisir() throws Exception {
        TestUtil.setCity(app, "Ankara");
        MainActivity a = start();
        TextView text = (TextView) a.findViewById(R.id.hadith_text);
        TextView src = (TextView) a.findViewById(R.id.hadith_source);
        int i = Hadisler.indexFor(Clock.now(), Times.tz(app));
        assertTrue(text.getText().toString().contains(Hadisler.LISTE[i].metin));
        assertEquals("Kaynak: " + Hadisler.LISTE[i].kaynak, src.getText().toString());
        System.out.println("Günün hadisi (" + i + "): " + text.getText() + " | " + src.getText());

        ScrollView sv = (ScrollView) a.findViewById(R.id.home_scroll);
        View card = a.findViewById(R.id.hadith_card);
        TestUtil.layout(a);
        sv.scrollTo(0, card.getTop() - 40);
        TestUtil.screenshot(a, "1b-ana-ekran-gunun-hadisi");

        Clock.fixedNow += 24 * 3600 * 1000L;
        a.page(MainActivity.HOME).onTick(Clock.now());
        int j = Hadisler.indexFor(Clock.now(), Times.tz(app));
        assertEquals((i + 1) % Hadisler.LISTE.length, j);
        assertTrue(text.getText().toString().contains(Hadisler.LISTE[j].metin));
    }

    @Test
    public void altMenudeBesDugmeVarVeSayfaDegisir() {
        TestUtil.setCity(app, "Ankara");
        MainActivity a = start();
        LinearLayout nav = (LinearLayout) a.findViewById(R.id.bottom_nav);
        assertEquals(5, nav.getChildCount());
        String[] labels = {"Ana Sayfa", "Ezan", "Dini Günler", "Kıble", "Dualar"};
        for (int k = 0; k < 5; k++) {
            assertEquals(labels[k], ((TextView) nav.getChildAt(k).findViewById(R.id.tab_label))
                    .getText().toString());
        }
        assertEquals(View.VISIBLE, a.findViewById(R.id.root).getVisibility());
        a.findViewById(R.id.tab_duas).performClick();
        assertEquals(MainActivity.DUAS, a.currentTab());
        assertEquals(View.VISIBLE, a.findViewById(R.id.duas_root).getVisibility());
        assertEquals(View.GONE, ((View) a.findViewById(R.id.root).getParent()).getVisibility());
        a.onBackPressed();
        assertEquals(MainActivity.HOME, a.currentTab());
    }
}
