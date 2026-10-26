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

    /** Bu vakit girince ezan/bildirim olsun mu? Güneş bir namaz vakti değildir, varsayılanı kapalı. */
    static boolean adhanOn(Context c, int i) {
        return sp(c).getBoolean("adhan" + i, i != PrayerCalc.GUNES);
    }

    static void setAdhanOn(Context c, int i, boolean on) {
        sp(c).edit().putBoolean("adhan" + i, on).apply();
    }

    /** Kullanıcının seçtiği ezan ses dosyası (yoksa null: telefonun bildirim sesi çalar). */
    static String adhanUri(Context c) { return sp(c).getString("adhanUri", null); }

    static String adhanName(Context c) { return sp(c).getString("adhanName", ""); }

    static void setAdhanSound(Context c, String uri, String name) {
        sp(c).edit().putString("adhanUri", uri).putString("adhanName", name).apply();
    }

    static void clearAdhanSound(Context c) {
        sp(c).edit().remove("adhanUri").remove("adhanName").apply();
    }

    /** Telefon sessiz veya titreşimdeyken ezan sesi çalınmasın mı? */
    static boolean quietWhenSilent(Context c) { return sp(c).getBoolean("quietSilent", true); }

    static void setQuietWhenSilent(Context c, boolean on) {
        sp(c).edit().putBoolean("quietSilent", on).apply();
    }

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

    /** Renk teması (Tema.ADLAR sırası). */
    static int theme(Context c) { return sp(c).getInt("tema", 0); }

    static void setTheme(Context c, int i) { sp(c).edit().putInt("tema", i).apply(); }

    // ---- Zikirmatik ----
    static final int[] ZIKIR_HEDEFLERI = {33, 99, 100, 500, 1000};

    static int zikirCount(Context c) { return sp(c).getInt("zikir", 0); }

    static void setZikirCount(Context c, int n) {
        sp(c).edit().putInt("zikir", Math.max(0, Math.min(999999, n))).apply();
    }

    static int zikirTarget(Context c) { return sp(c).getInt("zikirHedef", 33); }

    static void setZikirTarget(Context c, int n) {
        sp(c).edit().putInt("zikirHedef", Math.max(1, Math.min(99999, n))).apply();
    }

    static boolean askedFirstRun(Context c) { return sp(c).getBoolean("firstRun", false); }

    static void setAskedFirstRun(Context c) { sp(c).edit().putBoolean("firstRun", true).apply(); }
}
