package tr.kolay.namazvakti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Alarm çalınca: bildirim gösterir, widget'ı yeniler, sonraki alarmı kurar. */
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

        if (fresh && idx >= 0 && idx != PrayerCalc.GUNES) {
            String name = Times.NAMES[idx];
            String hm = Times.hhmm(c, time + (type == AlarmScheduler.TYPE_REMINDER
                    ? Prefs.remindBefore(c) * 60000L : 0));
            if (type == AlarmScheduler.TYPE_PRAYER && Prefs.notifyOn(c)) {
                Notifier.show(c, name + " vakti girdi", name + " namazı vakti: " + hm);
            } else if (type == AlarmScheduler.TYPE_REMINDER) {
                Notifier.show(c, name + " vaktine " + Prefs.remindBefore(c) + " dakika kaldı",
                        name + " vakti: " + hm);
            }
        }
        NextPrayerWidget.updateAll(c);
        AlarmScheduler.schedule(c);
    }
}
