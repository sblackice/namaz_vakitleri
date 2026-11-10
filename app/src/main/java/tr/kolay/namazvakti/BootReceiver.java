package tr.kolay.namazvakti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Telefon açılınca, uygulama güncellenince veya saat değişince alarmı yeniden kurar. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        // Otomatik konumda, telefonun saat dilimi değişince (yurt dışına çıkış gibi) vakitler de ona uyar.
        if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) && Prefs.autoLocation(c)
                && Prefs.hasLocation(c)) {
            String tz = java.util.TimeZone.getDefault().getID();
            Prefs.setLocation(c, AutoLocation.placeName(Prefs.lat(c), Prefs.lon(c), tz),
                    Prefs.lat(c), Prefs.lon(c), tz);
        }
        AlarmScheduler.schedule(c);
        NextPrayerWidget.updateAll(c);
    }
}
