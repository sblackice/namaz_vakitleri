package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Ana pencere. En altta 6 menü düğmesi vardır:
 * Ana Sayfa, Ezan (ve izinler), Dini Günler, Kıble, Dualar ve en sağda Menü.
 * Menü düğmesi yukarı doğru açılan bir panel gösterir: En Yakın Cami, Esmâ-ül Hüsnâ,
 * Zikirmatik, Renk Teması.
 */
public class MainActivity extends Activity {
    static final String NOTIF_PERMISSION = "android.permission.POST_NOTIFICATIONS";
    static final String EXTRA_TAB = "tab";
    static final int HOME = 0, ADHAN = 1, DAYS = 2, QIBLA = 3, DUAS = 4;
    /** Menü panelindeki sayfalar. */
    static final int CAMI = 5, ESMA = 6, ZIKIR = 7, TEMA = 8;
    static final int PAGE_COUNT = 9;
    static final int TAB_COUNT = 5;

    static final int[] TAB_IDS = {R.id.tab_home, R.id.tab_adhan, R.id.tab_days, R.id.tab_qibla,
            R.id.tab_duas};
    private static final int[] TAB_LABELS = {R.string.tab_home, R.string.tab_adhan,
            R.string.tab_days, R.string.tab_qibla, R.string.tab_duas};
    private static final int[] TAB_ICONS = {R.drawable.tab_home, R.drawable.tab_adhan,
            R.drawable.tab_days, R.drawable.tab_qibla, R.drawable.tab_duas};

    static final int[] MENU_IDS = {R.id.menu_cami, R.id.menu_esma, R.id.menu_zikir, R.id.menu_tema};
    private static final int[] MENU_PAGES = {CAMI, ESMA, ZIKIR, TEMA};
    private static final int[] MENU_LABELS = {R.string.menu_cami, R.string.menu_esma,
            R.string.menu_zikir, R.string.menu_tema};
    private static final int[] MENU_ICONS = {R.drawable.ic_cami, R.drawable.ic_esma,
            R.drawable.ic_zikir, R.drawable.ic_tema};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Page[] pages = new Page[PAGE_COUNT];
    private final View[] tabs = new View[TAB_COUNT + 1];
    private FrameLayout content;
    private View menuOverlay, menuPanel;
    private int current = -1;
    private boolean resumed;
    private float appliedScale;
    private int appliedTheme;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            long now = Clock.now();
            if (current >= 0) pages[current].onTick(now);
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000) + 5);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tema.uygula(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appliedScale = Prefs.textScale(this);
        appliedTheme = Tema.secili(this);
        content = (FrameLayout) findViewById(R.id.content);

        LinearLayout nav = (LinearLayout) findViewById(R.id.bottom_nav);
        LayoutInflater inf = getLayoutInflater();
        for (int i = 0; i <= TAB_COUNT; i++) {
            final int idx = i;
            boolean menu = i == TAB_COUNT;
            View t = inf.inflate(R.layout.item_tab, nav, false);
            int label = menu ? R.string.tab_menu : TAB_LABELS[i];
            t.setId(menu ? R.id.tab_menu : TAB_IDS[i]);
            ((ImageView) t.findViewById(R.id.tab_icon)).setImageResource(
                    menu ? R.drawable.tab_menu : TAB_ICONS[i]);
            ((TextView) t.findViewById(R.id.tab_label)).setText(label);
            t.setContentDescription(getString(label));
            t.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (idx == TAB_COUNT) {
                        toggleMenu();
                    } else {
                        closeMenu();
                        select(idx);
                    }
                }
            });
            tabs[i] = t;
            nav.addView(t);
        }
        buildMenu(inf);

        int start = savedInstanceState != null ? savedInstanceState.getInt(EXTRA_TAB, HOME)
                : getIntent().getIntExtra(EXTRA_TAB, HOME);
        select(start);
        askNotificationPermission();
    }

    /** Menü paneli: alttaki Menü düğmesinin üstünde, yukarı doğru açılır. */
    private void buildMenu(LayoutInflater inf) {
        menuOverlay = findViewById(R.id.menu_overlay);
        menuPanel = findViewById(R.id.menu_panel);
        findViewById(R.id.menu_scrim).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { closeMenu(); }
        });
        LinearLayout items = (LinearLayout) findViewById(R.id.menu_items);
        for (int i = 0; i < MENU_PAGES.length; i++) {
            final int page = MENU_PAGES[i];
            View item = inf.inflate(R.layout.item_menu, items, false);
            item.setId(MENU_IDS[i]);
            ((ImageView) item.findViewById(R.id.menu_icon)).setImageResource(MENU_ICONS[i]);
            ((TextView) item.findViewById(R.id.menu_label)).setText(MENU_LABELS[i]);
            item.setContentDescription(getString(MENU_LABELS[i]));
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    closeMenu();
                    select(page);
                }
            });
            items.addView(item);
        }
        Ui.scaleText(menuPanel, appliedScale);
    }

    boolean menuOpen() { return menuOverlay.getVisibility() == View.VISIBLE; }

    void toggleMenu() {
        if (menuOpen()) closeMenu(); else openMenu();
    }

    void openMenu() {
        if (menuOpen()) return;
        menuOverlay.setVisibility(View.VISIBLE);
        styleTab(tabs[TAB_COUNT], true);
        // Aşağıdan yukarı doğru kayarak açılır.
        menuPanel.setTranslationY(Ui.dp(this, 320));
        menuPanel.setAlpha(0f);
        menuPanel.animate().translationY(0).alpha(1f).setDuration(220).start();
    }

    void closeMenu() {
        if (!menuOpen()) return;
        menuPanel.animate().cancel();
        menuOverlay.setVisibility(View.GONE);
        styleTab(tabs[TAB_COUNT], current >= TAB_COUNT);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_TAB)) select(intent.getIntExtra(EXTRA_TAB, HOME));
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt(EXTRA_TAB, current);
    }

    /** Sayfayı ilk gerektiğinde oluşturur (harita gibi ağır sayfalar açılışı yavaşlatmasın). */
    Page page(int idx) {
        if (pages[idx] == null) {
            Page p;
            switch (idx) {
                case HOME: p = new HomePage(this, content); break;
                case ADHAN: p = new AdhanPage(this, content); break;
                case DAYS: p = new DaysPage(this, content); break;
                case QIBLA: p = new QiblaPage(this, content); break;
                case DUAS: p = new DuasPage(this, content); break;
                case CAMI: p = new CamiPage(this, content); break;
                case ESMA: p = new EsmaPage(this, content); break;
                case ZIKIR: p = new ZikirPage(this, content); break;
                default: p = new TemaPage(this, content); break;
            }
            Ui.scaleText(p.view, appliedScale);
            p.view.setVisibility(View.GONE);
            content.addView(p.view);
            pages[idx] = p;
        }
        return pages[idx];
    }

    /** Alt menüden veya Menü panelinden bir sayfa seçer. */
    void select(int idx) {
        if (idx < 0 || idx >= PAGE_COUNT) idx = HOME;
        if (idx == current) return;
        Page next = page(idx);
        if (current >= 0) {
            if (resumed) pages[current].onHide();
            pages[current].view.setVisibility(View.GONE);
        }
        current = idx;
        next.view.setVisibility(View.VISIBLE);
        for (int i = 0; i < TAB_COUNT; i++) styleTab(tabs[i], i == idx);
        styleTab(tabs[TAB_COUNT], idx >= TAB_COUNT || menuOpen());
        if (resumed) next.onShow();
    }

    int currentTab() { return current; }

    private void styleTab(View t, boolean selected) {
        t.setBackgroundResource(selected ? R.drawable.nav_selected : R.drawable.nav_normal);
        t.setSelected(selected);
        int color = Ui.color(this, selected ? R.attr.colOnAccent : R.attr.colText);
        ((TextView) t.findViewById(R.id.tab_label)).setTextColor(color);
        ((ImageView) t.findViewById(R.id.tab_icon)).setImageTintList(ColorStateList.valueOf(color));
    }

    void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(NOTIF_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {NOTIF_PERMISSION}, AdhanPage.REQ_NOTIF);
        }
    }

    /** Renk teması veya yazı boyutu değiştiyse pencereyi yeniden kurar. */
    void applySettingsIfChanged() {
        if (appliedScale != Prefs.textScale(this) || appliedTheme != Tema.secili(this)) {
            recreate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appliedScale != Prefs.textScale(this) || appliedTheme != Tema.secili(this)) {
            recreate();
            return;
        }
        if (!Prefs.hasLocation(this) && !Prefs.askedFirstRun(this)) {
            Prefs.setAskedFirstRun(this);
            startActivity(new Intent(this, CityActivity.class));
        }
        resumed = true;
        pages[current].onShow();
        handler.removeCallbacks(tick);
        handler.post(tick);
        AlarmScheduler.schedule(this);
        NextPrayerWidget.updateAll(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        handler.removeCallbacks(tick);
        if (current >= 0) pages[current].onHide();
    }

    @Override
    protected void onDestroy() {
        for (Page p : pages) {
            if (p != null) p.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Page p : pages) {
            if (p != null) p.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (resumed && current >= 0) pages[current].onPermissionResult(requestCode);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (menuOpen()) {
            closeMenu();
        } else if (current != HOME) {
            select(HOME);
        } else {
            super.onBackPressed();
        }
    }

    /** Dinamik olarak eklenen satırların yazılarını da seçili boyuta büyütür. */
    void scale(ViewGroup v) { Ui.scaleText(v, appliedScale); }
}
