package tr.kolay.namazvakti;

import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Ana Sayfa: sıradaki vakte kalan süre (saat:dakika:saniye), bugünün 6 vakti (ekrana sığacak
 * yükseklikte) ve aşağıda
 * her gün değişen "Günün Hadisi".
 */
final class HomePage extends Page {
    private final TextView[] nameViews = new TextView[PrayerCalc.COUNT];
    private final TextView[] timeViews = new TextView[PrayerCalc.COUNT];
    private final View[] rowViews = new View[PrayerCalc.COUNT];
    private final TextView dateView, hijriView, nextLabel, countdown, nextTimeView;
    private final TextView hadithText, hadithNarrator, hadithSource, scrollHint;
    private final View noPlaceBox, timesBox;
    private final LinearLayout rowsBox;
    private final ScrollView scroll;
    private long nextAt;
    private int hadithIndex = -1;

    HomePage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_home);
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
        scrollHint = (TextView) view.findViewById(R.id.scroll_hint);
        scroll = (ScrollView) view.findViewById(R.id.home_scroll);

        LinearLayout rows = (LinearLayout) view.findViewById(R.id.rows);
        rowsBox = rows;
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
        view.findViewById(R.id.btn_choose_place).setOnClickListener(openCity);
        Ui.headerAction(view, R.drawable.ic_pin, "Değiştir", openCity);

        // Ekran boyu değişince (ilk açılış, yazı boyutu) vakit satırları ekrana sığdırılır.
        scroll.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int l, int t, int r, int b,
                    int ol, int ot, int or, int ob) {
                if (b - t != ob - ot) fitRows();
            }
        });
    }

    /**
     * Günün 6 vakti ilk ekranda tam görünsün: satır yüksekliği, kalan boşluğa göre hesaplanır.
     * Satır, yazının sığacağı en küçük yükseklikten daha kısa yapılmaz (o zaman sayfa kayar).
     */
    void fitRows() {
        int h = scroll.getHeight();
        if (h == 0 || timesBox.getVisibility() != View.VISIBLE) return;
        float d = activity.getResources().getDisplayMetrics().density;
        int top = rowsBox.getTop() + timesBox.getTop();
        LinearLayout.LayoutParams hintLp = (LinearLayout.LayoutParams) scrollHint.getLayoutParams();
        int hint = scrollHint.getHeight() + hintLp.topMargin;
        int gap = Math.round(5 * d);
        int each = (h - top - hint - Math.round(4 * d)) / PrayerCalc.COUNT - gap;
        int min = Math.round(Math.max(timeViews[0].getTextSize(), nameViews[0].getTextSize()) * 1.15f
                + 6 * d);
        int max = Math.round(80 * d);
        each = Math.max(min, Math.min(max, each));
        boolean changed = false;
        for (View row : rowViews) {
            ViewGroup.LayoutParams lp = row.getLayoutParams();
            if (lp.height != each) {
                lp.height = each;
                row.setLayoutParams(lp);
                changed = true;
            }
        }
        if (changed) rowsBox.requestLayout();
    }

    @Override
    void onShow() {
        fitCountdown();
        refresh();
        scroll.post(new Runnable() {
            @Override public void run() { fitRows(); }
        });
    }

    /** Geri sayım her yazı boyutunda tek satıra sığsın: gerekirse yazıyı küçültür. */
    private void fitCountdown() {
        float density = activity.getResources().getDisplayMetrics().density;
        // Sayfa kenar boşluğu (2 x 14dp) ve kutunun iç boşluğu (2 x 10dp) çıkarılır.
        float avail = activity.getResources().getDisplayMetrics().widthPixels - 52 * density;
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
            Ui.header(view, "Kolay Namaz Vakti", "Hoş geldiniz");
            nextAt = 0;
            return;
        }

        Ui.header(view, "Türkiye", Prefs.placeName(activity));
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
