package tr.kolay.namazvakti;

import android.content.Context;
import android.icu.util.IslamicCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Kayıtlı konuma göre vakitleri ve ekranda gösterilecek metinleri hazırlar. */
final class Times {
    private Times() {}

    static final Locale TR = new Locale("tr", "TR");
    static final String[] NAMES = {"İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"};
    static final String[] HIJRI_MONTHS = {"Muharrem", "Safer", "Rebiülevvel", "Rebiülahir",
            "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban", "Ramazan", "Şevval",
            "Zilkade", "Zilhicce"};

    static TimeZone tz(Context c) { return TimeZone.getTimeZone(Prefs.tzId(c)); }

    /** Bugünden dayOffset gün sonrasının vakitleri. */
    static long[] forDay(Context c, long now, int dayOffset) {
        Calendar cal = Calendar.getInstance(tz(c));
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
        return PrayerCalc.compute(Prefs.lat(c), Prefs.lon(c), cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), Prefs.offsets(c));
    }

    /** Sıradaki vakit: index, zaman ve o günün tüm vakitleri. */
    static final class Next {
        int index;
        long time;
        long[] dayTimes;
        boolean tomorrow;
    }

    static Next next(Context c, long now) {
        long[] today = forDay(c, now, 0);
        Next n = new Next();
        for (int i = 0; i < today.length; i++) {
            if (today[i] > now) {
                n.index = i;
                n.time = today[i];
                n.dayTimes = today;
                return n;
            }
        }
        long[] tm = forDay(c, now, 1);
        n.index = PrayerCalc.IMSAK;
        n.time = tm[PrayerCalc.IMSAK];
        n.dayTimes = tm;
        n.tomorrow = true;
        return n;
    }

    static String hhmm(Context c, long t) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", TR);
        f.setTimeZone(tz(c));
        return f.format(new Date(t));
    }

    static String dateLine(Context c, long now) {
        SimpleDateFormat f = new SimpleDateFormat("d MMMM yyyy, EEEE", TR);
        f.setTimeZone(tz(c));
        return f.format(new Date(now));
    }

    /** Hicri tarih (Ümmü'l-Kurâ takvimi; Diyanet takviminden 1 gün farklı olabilir). */
    static String hijriLine(Context c, long now) {
        try {
            IslamicCalendar ic = new IslamicCalendar(
                    android.icu.util.TimeZone.getTimeZone(Prefs.tzId(c)));
            ic.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
            ic.setTimeInMillis(now);
            return ic.get(IslamicCalendar.DAY_OF_MONTH) + " "
                    + HIJRI_MONTHS[ic.get(IslamicCalendar.MONTH)] + " "
                    + ic.get(IslamicCalendar.YEAR) + " (Hicri)";
        } catch (Throwable e) {
            return "";
        }
    }

    static String upper(String s) { return s.toUpperCase(TR); }

    /** Geri sayım: "01:23:45" (saat:dakika:saniye). Saniye yukarı yuvarlanır. */
    static String hms(long ms) {
        long sec = ms <= 0 ? 0 : (ms + 999) / 1000;
        return String.format(Locale.US, "%02d:%02d:%02d", sec / 3600, (sec / 60) % 60, sec % 60);
    }

    /** "10 Aralık 2026, Perşembe" */
    static String longDate(int y, int m, int d) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), TR);
        cal.clear();
        cal.set(y, m - 1, d, 12, 0, 0);
        SimpleDateFormat f = new SimpleDateFormat("d MMMM yyyy, EEEE", TR);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(cal.getTime());
    }

    /** "2 saat 5 dakika kaldı" gibi okunaklı kalan süre. */
    static String remaining(long ms) {
        if (ms < 60000) return "1 dakikadan az kaldı";
        long min = (ms + 59999) / 60000;
        long h = min / 60;
        long m = min % 60;
        if (h == 0) return m + " dakika kaldı";
        if (m == 0) return h + " saat kaldı";
        return h + " saat " + m + " dakika kaldı";
    }
}
