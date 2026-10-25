package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Basit ayarlar: büyük düğmeler, her düğmeye basınca değer değişir. */
public class SettingsActivity extends Activity {
    private Button notifyBtn, remindBtn, scaleBtn;
    private final TextView[] offsetValues = new TextView[PrayerCalc.COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        notifyBtn = (Button) findViewById(R.id.btn_notify);
        remindBtn = (Button) findViewById(R.id.btn_remind);
        scaleBtn = (Button) findViewById(R.id.btn_scale);

        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Prefs.setNotifyOn(SettingsActivity.this, !Prefs.notifyOn(SettingsActivity.this));
                changed();
            }
        });
        remindBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Prefs.setRemindBefore(SettingsActivity.this,
                        nextChoice(Prefs.REMIND_CHOICES, Prefs.remindBefore(SettingsActivity.this)));
                changed();
            }
        });
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
        findViewById(R.id.btn_sound).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openSoundSettings(); }
        });
        findViewById(R.id.btn_battery).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                safeStart(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
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
        changed();
    }

    private void changed() {
        show();
        AlarmScheduler.schedule(this);
        NextPrayerWidget.updateAll(this);
    }

    void show() {
        notifyBtn.setText("Vakit girince bildirim: " + (Prefs.notifyOn(this) ? "AÇIK" : "KAPALI"));
        int r = Prefs.remindBefore(this);
        remindBtn.setText("Vakitten önce hatırlat: " + (r == 0 ? "KAPALI" : r + " dakika"));
        float s = Prefs.textScale(this);
        String size = s < 1.1f ? "Büyük" : (s < 1.3f ? "Daha büyük" : "En büyük");
        scaleBtn.setText("Yazı boyutu: " + size);
        int[] off = Prefs.offsets(this);
        for (int i = 0; i < off.length; i++) {
            offsetValues[i].setText(off[i] > 0 ? "+" + off[i] : String.valueOf(off[i]));
        }
    }

    private void openSoundSettings() {
        Intent i;
        if (Build.VERSION.SDK_INT >= 26) {
            Notifier.ensureChannel(this);
            i = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                    .putExtra(Settings.EXTRA_CHANNEL_ID, Notifier.CHANNEL_ID);
        } else {
            i = new Intent(Settings.ACTION_SOUND_SETTINGS);
        }
        safeStart(i);
    }

    private void safeStart(Intent i) {
        try {
            startActivity(i);
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }
}
