package tr.kolay.namazvakti;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Bir sonraki olayı (vakit girişi veya vakit öncesi hatırlatma) kurar.
 * Tek bir alarm kurulur; çaldığında AlarmReceiver bir sonrakini kurar.
 * Telefon yeniden açılınca BootReceiver alarmı yeniden kurar.
 */
final class AlarmScheduler {
    private AlarmScheduler() {}

    static final String ACTION = "tr.kolay.namazvakti.VAKIT";
    static final String EXTRA_INDEX = "index";
    static final String EXTRA_TYPE = "type";
    static final String EXTRA_TIME = "time";
    static final int TYPE_PRAYER = 0;
    static final int TYPE_REMINDER = 1;

    /** Bir sonraki olay: [zaman, vakit sırası, tür]. Konum yoksa null. */
    static long[] nextEvent(Context c, long now) {
        if (!Prefs.hasLocation(c)) return null;
        int remind = Prefs.remindBefore(c);
        long best = Long.MAX_VALUE;
        int bestIdx = -1, bestType = -1;
        for (int day = 0; day <= 1; day++) {
            long[] t = Times.forDay(c, now, day);
            for (int i = 0; i < t.length; i++) {
                // Vakit girişleri her zaman kurulur (widget'ı da günceller).
                if (t[i] > now && t[i] < best) {
                    best = t[i]; bestIdx = i; bestType = TYPE_PRAYER;
                }
                if (remind > 0 && i != PrayerCalc.GUNES) {
                    long r = t[i] - remind * 60000L;
                    if (r > now && r < best) {
                        best = r; bestIdx = i; bestType = TYPE_REMINDER;
                    }
                }
            }
        }
        if (bestIdx < 0) return null;
        return new long[] {best, bestIdx, bestType};
    }

    static PendingIntent pending(Context c, Intent extras) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(ACTION);
        if (extras != null) i.putExtras(extras);
        return PendingIntent.getBroadcast(c, 100, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        long[] ev = nextEvent(c, Clock.now());
        if (ev == null) {
            am.cancel(pending(c, null));
            return;
        }
        Intent extras = new Intent()
                .putExtra(EXTRA_TIME, ev[0])
                .putExtra(EXTRA_INDEX, (int) ev[1])
                .putExtra(EXTRA_TYPE, (int) ev[2]);
        PendingIntent pi = pending(c, extras);
        boolean exactAllowed = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
        try {
            if (exactAllowed) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ev[0], pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ev[0], pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ev[0], pi);
        }
    }
}
