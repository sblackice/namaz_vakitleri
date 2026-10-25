package tr.kolay.namazvakti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Telefon açılınca, uygulama güncellenince veya saat değişince alarmı yeniden kurar. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        AlarmScheduler.schedule(c);
        NextPrayerWidget.updateAll(c);
    }
}
