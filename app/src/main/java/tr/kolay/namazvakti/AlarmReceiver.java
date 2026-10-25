package tr.kolay.namazvakti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Alarm çalınca: ezan sesini çalar veya bildirim gösterir, widget'ı yeniler, sonraki alarmı kurar. */
public class AlarmReceiver extends BroadcastReceiver {
    /** Bu kadar geç gelen alarm için bildirim gösterilmez (ör. telefon kapalıyken kaçan). */
    static final long LATE_LIMIT_MS = 10 * 60 * 1000L;

    @Override
    public void onReceive(Context c, Intent intent) {
        int idx = intent.getIntExtra(AlarmScheduler.EXTRA_INDEX, -1);
        int type = intent.getIntExtra(AlarmScheduler.EXTRA_TYPE, -1);
        long time = intent.getLongExtra(AlarmScheduler.EXTRA_TIME, 0);
        long now = Clock.now();
        boolean fresh = time > 0 && now - time < LATE_LIMIT_MS;

        if (fresh && idx >= 0 && idx != PrayerCalc.GUNES && Prefs.adhanOn(c, idx)) {
            String name = Times.NAMES[idx];
            String hm = Times.hhmm(c, time + (type == AlarmScheduler.TYPE_REMINDER
                    ? Prefs.remindBefore(c) * 60000L : 0));
            if (type == AlarmScheduler.TYPE_PRAYER) {
                String title = name + " vakti girdi";
                String text = idx == PrayerCalc.IMSAK
                        ? "Sabah namazının vakti başladı: " + hm
                        : name + " namazı vakti: " + hm;
                boolean played = Prefs.adhanUri(c) != null && AdhanService.canPlayNow(c)
                        && AdhanService.start(c, title, text);
                if (!played) Notifier.show(c, title, text);
            } else if (type == AlarmScheduler.TYPE_REMINDER) {
                Notifier.show(c, name + " vaktine " + Prefs.remindBefore(c) + " dakika kaldı",
                        name + " vakti: " + hm);
            }
        }
        NextPrayerWidget.updateAll(c);
        AlarmScheduler.schedule(c);
    }
}
