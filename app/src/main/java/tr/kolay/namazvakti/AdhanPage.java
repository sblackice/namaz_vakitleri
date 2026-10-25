package tr.kolay.namazvakti;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Ezan ve İzinler: hangi vakitlerde ezan okunacağı, ezan sesi seçimi ve ezanın zamanında
 * okunması için gereken telefon izinlerinin durumu.
 */
final class AdhanPage extends Page {
    static final int REQ_SOUND = 41;
    static final int REQ_NOTIF = 42;
    static final int[] PRAYERS = {PrayerCalc.IMSAK, PrayerCalc.OGLE, PrayerCalc.IKINDI,
            PrayerCalc.AKSAM, PrayerCalc.YATSI};

    // İzin satırları
    static final int P_NOTIF = 0, P_EXACT = 1, P_BATTERY = 2, P_VOLUME = 3, P_FILE = 4;

    private final Button[] toggles = new Button[PRAYERS.length];
    private final TextView soundState;
    private final Button pickBtn, testBtn, defaultBtn, quietBtn, remindBtn;
    private final View[] permRows = new View[5];

    AdhanPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_adhan);
        LayoutInflater inf = a.getLayoutInflater();

        LinearLayout rows = (LinearLayout) view.findViewById(R.id.adhan_rows);
        for (int k = 0; k < PRAYERS.length; k++) {
            final int idx = PRAYERS[k];
            View row = inf.inflate(R.layout.row_adhan, rows, false);
            ((TextView) row.findViewById(R.id.adhan_name)).setText(
                    idx == PrayerCalc.IMSAK ? "İmsak (Sabah)" : Times.NAMES[idx]);
            toggles[k] = (Button) row.findViewById(R.id.adhan_toggle);
            toggles[k].setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Prefs.setAdhanOn(activity, idx, !Prefs.adhanOn(activity, idx));
                    changed();
                }
            });
            rows.addView(row);
        }

        soundState = (TextView) view.findViewById(R.id.sound_state);
        pickBtn = (Button) view.findViewById(R.id.btn_pick_sound);
        testBtn = (Button) view.findViewById(R.id.btn_test_sound);
        defaultBtn = (Button) view.findViewById(R.id.btn_default_sound);
        quietBtn = (Button) view.findViewById(R.id.btn_quiet);
        remindBtn = (Button) view.findViewById(R.id.btn_remind);
        pickBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { pickSound(); }
        });
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (AdhanService.playing) {
                    AdhanService.stop(activity);
                } else if (Prefs.adhanUri(activity) != null) {
                    AdhanService.startTest(activity);
                }
                handlerRefresh();
            }
        });
        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Prefs.clearAdhanSound(activity);
                refresh();
            }
        });
        quietBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Prefs.setQuietWhenSilent(activity, !Prefs.quietWhenSilent(activity));
                refresh();
            }
        });
        remindBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Prefs.setRemindBefore(activity, SettingsActivity.nextChoice(Prefs.REMIND_CHOICES,
                        Prefs.remindBefore(activity)));
                changed();
            }
        });
        view.findViewById(R.id.btn_more_settings).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                activity.startActivity(new Intent(activity, SettingsActivity.class));
            }
        });

        LinearLayout perms = (LinearLayout) view.findViewById(R.id.perm_rows);
        String[] names = {"Bildirim izni", "Tam zamanında alarm", "Pil tasarrufu",
                "Alarm ses seviyesi", "Ezan ses dosyası"};
        for (int i = 0; i < permRows.length; i++) {
            final int which = i;
            View row = inf.inflate(R.layout.row_perm, perms, false);
            ((TextView) row.findViewById(R.id.perm_name)).setText(names[i]);
            row.findViewById(R.id.perm_button).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { fixPermission(which); }
            });
            permRows[i] = row;
            perms.addView(row);
        }
    }

    @Override
    void onShow() { refresh(); }

    @Override
    void onTick(long now) { updateTestButton(); }

    private void handlerRefresh() {
        // Servis birkaç milisaniye içinde başlar/durur; düğme bir sonraki saniyede güncellenir.
        updateTestButton();
    }

    private void changed() {
        refresh();
        AlarmScheduler.schedule(activity);
        NextPrayerWidget.updateAll(activity);
    }

    void refresh() {
        for (int k = 0; k < PRAYERS.length; k++) {
            boolean on = Prefs.adhanOn(activity, PRAYERS[k]);
            toggles[k].setText(on ? "AÇIK ✔" : "KAPALI");
            toggles[k].setBackgroundResource(on ? R.drawable.button : R.drawable.button_off);
        }
        String uri = Prefs.adhanUri(activity);
        if (uri == null) {
            soundState.setText("Şu an: vakit girince telefonun bildirim sesi çalar.");
        } else {
            String n = Prefs.adhanName(activity);
            soundState.setText("Şu an: seçtiğiniz ezan çalar" + (n.length() > 0 ? " (" + n + ")" : "")
                    + ".");
        }
        pickBtn.setText(uri == null ? "Ezan Ses Dosyası Seç" : "Başka Ezan Dosyası Seç");
        testBtn.setVisibility(uri == null ? View.GONE : View.VISIBLE);
        defaultBtn.setVisibility(uri == null ? View.GONE : View.VISIBLE);
        quietBtn.setVisibility(uri == null ? View.GONE : View.VISIBLE);
        quietBtn.setText("Telefon sessizdeyken ezan: " + (Prefs.quietWhenSilent(activity)
                ? "OKUNMAZ" : "OKUNUR"));
        int r = Prefs.remindBefore(activity);
        remindBtn.setText("Vakitten önce hatırlat: " + (r == 0 ? "KAPALI" : r + " dakika"));
        updateTestButton();
        refreshPermissions();
    }

    private void updateTestButton() {
        testBtn.setText(AdhanService.playing ? activity.getString(R.string.stop_sound)
                : activity.getString(R.string.test_sound));
    }

    // ---- İzinler ----

    static boolean notificationsAllowed(Context c) {
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(MainActivity.NOTIF_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm == null || nm.areNotificationsEnabled();
    }

    static boolean exactAlarmsAllowed(Context c) {
        if (Build.VERSION.SDK_INT < 31) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am == null || am.canScheduleExactAlarms();
    }

    static boolean batteryUnrestricted(Context c) {
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(c.getPackageName());
    }

    static boolean alarmVolumeOn(Context c) {
        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        return am == null || am.getStreamVolume(AudioManager.STREAM_ALARM) > 0;
    }

    static boolean soundFileReadable(Context c) {
        String uri = Prefs.adhanUri(c);
        if (uri == null) return true;
        for (UriPermission p : c.getContentResolver().getPersistedUriPermissions()) {
            if (p.getUri().toString().equals(uri) && p.isReadPermission()) return true;
        }
        return false;
    }

    private void refreshPermissions() {
        boolean hasFile = Prefs.adhanUri(activity) != null;
        setPerm(P_NOTIF, notificationsAllowed(activity), "Verildi", "Verilmedi",
                "Vakit girdiğinde bildirim ve ezan gösterebilmek için gerekir.", "İzin Ver");
        setPerm(P_EXACT, exactAlarmsAllowed(activity), "Verildi", "Verilmedi",
                "Ezanın tam vaktinde (dakikası dakikasına) okunması için gerekir.", "İzin Ver");
        setPerm(P_BATTERY, batteryUnrestricted(activity), "Kısıtlama yok",
                "Kısıtlı (ezan gecikebilir)",
                "Pil tasarrufu ezanı geciktirebilir; bazı telefonlarda seçtiğiniz ezan sesi "
                        + "yerine yalnızca bildirim sesi çalabilir. Açılan listede “Kolay Namaz "
                        + "Vakti”ni bulup kısıtlamayı kaldırın.",
                "Pil Ayarlarını Aç");
        permRows[P_VOLUME].setVisibility(hasFile ? View.VISIBLE : View.GONE);
        setPerm(P_VOLUME, alarmVolumeOn(activity), "Açık", "Kapalı (ezan duyulmaz)",
                "Seçtiğiniz ezan, telefonun ALARM ses seviyesinde çalar.", "Ses Ayarlarını Aç");
        permRows[P_FILE].setVisibility(hasFile ? View.VISIBLE : View.GONE);
        setPerm(P_FILE, soundFileReadable(activity), "Okunabiliyor", "Dosyaya erişilemiyor",
                "Dosya silinmiş veya taşınmış olabilir. Dosyayı yeniden seçin.", "Dosyayı Yeniden Seç");
    }

    private void setPerm(int i, boolean ok, String okText, String badText, String why,
            String action) {
        View row = permRows[i];
        TextView state = (TextView) row.findViewById(R.id.perm_state);
        state.setText(ok ? "✔ " + okText : "✖ " + badText);
        state.setTextColor(activity.getColor(ok ? R.color.accent : R.color.warn));
        ((TextView) row.findViewById(R.id.perm_why)).setText(why);
        Button b = (Button) row.findViewById(R.id.perm_button);
        b.setText(action);
        b.setVisibility(ok ? View.GONE : View.VISIBLE);
    }

    void fixPermission(int which) {
        switch (which) {
            case P_NOTIF:
                if (Build.VERSION.SDK_INT >= 33 && !Prefs.sp(activity).getBoolean("askedNotif", false)
                        && activity.checkSelfPermission(MainActivity.NOTIF_PERMISSION)
                                != PackageManager.PERMISSION_GRANTED) {
                    Prefs.sp(activity).edit().putBoolean("askedNotif", true).apply();
                    activity.askNotificationPermission();
                } else {
                    open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName()));
                }
                break;
            case P_EXACT:
                if (Build.VERSION.SDK_INT >= 31) {
                    open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + activity.getPackageName())));
                }
                break;
            case P_BATTERY:
                open(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                break;
            case P_VOLUME:
                open(new Intent(Settings.ACTION_SOUND_SETTINGS));
                break;
            default:
                pickSound();
        }
    }

    private void open(Intent i) {
        try {
            activity.startActivity(i);
        } catch (Exception e) {
            activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    // ---- Ezan ses dosyası seçimi ----

    void pickSound() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("audio/*");
        try {
            activity.startActivityForResult(i, REQ_SOUND);
        } catch (Exception e) {
            soundState.setText("Bu telefonda dosya seçici açılamadı.");
        }
    }

    @Override
    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_SOUND || resultCode != Activity.RESULT_OK || data == null
                || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            activity.getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // Kalıcı izin verilmediyse dosya yine de bu oturumda çalınabilir; durum ekranda görünür.
        }
        Prefs.setAdhanSound(activity, uri.toString(), displayName(uri));
        refresh();
    }

    private String displayName(Uri uri) {
        Cursor c = null;
        try {
            c = activity.getContentResolver().query(uri,
                    new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {
            // ad okunamazsa boş bırakılır
        } finally {
            if (c != null) c.close();
        }
        return "";
    }
}
