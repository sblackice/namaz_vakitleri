package tr.kolay.namazvakti;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Calendar;
import java.util.TimeZone;

/** Bir dini gün veya gece hakkında bilgi sayfası. */
public class DayDetailActivity extends Activity {
    static final String EXTRA_INDEX = "index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        int idx = getIntent().getIntExtra(EXTRA_INDEX, -1);
        if (idx < 0 || idx >= DiniGunler.LISTE.length) {
            finish();
            return;
        }
        DiniGunler.Gun g = DiniGunler.LISTE[idx];
        TimeZone tz = Times.tz(this);
        long now = Clock.now();

        View page = findViewById(R.id.day_page);
        Ui.header(page, "Dini Gün ve Geceler", g.ad);
        Ui.headerAction(page, R.drawable.ic_back, "Geri", new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        ((TextView) findViewById(R.id.detail_date)).setText(dateText(g));
        ((TextView) findViewById(R.id.detail_hijri)).setText("Hicri: " + g.hicri);
        ((TextView) findViewById(R.id.detail_left)).setText(DaysPage.leftText(g, now, tz));
        DiniGunler.Bilgi b = DiniGunler.bilgi(g.tur);
        ((TextView) findViewById(R.id.detail_text)).setText(b.metin);
        TextView src = (TextView) findViewById(R.id.detail_source);
        src.setText(b.kaynak.length() == 0 ? "" : "Kaynak: " + b.kaynak);
        src.setVisibility(b.kaynak.length() == 0 ? View.GONE : View.VISIBLE);
        ((TextView) findViewById(R.id.detail_note)).setText(DiniGunler.TARIH_KAYNAGI);
        Ui.scaleText(page, Prefs.textScale(this));
    }

    static String dateText(DiniGunler.Gun g) {
        String start = Times.longDate(g.yil, g.ay, g.gun);
        if (g.gece()) return start + " akşamı";
        if (g.sureGun > 1) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.clear();
            c.set(g.yil, g.ay - 1, g.gun);
            c.add(Calendar.DAY_OF_MONTH, g.sureGun - 1);
            String end = Times.longDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.DAY_OF_MONTH));
            return start + " –\n" + end + " (" + g.sureGun + " gün)";
        }
        return start;
    }
}
