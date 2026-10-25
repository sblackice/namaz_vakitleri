package tr.kolay.namazvakti;

import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Ana Sayfa: sıradaki vakte kalan süre (saat:dakika:saniye), bugünün 6 vakti ve aşağıda
 * her gün değişen "Günün Hadisi".
 */
final class HomePage extends Page {
    private final TextView[] nameViews = new TextView[PrayerCalc.COUNT];
    private final TextView[] timeViews = new TextView[PrayerCalc.COUNT];
    private final View[] rowViews = new View[PrayerCalc.COUNT];
    private final TextView placeView, dateView, hijriView, nextLabel, countdown, nextTimeView;
    private final TextView hadithText, hadithNarrator, hadithSource;
    private final View noPlaceBox, timesBox;
    private long nextAt;
    private int hadithIndex = -1;

    HomePage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_home);
        placeView = (TextView) view.findViewById(R.id.place);
        dateView = (TextView) view.findViewById(R.id.date);
        hijriView = (TextView) view.findViewById(R.id.hijri);
        nextLabel = (TextView) view.findViewById(R.id.next_label);
        countdown = (TextView) view.findViewById(R.id.countdown);
        nextTimeView = (TextView) view.findViewById(R.id.next_time);
        hadithText = (TextView) view.findViewById(R.id.hadith_text);
        hadithNarrator = (TextView) view.findViewById(R.id.hadith_narrator);
        hadithSource = (TextView) view.findViewById(R.id.hadith_source);
        noPlaceBox = view.findViewById(R.id.no_place_box);
        timesBox = view.findViewById(R.id.times_box);

        LinearLayout rows = (LinearLayout) view.findViewById(R.id.rows);
        LayoutInflater inf = a.getLayoutInflater();
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
                activity.startActivity(new Intent(activity, CityActivity.class));
            }
        };
        view.findViewById(R.id.btn_city).setOnClickListener(openCity);
        view.findViewById(R.id.btn_choose_place).setOnClickListener(openCity);
        placeView.setOnClickListener(openCity);
    }

    @Override
    void onShow() {
        fitCountdown();
        refresh();
    }

    /** Geri sayım her yazı boyutunda tek satıra sığsın: gerekirse yazıyı küçültür. */
    private void fitCountdown() {
        float density = activity.getResources().getDisplayMetrics().density;
        // Sayfa kenar boşluğu (2 x 16dp) ve kutunun iç boşluğu (2 x 14dp) çıkarılır.
        float avail = activity.getResources().getDisplayMetrics().widthPixels - 60 * density;
        float w = countdown.getPaint().measureText("00:00:00");
        if (w > avail) {
            countdown.setTextSize(TypedValue.COMPLEX_UNIT_PX, countdown.getTextSize() * avail / w * 0.97f);
        }
        countdown.setMaxLines(1);
    }

    @Override
    void onTick(long now) {
        if (nextAt == 0) return;
        int h = Hadisler.indexFor(now, Times.tz(activity));
        if (now >= nextAt || h != hadithIndex) {
            refresh(); // vakit girdi veya gün değişti
        } else {
            countdown.setText(Times.hms(nextAt - now));
        }
    }

    void refresh() {
        long now = Clock.now();
        boolean has = Prefs.hasLocation(activity);
        noPlaceBox.setVisibility(has ? View.GONE : View.VISIBLE);
        timesBox.setVisibility(has ? View.VISIBLE : View.GONE);
        if (!has) {
            nextAt = 0;
            return;
        }

        placeView.setText(Prefs.placeName(activity));
        dateView.setText(Times.dateLine(activity, now));
        String hijri = Times.hijriLine(activity, now);
        hijriView.setText(hijri);
        hijriView.setVisibility(hijri.length() == 0 ? View.GONE : View.VISIBLE);

        long[] today = Times.forDay(activity, now, 0);
        Times.Next next = Times.next(activity, now);
        int highlight = next.tomorrow ? -1 : next.index;
        for (int i = 0; i < PrayerCalc.COUNT; i++) {
            timeViews[i].setText(Times.hhmm(activity, today[i]));
            int bg, color;
            if (i == highlight) {
                bg = R.drawable.row_next;
                color = activity.getColor(R.color.on_accent);
            } else {
                bg = R.drawable.row_normal;
                color = activity.getColor(today[i] <= now ? R.color.text_past : R.color.text_main);
            }
            rowViews[i].setBackgroundResource(bg);
            nameViews[i].setTextColor(color);
            timeViews[i].setTextColor(color);
        }
        String name = Times.NAMES[next.index];
        nextLabel.setText((next.tomorrow ? "Yarın " : "") + Times.upper(name) + " vaktine kalan süre");
        nextTimeView.setText((next.tomorrow ? "Yarın " : "") + name + " vakti: "
                + Times.hhmm(activity, next.time));
        nextAt = next.time;
        countdown.setText(Times.hms(nextAt - now));

        hadithIndex = Hadisler.indexFor(now, Times.tz(activity));
        Hadisler.Hadis h = Hadisler.LISTE[hadithIndex];
        // Konuşma içeren hadislerde tırnaklar metnin içinde zaten var.
        hadithText.setText(h.metin.contains("“") ? h.metin : "“" + h.metin + "”");
        hadithNarrator.setText("Rivayet eden: " + h.ravi);
        hadithSource.setText("Kaynak: " + h.kaynak);
    }
}
