package tr.kolay.namazvakti;

import android.content.Context;
import android.content.SharedPreferences;

/** Kullanıcı ayarları (yalnızca telefonda saklanır, hiçbir yere gönderilmez). */
final class Prefs {
    private Prefs() {}

    static final String DEFAULT_TZ = "Europe/Istanbul";
    static final int[] REMIND_CHOICES = {0, 10, 15, 30, 45};
    static final float[] SCALE_CHOICES = {1.0f, 1.2f, 1.4f};

    static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("ayarlar", Context.MODE_PRIVATE);
    }

    static boolean hasLocation(Context c) { return sp(c).contains("lat"); }

    static String placeName(Context c) { return sp(c).getString("place", ""); }

    static double lat(Context c) { return Double.longBitsToDouble(sp(c).getLong("lat", 0)); }

    static double lon(Context c) { return Double.longBitsToDouble(sp(c).getLong("lon", 0)); }

    static String tzId(Context c) { return sp(c).getString("tz", DEFAULT_TZ); }

    static void setLocation(Context c, String name, double lat, double lon, String tzId) {
        sp(c).edit()
                .putString("place", name)
                .putLong("lat", Double.doubleToRawLongBits(lat))
                .putLong("lon", Double.doubleToRawLongBits(lon))
                .putString("tz", tzId)
                .apply();
    }

    static boolean notifyOn(Context c) { return sp(c).getBoolean("notify", true); }

    static void setNotifyOn(Context c, boolean on) { sp(c).edit().putBoolean("notify", on).apply(); }

    static int remindBefore(Context c) { return sp(c).getInt("remind", 0); }

    static void setRemindBefore(Context c, int min) { sp(c).edit().putInt("remind", min).apply(); }

    static float textScale(Context c) { return sp(c).getFloat("scale", 1.0f); }

    static void setTextScale(Context c, float s) { sp(c).edit().putFloat("scale", s).apply(); }

    static int[] offsets(Context c) {
        int[] o = new int[PrayerCalc.COUNT];
        for (int i = 0; i < o.length; i++) o[i] = sp(c).getInt("off" + i, 0);
        return o;
    }

    static void setOffset(Context c, int i, int min) {
        sp(c).edit().putInt("off" + i, Math.max(-30, Math.min(30, min))).apply();
    }

    static boolean askedFirstRun(Context c) { return sp(c).getBoolean("firstRun", false); }

    static void setAskedFirstRun(Context c) { sp(c).edit().putBoolean("firstRun", true).apply(); }
}
