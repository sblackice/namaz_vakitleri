package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Ana pencere. En altta 5 menü düğmesi vardır:
 * Ana Sayfa, Ezan (ve izinler), Dini Günler, Kıble, Dualar.
 */
public class MainActivity extends Activity {
    static final String NOTIF_PERMISSION = "android.permission.POST_NOTIFICATIONS";
    static final String EXTRA_TAB = "tab";
    static final int HOME = 0, ADHAN = 1, DAYS = 2, QIBLA = 3, DUAS = 4;
    static final int[] TAB_IDS = {R.id.tab_home, R.id.tab_adhan, R.id.tab_days, R.id.tab_qibla,
            R.id.tab_duas};
    private static final int[] TAB_LABELS = {R.string.tab_home, R.string.tab_adhan,
            R.string.tab_days, R.string.tab_qibla, R.string.tab_duas};
    private static final int[] TAB_ICONS = {R.drawable.tab_home, R.drawable.tab_adhan,
            R.drawable.tab_days, R.drawable.tab_qibla, R.drawable.tab_duas};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Page[] pages;
    private View[] tabs;
    private int current = -1;
    private boolean resumed;
    private float appliedScale;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            long now = Clock.now();
            if (current >= 0) pages[current].onTick(now);
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000) + 5);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appliedScale = Prefs.textScale(this);

        FrameLayout content = (FrameLayout) findViewById(R.id.content);
        pages = new Page[] {new HomePage(this, content), new AdhanPage(this, content),
                new DaysPage(this, content), new QiblaPage(this, content), new DuasPage(this, content)};
        for (Page p : pages) {
            Ui.scaleText(p.view, appliedScale);
            p.view.setVisibility(View.GONE);
            content.addView(p.view);
        }

        LinearLayout nav = (LinearLayout) findViewById(R.id.bottom_nav);
        tabs = new View[pages.length];
        for (int i = 0; i < pages.length; i++) {
            final int idx = i;
            View t = getLayoutInflater().inflate(R.layout.item_tab, nav, false);
            t.setId(TAB_IDS[i]);
            ((ImageView) t.findViewById(R.id.tab_icon)).setImageResource(TAB_ICONS[i]);
            ((TextView) t.findViewById(R.id.tab_label)).setText(TAB_LABELS[i]);
            t.setContentDescription(getString(TAB_LABELS[i]));
            t.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { select(idx); }
            });
            tabs[i] = t;
            nav.addView(t);
        }

        int start = savedInstanceState != null ? savedInstanceState.getInt(EXTRA_TAB, HOME)
                : getIntent().getIntExtra(EXTRA_TAB, HOME);
        select(start);
        askNotificationPermission();
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

    /** Alt menüden bir sayfa seçer. */
    void select(int idx) {
        if (idx < 0 || idx >= pages.length) idx = HOME;
        if (idx == current) return;
        if (current >= 0) {
            if (resumed) pages[current].onHide();
            pages[current].view.setVisibility(View.GONE);
        }
        current = idx;
        pages[idx].view.setVisibility(View.VISIBLE);
        for (int i = 0; i < tabs.length; i++) styleTab(tabs[i], i == idx);
        if (resumed) pages[idx].onShow();
    }

    int currentTab() { return current; }

    Page page(int idx) { return pages[idx]; }

    private void styleTab(View t, boolean selected) {
        t.setBackgroundResource(selected ? R.drawable.nav_selected : R.drawable.nav_normal);
        t.setSelected(selected);
        int color = getColor(selected ? R.color.on_accent : R.color.text_main);
        ((TextView) t.findViewById(R.id.tab_label)).setTextColor(color);
        ((ImageView) t.findViewById(R.id.tab_icon)).setImageTintList(ColorStateList.valueOf(color));
    }

    void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(NOTIF_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {NOTIF_PERMISSION}, AdhanPage.REQ_NOTIF);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appliedScale != Prefs.textScale(this)) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Page p : pages) p.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (current == ADHAN && resumed) pages[ADHAN].onShow();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (current != HOME) {
            select(HOME);
        } else {
            super.onBackPressed();
        }
    }

    /** Dinamik olarak eklenen satırların yazılarını da seçili boyuta büyütür. */
    void scale(ViewGroup v) { Ui.scaleText(v, appliedScale); }
}
