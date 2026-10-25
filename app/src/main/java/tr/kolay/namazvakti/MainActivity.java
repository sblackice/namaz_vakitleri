package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Ana ekran: bugünün 6 vakti büyük yazıyla ve sıradaki vakte kalan süre. */
public class MainActivity extends Activity {
    static final String NOTIF_PERMISSION = "android.permission.POST_NOTIFICATIONS";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView[] nameViews = new TextView[PrayerCalc.COUNT];
    private final TextView[] timeViews = new TextView[PrayerCalc.COUNT];
    private final View[] rowViews = new View[PrayerCalc.COUNT];
    private TextView placeView, dateView, hijriView, nextView, countdownView;
    private View card, noPlaceBox, timesBox;
    private float appliedScale;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            refresh();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        placeView = (TextView) findViewById(R.id.place);
        dateView = (TextView) findViewById(R.id.date);
        hijriView = (TextView) findViewById(R.id.hijri);
        nextView = (TextView) findViewById(R.id.next_label);
        countdownView = (TextView) findViewById(R.id.countdown);
        card = findViewById(R.id.next_card);
        noPlaceBox = findViewById(R.id.no_place_box);
        timesBox = findViewById(R.id.times_box);

        LinearLayout rows = (LinearLayout) findViewById(R.id.rows);
        LayoutInflater inf = getLayoutInflater();
        for (int i = 0; i < PrayerCalc.COUNT; i++) {
            View row = inf.inflate(R.layout.row_prayer, rows, false);
            nameViews[i] = (TextView) row.findViewById(R.id.row_name);
            timeViews[i] = (TextView) row.findViewById(R.id.row_time);
            nameViews[i].setText(Times.NAMES[i]);
            rowViews[i] = row;
            rows.addView(row);
        }

        View.OnClickListener openCity = new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CityActivity.class));
            }
        };
        findViewById(R.id.btn_city).setOnClickListener(openCity);
        findViewById(R.id.btn_choose_place).setOnClickListener(openCity);
        placeView.setOnClickListener(openCity);
        findViewById(R.id.btn_qibla).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QiblaActivity.class));
            }
        });
        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        appliedScale = Prefs.textScale(this);
        Ui.scaleText(findViewById(R.id.root), appliedScale);
        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(NOTIF_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {NOTIF_PERMISSION}, 1);
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
        refresh();
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 10000);
        AlarmScheduler.schedule(this);
        NextPrayerWidget.updateAll(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    void refresh() {
        long now = Clock.now();
        boolean has = Prefs.hasLocation(this);
        noPlaceBox.setVisibility(has ? View.GONE : View.VISIBLE);
        timesBox.setVisibility(has ? View.VISIBLE : View.GONE);
        if (!has) return;

        placeView.setText(Prefs.placeName(this));
        dateView.setText(Times.dateLine(this, now));
        String hijri = Times.hijriLine(this, now);
        hijriView.setText(hijri);
        hijriView.setVisibility(hijri.length() == 0 ? View.GONE : View.VISIBLE);

        long[] today = Times.forDay(this, now, 0);
        Times.Next next = Times.next(this, now);
        int highlight = next.tomorrow ? -1 : next.index;
        for (int i = 0; i < PrayerCalc.COUNT; i++) {
            timeViews[i].setText(Times.hhmm(this, today[i]));
            int bg, color;
            if (i == highlight) {
                bg = R.drawable.row_next;
                color = getColor(R.color.on_accent);
            } else {
                bg = R.drawable.row_normal;
                color = getColor(today[i] <= now ? R.color.text_past : R.color.text_main);
            }
            rowViews[i].setBackgroundResource(bg);
            nameViews[i].setTextColor(color);
            timeViews[i].setTextColor(color);
        }
        String when = next.tomorrow ? " (yarın " : " (";
        nextView.setText("Sıradaki vakit: " + Times.upper(Times.NAMES[next.index])
                + when + Times.hhmm(this, next.time) + ")");
        countdownView.setText(Times.remaining(next.time - now));
    }
}
