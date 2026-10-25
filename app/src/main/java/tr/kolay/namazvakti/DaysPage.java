package tr.kolay.namazvakti;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.TimeZone;

/** Dini Gün ve Geceler: yaklaşanlar tarih sırasıyla; dokununca bilgi sayfası açılır. */
final class DaysPage extends Page {
    static final String[] AYLAR = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};

    private final LinearLayout list;
    private long builtForDay = Long.MIN_VALUE;

    DaysPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_days);
        list = (LinearLayout) view.findViewById(R.id.days_list);
        ((TextView) view.findViewById(R.id.days_note)).setText(DiniGunler.TARIH_KAYNAGI);
    }

    @Override
    void onTick(long now) { onShow(); } // gece yarısı geçince "kaç gün kaldı" yenilenir

    @Override
    void onShow() {
        long now = Clock.now();
        TimeZone tz = Times.tz(activity);
        long day = Math.floorDiv(now + tz.getOffset(now), 86400000L);
        if (day != builtForDay) {
            build(now, tz);
            builtForDay = day;
        }
    }

    private void build(long now, TimeZone tz) {
        list.removeAllViews();
        LayoutInflater inf = activity.getLayoutInflater();
        List<DiniGunler.Gun> gunler = DiniGunler.yaklasanlar(now, tz);
        if (gunler.isEmpty()) {
            TextView t = (TextView) inf.inflate(R.layout.item_section, list, false);
            t.setText("Kayıtlı tarihlerin hepsi geçti. Yeni tarihler için uygulamayı güncelleyin.");
            list.addView(t);
        }
        for (final DiniGunler.Gun g : gunler) {
            View item = inf.inflate(R.layout.item_day, list, false);
            ((TextView) item.findViewById(R.id.day_num)).setText(String.valueOf(g.gun));
            ((TextView) item.findViewById(R.id.day_month)).setText(AYLAR[g.ay - 1]);
            ((TextView) item.findViewById(R.id.day_year)).setText(String.valueOf(g.yil));
            ((TextView) item.findViewById(R.id.day_name)).setText(g.ad);
            ((TextView) item.findViewById(R.id.day_weekday)).setText(subLine(g));
            ((TextView) item.findViewById(R.id.day_left)).setText(leftText(g, now, tz));
            item.setContentDescription(g.ad + ", " + Times.longDate(g.yil, g.ay, g.gun));
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    activity.startActivity(new Intent(activity, DayDetailActivity.class)
                            .putExtra(DayDetailActivity.EXTRA_INDEX, indexOf(g)));
                }
            });
            activity.scale((ViewGroup) item);
            list.addView(item);
        }
    }

    static int indexOf(DiniGunler.Gun g) {
        for (int i = 0; i < DiniGunler.LISTE.length; i++) {
            if (DiniGunler.LISTE[i] == g) return i;
        }
        return -1;
    }

    /** "Perşembe akşamı" veya "3 gün sürer" gibi ikinci satır. */
    static String subLine(DiniGunler.Gun g) {
        String date = Times.longDate(g.yil, g.ay, g.gun);
        String weekday = date.substring(date.lastIndexOf(", ") + 2);
        if (g.gece()) return weekday + " akşamı başlar";
        if (g.sureGun > 1) return weekday + " • " + g.sureGun + " gün sürer";
        return weekday;
    }

    static String leftText(DiniGunler.Gun g, long now, TimeZone tz) {
        int k = DiniGunler.kalanGun(g, now, tz);
        if (k < 0) return "Devam ediyor";
        if (k == 0) return g.gece() ? "Bu akşam" : "Bugün";
        if (k == 1) return g.gece() ? "Yarın akşam" : "Yarın";
        return k + " gün kaldı";
    }
}
