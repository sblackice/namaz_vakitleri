package tr.kolay.namazvakti;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Namaz vakitlerini telefonda, internet olmadan hesaplar.
 *
 * Güneşin konumu, "Astronomical Algorithms" (Jean Meeus) kitabındaki yaklaşık
 * formüllerle bulunur. Türkiye ayarları, açık kaynak "adhan" kütüphanesindeki
 * "Turkey" yöntemiyle aynıdır: imsak 18°, yatsı 17°, ikindi için gölge boyu 1 kat
 * (Hanefi değil, standart) ve şu dakika düzeltmeleri: güneş -7, öğle +5,
 * ikindi +4, akşam +7.
 *
 * Bu sınıf Android'e bağlı değildir; bu yüzden bilgisayarda da test edilebilir.
 */
public final class PrayerCalc {
    private PrayerCalc() {}

    public static final int IMSAK = 0;
    public static final int GUNES = 1;
    public static final int OGLE = 2;
    public static final int IKINDI = 3;
    public static final int AKSAM = 4;
    public static final int YATSI = 5;
    public static final int COUNT = 6;

    static final double FAJR_ANGLE = 18.0;
    static final double ISHA_ANGLE = 17.0;
    static final double SUNRISE_ANGLE = 0.833;
    static final int[] METHOD_ADJUST_MIN = {0, -7, 5, 4, 7, 0};

    static final double KAABA_LAT = 21.4225241;
    static final double KAABA_LON = 39.8261818;

    /**
     * Verilen gün için 6 vakti hesaplar.
     *
     * @param year yıl, @param month ay (1-12), @param day gün (yerel takvim günü)
     * @param userOffsetMin kullanıcının her vakit için eklediği dakika (null olabilir)
     * @return her vaktin zamanı (epoch milisaniye, dakikaya yuvarlanmış)
     */
    public static long[] compute(double lat, double lon, int year, int month, int day,
                                 int[] userOffsetMin) {
        double jd = julian(year, month, day) - lon / (15.0 * 24.0);

        // Saat cinsinden yerel güneş zamanı. Başlangıç tahminleri, sonra iyileştirme.
        double[] t = {5, 6, 12, 13, 18, 18};
        for (int iter = 0; iter < 3; iter++) {
            double[] p = new double[COUNT];
            for (int i = 0; i < COUNT; i++) p[i] = t[i] / 24.0;
            t[IMSAK] = sunAngleTime(jd, FAJR_ANGLE, p[IMSAK], true, lat);
            t[GUNES] = sunAngleTime(jd, SUNRISE_ANGLE, p[GUNES], true, lat);
            t[OGLE] = midDay(jd, p[OGLE]);
            t[IKINDI] = asrTime(jd, 1, p[IKINDI], lat);
            t[AKSAM] = sunAngleTime(jd, SUNRISE_ANGLE, p[AKSAM], false, lat);
            t[YATSI] = sunAngleTime(jd, ISHA_ANGLE, p[YATSI], false, lat);
            if (Double.isNaN(t[GUNES]) || Double.isNaN(t[AKSAM])) break;
        }

        // Yüksek enlem güvencesi ("gecenin yarısı" kuralı): imsak/yatsı hesaplanamazsa
        // veya geceden uzun çıkarsa gecenin yarısı ile sınırlanır.
        if (!Double.isNaN(t[GUNES]) && !Double.isNaN(t[AKSAM])) {
            double night = 24.0 - (t[AKSAM] - t[GUNES]);
            double half = night / 2.0;
            if (Double.isNaN(t[IMSAK]) || t[GUNES] - t[IMSAK] > half) t[IMSAK] = t[GUNES] - half;
            if (Double.isNaN(t[YATSI]) || t[YATSI] - t[AKSAM] > half) t[YATSI] = t[AKSAM] + half;
        }

        long utcMidnight = utcMidnightMillis(year, month, day);
        long[] out = new long[COUNT];
        for (int i = 0; i < COUNT; i++) {
            double utHours = t[i] - lon / 15.0;
            double ms = utcMidnight + utHours * 3600000.0;
            int adj = METHOD_ADJUST_MIN[i] + (userOffsetMin == null ? 0 : userOffsetMin[i]);
            ms += adj * 60000.0;
            out[i] = Math.round(ms / 60000.0) * 60000L;
        }
        return out;
    }

    /** Kıble yönü: kuzeyden saat yönünde derece (0-360). */
    public static double qibla(double lat, double lon) {
        double phi = Math.toRadians(lat);
        double phiK = Math.toRadians(KAABA_LAT);
        double dLon = Math.toRadians(KAABA_LON - lon);
        double y = Math.sin(dLon);
        double x = Math.cos(phi) * Math.tan(phiK) - Math.sin(phi) * Math.cos(dLon);
        double deg = Math.toDegrees(Math.atan2(y, x));
        return (deg + 360.0) % 360.0;
    }

    /** O günün yerel takvim tarihine göre gün başı (UTC gece yarısı). */
    static long utcMidnightMillis(int year, int month, int day) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(year, month - 1, day, 0, 0, 0);
        return c.getTimeInMillis();
    }

    // ---- Astronomi yardımcıları ----

    static double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double a = Math.floor(year / 100.0);
        double b = 2 - a + Math.floor(a / 4.0);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5;
    }

    /** [0] = deklinasyon (derece), [1] = zaman denklemi (saat). */
    static double[] sunPosition(double jd) {
        double d = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * d);
        double q = fixAngle(280.459 + 0.98564736 * d);
        double l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0;
        double eqt = q / 15.0 - fixHour(ra);
        double decl = darcsin(dsin(e) * dsin(l));
        return new double[] {decl, eqt};
    }

    static double midDay(double jd, double portion) {
        double eqt = sunPosition(jd + portion)[1];
        return fixHour(12 - eqt);
    }

    static double sunAngleTime(double jd, double angle, double portion, boolean beforeNoon, double lat) {
        double decl = sunPosition(jd + portion)[0];
        double noon = midDay(jd, portion);
        double cosT = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat));
        if (cosT > 1 || cosT < -1) return Double.NaN; // güneş o açıya hiç ulaşmıyor
        double t = darccos(cosT) / 15.0;
        return noon + (beforeNoon ? -t : t);
    }

    static double asrTime(double jd, double shadowFactor, double portion, double lat) {
        double decl = sunPosition(jd + portion)[0];
        double angle = -darccot(shadowFactor + dtan(Math.abs(lat - decl)));
        return sunAngleTime(jd, angle, portion, false, lat);
    }

    static double dsin(double d) { return Math.sin(Math.toRadians(d)); }
    static double dcos(double d) { return Math.cos(Math.toRadians(d)); }
    static double dtan(double d) { return Math.tan(Math.toRadians(d)); }
    static double darcsin(double x) { return Math.toDegrees(Math.asin(x)); }
    static double darccos(double x) { return Math.toDegrees(Math.acos(x)); }
    static double darctan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    static double darccot(double x) { return Math.toDegrees(Math.atan(1.0 / x)); }

    static double fixAngle(double a) {
        a = a - 360.0 * Math.floor(a / 360.0);
        return a < 0 ? a + 360.0 : a;
    }

    static double fixHour(double h) {
        h = h - 24.0 * Math.floor(h / 24.0);
        return h < 0 ? h + 24.0 : h;
    }
}
