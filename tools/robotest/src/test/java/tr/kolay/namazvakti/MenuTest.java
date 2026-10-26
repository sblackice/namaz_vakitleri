package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowTextToSpeech;

/** Alt menüdeki "Menü" düğmesi ve açtığı sayfalar. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MenuTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        TestUtil.setCity(app, "Ankara");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    private MainActivity start() {
        return Robolectric.buildActivity(MainActivity.class).setup().get();
    }

    private MainActivity openPage(int menuId, int page) {
        MainActivity a = start();
        a.findViewById(R.id.tab_menu).performClick();
        a.findViewById(menuId).performClick();
        assertEquals(page, a.currentTab());
        ShadowLooper.idleMainLooper();
        return a;
    }

    @Test
    public void menuDugmesiYukariDogruAcilirVeKapanir() throws Exception {
        MainActivity a = start();
        View overlay = a.findViewById(R.id.menu_overlay);
        assertEquals(View.GONE, overlay.getVisibility());
        a.findViewById(R.id.tab_menu).performClick();
        assertEquals(View.VISIBLE, overlay.getVisibility());
        ShadowLooper.idleMainLooper();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        LinearLayout items = (LinearLayout) a.findViewById(R.id.menu_items);
        assertEquals(4, items.getChildCount());
        String[] labels = {"En Yakın Cami", "Esmâ-ül Hüsnâ", "Zikirmatik", "Renk Teması"};
        for (int i = 0; i < 4; i++) {
            assertEquals(labels[i], ((TextView) items.getChildAt(i).findViewById(R.id.menu_label))
                    .getText().toString());
        }
        // Panel, alttaki Menü düğmesinin hemen üstünde ve sağ tarafta durur (yukarı doğru açılır).
        TestUtil.layout(a);
        int[] panel = new int[2], nav = new int[2], btn = new int[2];
        View p = a.findViewById(R.id.menu_panel);
        p.getLocationInWindow(panel);
        a.findViewById(R.id.bottom_nav).getLocationInWindow(nav);
        a.findViewById(R.id.tab_menu).getLocationInWindow(btn);
        int panelBottom = panel[1] + p.getHeight();
        System.out.println("Menü paneli: üst " + panel[1] + ", alt " + panelBottom + ", alt menü " + nav[1]);
        assertTrue("panel alt menünün üstünde bitmeli", panelBottom <= nav[1]);
        assertTrue("panel alt menüye yakın olmalı", nav[1] - panelBottom < 40);
        int menuBtnRight = btn[0] + a.findViewById(R.id.tab_menu).getWidth();
        assertTrue("panel sağa dayalı olmalı", Math.abs(panel[0] + p.getWidth() - menuBtnRight) < 30);
        TestUtil.screenshot(a, "11-menu");

        // Boşluğa dokununca kapanır; geri tuşu da kapatır.
        a.findViewById(R.id.menu_scrim).performClick();
        assertEquals(View.GONE, overlay.getVisibility());
        a.findViewById(R.id.tab_menu).performClick();
        a.onBackPressed();
        assertEquals(View.GONE, overlay.getVisibility());
        assertEquals(MainActivity.HOME, a.currentTab());
    }

    @Test
    public void menudenSayfaAcilinceMenuDugmesiSeciliKalir() {
        MainActivity a = openPage(R.id.menu_zikir, MainActivity.ZIKIR);
        assertEquals(View.GONE, a.findViewById(R.id.menu_overlay).getVisibility());
        assertTrue(a.findViewById(R.id.tab_menu).isSelected());
        assertFalse(a.findViewById(R.id.tab_home).isSelected());
        a.onBackPressed();
        assertEquals(MainActivity.HOME, a.currentTab());
        assertFalse(a.findViewById(R.id.tab_menu).isSelected());
    }

    // ---- Zikirmatik ----

    @Test
    public void zikirmatikSayarHedefeUlasincaTitrerVeSifirlanir() throws Exception {
        MainActivity a = openPage(R.id.menu_zikir, MainActivity.ZIKIR);
        ZikirPage page = (ZikirPage) a.page(MainActivity.ZIKIR);
        ZikirView dial = (ZikirView) a.findViewById(R.id.zikir_view);
        TextView target = (TextView) a.findViewById(R.id.zikir_target);
        assertEquals("33", target.getText().toString());

        dial.performClick();
        dial.performClick();
        assertEquals(2, Prefs.zikirCount(app));
        a.findViewById(R.id.zikir_minus).performClick();
        assertEquals(1, Prefs.zikirCount(app));
        a.findViewById(R.id.zikir_plus).performClick();
        assertEquals(2, Prefs.zikirCount(app));

        // Hedefi değiştir: sağ üstteki kutuya dokununca rakam girilir.
        a.findViewById(R.id.zikir_target_box).performClick();
        AlertDialog d = (AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull(d);
        d.dismiss();
        page.setTarget("5");
        assertEquals("5", target.getText().toString());
        assertEquals(5, Prefs.zikirTarget(app));

        Vibrator v = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
        dial.performClick();
        dial.performClick();
        assertFalse(shadowOf(v).isVibrating());
        dial.performClick(); // 5. zikir: hedef
        assertEquals(5, Prefs.zikirCount(app));
        assertTrue("hedefe ulaşınca titremeli", shadowOf(v).isVibrating());
        assertTrue(dial.done());
        assertEquals(1f, dial.fraction(), 0.001f);

        // Yerleşim: hedef sağ üstte, − + sol altta, Sıfırla sağ altta, büyük düğme ortada.
        Prefs.setZikirTarget(app, 99);
        Prefs.setZikirCount(app, 40);
        page.show();
        TestUtil.layout(a);
        int[] root = loc(a.findViewById(R.id.zikir_root));
        View rootV = a.findViewById(R.id.zikir_root);
        int midX = root[0] + rootV.getWidth() / 2, midY = root[1] + rootV.getHeight() / 2;
        int[] t = loc(a.findViewById(R.id.zikir_target_box));
        int[] minus = loc(a.findViewById(R.id.zikir_minus));
        int[] plus = loc(a.findViewById(R.id.zikir_plus));
        int[] reset = loc(a.findViewById(R.id.zikir_reset));
        assertTrue("hedef sağ üstte", t[0] > midX && t[1] < midY);
        assertTrue("eksi sol altta", minus[0] < midX && minus[1] > midY);
        assertTrue("artı sol altta", plus[0] < midX && plus[1] > midY);
        assertTrue("sıfırla sağ altta", reset[0] > midX && reset[1] > midY);
        TestUtil.screenshot(a, "12-zikirmatik");

        // Sıfırla: önce sorar, "Evet" denince sıfırlar.
        a.findViewById(R.id.zikir_reset).performClick();
        AlertDialog r = (AlertDialog) ShadowDialog.getLatestDialog();
        r.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        ShadowLooper.idleMainLooper();
        assertEquals(0, Prefs.zikirCount(app));
    }

    private static int[] loc(View v) {
        int[] p = new int[2];
        v.getLocationInWindow(p);
        return p;
    }

    // ---- Esmâ-ül Hüsnâ ----

    @Test
    public void esmaulHusna99IsimVeArapcaSeslendirme() throws Exception {
        ShadowTextToSpeech.addLanguageAvailability(new Locale("ar"));
        MainActivity a = openPage(R.id.menu_esma, MainActivity.ESMA);
        EsmaPage page = (EsmaPage) a.page(MainActivity.ESMA);
        LinearLayout list = (LinearLayout) a.findViewById(R.id.list_items);
        assertEquals("3 sütun x 33 satır", 33, list.getChildCount());
        assertEquals(3, ((LinearLayout) list.getChildAt(0)).getChildCount());
        View first = page.card(0);
        assertEquals("Allah", ((TextView) first.findViewById(R.id.esma_okunus)).getText().toString());
        View last = page.card(98);
        assertEquals("Es-Sabûr", ((TextView) last.findViewById(R.id.esma_okunus)).getText().toString());
        TextView ar = (TextView) last.findViewById(R.id.esma_arapca);
        assertEquals(nfc("الصَّبُورُ"), nfc(ar.getText().toString()));
        assertEquals("altın renk", 0xFFD9B56A, ar.getCurrentTextColor());
        assertNotNull(ar.getTypeface());
        // Kartlar aynı satırda yan yana
        TestUtil.layout(a);
        assertEquals(loc(page.card(0))[1], loc(page.card(2))[1]);
        assertTrue(loc(page.card(0))[0] < loc(page.card(1))[0]);
        TestUtil.screenshot(a, "13-esmaul-husna");

        initTts(TextToSpeech.SUCCESS);
        assertEquals(EsmaPage.TTS_READY, page.ttsState());
        page.card(1).performClick(); // Er-Rahmân: pencere açılır ve okunur
        assertEquals(1, page.speaking());
        assertTrue(page.card(1).isSelected());
        TestUtil.screenshot(a, "13b-esmaul-husna-detay");
        ShadowLooper.idleMainLooper(); // test ortamında okuma hemen "bitti" sayılır
        AlertDialog d = page.detail();
        assertTrue(d.isShowing());
        assertEquals("Er-Rahmân", ((TextView) d.findViewById(R.id.esma_d_okunus)).getText().toString());
        assertEquals("2 / 99", ((TextView) d.findViewById(R.id.esma_d_no)).getText().toString());
        ShadowTextToSpeech st = Shadow.extract(findTts());
        assertEquals(nfc("الرَّحْمَن"), nfc(st.getLastSpokenText()));
        assertEquals(new Locale("ar"), st.getCurrentLanguage());
        assertEquals(-1, page.speaking());
        assertFalse(page.card(1).isSelected());
        d.findViewById(R.id.esma_d_next).performClick();
        assertEquals("Er-Rahîm", ((TextView) d.findViewById(R.id.esma_d_okunus)).getText().toString());
        d.findViewById(R.id.esma_d_close).performClick();
        ShadowLooper.idleMainLooper();
        assertEquals(-1, page.speaking());

        // Sağ alttaki ▶ hepsini sırayla okur; Tesbih zikirmatiği açar.
        a.findViewById(R.id.esma_play_all).performClick();
        assertEquals(0, page.speaking());
        assertEquals("■", ((TextView) a.findViewById(R.id.esma_play_all)).getText().toString());
        a.findViewById(R.id.esma_tab_tesbih).performClick();
        assertEquals(MainActivity.ZIKIR, a.currentTab());
    }

    private static TextToSpeech findTts() {
        return ShadowTextToSpeech.getLastTextToSpeechInstance();
    }

    /** Test ortamında konuşma motoru kendiliğinden hazır olmaz; hazır olduğunu bildiririz. */
    private static void initTts(int status) {
        ShadowTextToSpeech st = Shadow.extract(findTts());
        st.getOnInitListener().onInit(status);
        ShadowLooper.idleMainLooper();
    }

    private static String nfc(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC);
    }

    @Test
    public void esmaArapcaSesPaketiYoksaYuklemeDugmesiCikar() {
        MainActivity a = openPage(R.id.menu_esma, MainActivity.ESMA);
        EsmaPage page = (EsmaPage) a.page(MainActivity.ESMA);
        initTts(TextToSpeech.SUCCESS);
        assertEquals(EsmaPage.TTS_NO_ARABIC, page.ttsState());
        android.widget.Button fix = (android.widget.Button) a.findViewById(R.id.esma_fix_voice);
        assertEquals(View.VISIBLE, fix.getVisibility());
        fix.performClick();
        Intent next = shadowOf(a).getNextStartedActivity();
        assertEquals(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA, next.getAction());
    }

    @Test
    public void esmaSeslendirmeDurakHalindeOkur() {
        String damma = "\u064F", kasra = "\u0650", shadda = "\u0651";
        // Uygulamadaki gerçek metinlerle (hareke sırası veri kaynağındaki gibi)
        String allah = EsmaPage.spoken(Esma.LISTE[0].arapca);
        assertFalse(allah.endsWith(damma));
        assertEquals(Esma.LISTE[0].arapca.length() - 1, allah.length());
        String zul = EsmaPage.spoken(Esma.LISTE[84].arapca); // Zü'l-Celâli ve'l-İkrâm
        assertFalse(zul.endsWith(kasra));
        assertTrue("isim içindeki harekeler korunur", zul.contains(kasra));
        String aliyy = EsmaPage.spoken(Esma.LISTE[36].arapca); // El-Aliyy
        assertTrue("şedde korunmalı: " + aliyy, aliyy.endsWith(shadda));
        assertFalse(aliyy.contains("\u064F"));
        // Hareke sırası farklı yazılmış olsa da (ötre önce, şedde sonra) aynı sonuç
        assertEquals("\u0639\u0644\u064A" + shadda,
                EsmaPage.spoken("\u0639\u0644\u064A" + damma + shadda));
    }

    // ---- Renk Teması ----

    @Test
    public void renkTemasiSecilinceUygulanir() throws Exception {
        MainActivity a = openPage(R.id.menu_tema, MainActivity.TEMA);
        LinearLayout list = (LinearLayout) a.findViewById(R.id.list_items);
        assertEquals(6, list.getChildCount());
        String[] names = {"Krem üzerine Turkuaz", "Beyaz üzerine Kırmızı", "Pudra üzerine Yeşil",
                "Pudra üzerine Turkuaz", "Siyah üzerine Beyaz", "Siyah üzerine Kırmızı"};
        for (int i = 0; i < 6; i++) {
            assertEquals(names[i], ((TextView) list.getChildAt(i).findViewById(R.id.tema_ad))
                    .getText().toString());
        }
        assertEquals(View.VISIBLE, list.getChildAt(0).findViewById(R.id.tema_secili).getVisibility());
        TestUtil.screenshot(a, "14-renk-temasi");

        list.getChildAt(4).performClick(); // Siyah üzerine Beyaz
        assertEquals(4, Prefs.theme(app));
    }

    /** Her temada ana ekran: zemin rengi temaya göre, yazılar okunur. */
    @Test
    public void herTemadaAnaEkranAcilir() throws Exception {
        int[] bgs = {0xFFFFFDF7, 0xFFFFFFFF, 0xFFF7E6E2, 0xFFF7E6E2, 0xFF000000, 0xFF000000};
        String[] shots = {null, "15a-tema-beyaz-kirmizi", "15b-tema-pudra-yesil",
                "15c-tema-pudra-turkuaz", "15d-tema-siyah-beyaz", "15e-tema-siyah-kirmizi"};
        for (int i = 0; i < Tema.ADLAR.length; i++) {
            Prefs.setTheme(app, i);
            MainActivity a = start();
            View root = a.findViewById(R.id.main_root);
            assertEquals(Tema.ADLAR[i], bgs[i], ((ColorDrawable) root.getBackground()).getColor());
            TextView time = (TextView) ((LinearLayout) a.findViewById(R.id.rows)).getChildAt(0)
                    .findViewById(R.id.row_time);
            assertEquals(Ui.color(a, R.attr.colTextPast), time.getCurrentTextColor()); // imsak geçti
            if (shots[i] != null) TestUtil.screenshot(a, shots[i]);
        }
    }

    // ---- En Yakın Cami ----

    @Test
    public void enYakinCamiSayfasiHaritaYukler() throws Exception {
        shadowOf(app).grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        MainActivity a = openPage(R.id.menu_cami, MainActivity.CAMI);
        CamiPage page = (CamiPage) a.page(MainActivity.CAMI);
        WebView web = (WebView) ((android.view.ViewGroup) a.findViewById(R.id.cami_map_box)).getChildAt(0);
        assertEquals("file:///android_asset/harita/harita.html", shadowOf(web).getLastLoadedUrl());
        assertTrue(web.getSettings().getJavaScriptEnabled());

        // İnternetten gelmiş gibi bir sonuç gösterilir.
        double lat = 39.9334, lon = 32.8597;
        page.showResult(CamiPage.parse(CamiTest.SAMPLE, lat, lon), null);
        assertEquals(3, page.found().size());
        LinearLayout list = (LinearLayout) a.findViewById(R.id.cami_list);
        assertEquals(3, list.getChildCount());
        assertEquals("2. Kocatepe Camii",
                ((TextView) list.getChildAt(1).findViewById(R.id.cami_ad)).getText().toString());
        TestUtil.screenshot(a, "16-en-yakin-cami");

        // "Yol Tarifi" Google Haritalar'ı açar.
        list.getChildAt(1).findViewById(R.id.cami_tarif).performClick();
        Intent maps = shadowOf(a).getNextStartedActivity();
        assertEquals(Intent.ACTION_VIEW, maps.getAction());
        String u = maps.getData().toString();
        assertTrue(u, u.startsWith("https://www.google.com/maps/dir/?api=1&destination=39.9"));
        assertTrue(u, u.endsWith("travelmode=walking")); // 1,85 km: yürüyerek

        // Haritadaki işarete dokunulunca (JavaScript köprüsü) de aynı şey olur.
        page.bridge().yolTarifi(1);
        ShadowLooper.idleMainLooper();
        Intent maps2 = shadowOf(a).getNextStartedActivity();
        assertNotNull(maps2);
        assertTrue(maps2.getData().toString().contains("destination="));

        // Google Haritalar'da arama düğmesi
        a.findViewById(R.id.cami_google).performClick();
        Intent geo = shadowOf(a).getNextStartedActivity();
        assertTrue(geo.getData().toString().startsWith("geo:"));
    }

    @Test
    public void camiSayfasiKonumIzniIster() {
        MainActivity a = openPage(R.id.menu_cami, MainActivity.CAMI);
        String[] asked = shadowOf(a).getLastRequestedPermission().requestedPermissions;
        assertEquals(android.Manifest.permission.ACCESS_COARSE_LOCATION, asked[0]);
    }

    static <T> T last(List<T> l) { return l.get(l.size() - 1); }
}
