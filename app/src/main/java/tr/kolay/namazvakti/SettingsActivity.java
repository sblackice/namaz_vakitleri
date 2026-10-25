package tr.kolay.namazvakti;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Diğer ayarlar: yazı boyutu ve her vakit için dakika düzeltmesi. */
public class SettingsActivity extends Activity {
    private Button scaleBtn;
    private final TextView[] offsetValues = new TextView[PrayerCalc.COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Ui.header(findViewById(R.id.settings_page), "Kolay Namaz Vakti", "Diğer Ayarlar");
        scaleBtn = (Button) findViewById(R.id.btn_scale);
        scaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                float cur = Prefs.textScale(SettingsActivity.this);
                int idx = 0;
                for (int i = 0; i < Prefs.SCALE_CHOICES.length; i++) {
                    if (Math.abs(Prefs.SCALE_CHOICES[i] - cur) < 0.01f) idx = i;
                }
                Prefs.setTextScale(SettingsActivity.this,
                        Prefs.SCALE_CHOICES[(idx + 1) % Prefs.SCALE_CHOICES.length]);
                recreate();
            }
        });
        findViewById(R.id.btn_settings_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        LinearLayout box = (LinearLayout) findViewById(R.id.offset_rows);
        LayoutInflater inf = getLayoutInflater();
        for (int i = 0; i < PrayerCalc.COUNT; i++) {
            final int idx = i;
            View row = inf.inflate(R.layout.row_offset, box, false);
            ((TextView) row.findViewById(R.id.off_name)).setText(Times.NAMES[i]);
            offsetValues[i] = (TextView) row.findViewById(R.id.off_value);
            row.findViewById(R.id.off_minus).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { bump(idx, -1); }
            });
            row.findViewById(R.id.off_plus).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { bump(idx, 1); }
            });
            box.addView(row);
        }
        Ui.scaleText(findViewById(R.id.settings_root), Prefs.textScale(this));
        show();
    }

    static int nextChoice(int[] choices, int cur) {
        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == cur) return choices[(i + 1) % choices.length];
        }
        return choices[0];
    }

    void bump(int i, int delta) {
        Prefs.setOffset(this, i, Prefs.offsets(this)[i] + delta);
        show();
        AlarmScheduler.schedule(this);
        NextPrayerWidget.updateAll(this);
    }

    void show() {
        float s = Prefs.textScale(this);
        String size = s < 1.1f ? "Büyük" : (s < 1.3f ? "Daha büyük" : "En büyük");
        scaleBtn.setText("Yazı boyutu: " + size);
        int[] off = Prefs.offsets(this);
        for (int i = 0; i < off.length; i++) {
            offsetValues[i].setText(off[i] > 0 ? "+" + off[i] : String.valueOf(off[i]));
        }
    }
}
