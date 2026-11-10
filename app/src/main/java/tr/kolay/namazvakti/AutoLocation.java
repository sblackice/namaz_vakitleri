package tr.kolay.namazvakti;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.util.TimeZone;

/**
 * Otomatik konum: kullanıcı "Konumumu Otomatik Bul ve Takip Et" dediyse, uygulama her açıldığında
 * (öne geldiğinde) telefonun konumuna bakar; başka bir yere gidilmişse vakitleri oraya göre günceller.
 *
 * Konum yalnızca uygulama açıkken okunur (arka planda konum izni istenmez) ve telefondan dışarı
 * gönderilmez. Listeden elle şehir seçilirse otomatik konum kapanır.
 */
final class AutoLocation {
    interface Listener {
        /** Konum değişti ve kaydedildi. */
        void onPlaceChanged(String placeName);
    }

    /** Bu kadar yer değiştirmeden vakitler yeniden hesaplanmaz (vakit farkı 1 dakikanın altında kalır). */
    static final double MOVE_KM = 5.0;
    /** İl merkezine bu kadar yakınsa o ilin adı gösterilir; değilse "Konumum" yazar. */
    static final double NAME_KM = 100.0;
    /** Bu kadar yeni bir "son bilinen konum" varsa yeniden aranmaz. */
    static final long FRESH_MS = 15 * 60 * 1000L;
    /** Yeni konum gelmezse en fazla bu kadar eski bir konum kullanılır. */
    static final long STALE_MS = 6 * 3600 * 1000L;
    static final long TIMEOUT_MS = 25000;
    static final String OWN_PLACE = "Konumum";

    private final Context context;
    private final Listener listener;
    private final LocationManager lm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean waiting;
    private Location fallback;

    private final LocationListener locationListener = new LocationListener() {
        @Override public void onLocationChanged(Location l) { stop(); use(l); }
        @Override public void onStatusChanged(String p, int s, Bundle b) {}
        @Override public void onProviderEnabled(String p) {}
        @Override public void onProviderDisabled(String p) {}
    };

    private final Runnable timeout = new Runnable() {
        @Override public void run() {
            Location f = fallback;
            stop();
            if (f != null) use(f);
        }
    };

    AutoLocation(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Telefonun konum özelliği açık mı (en az bir konum kaynağı var mı)? */
    static boolean locationOn(Context c) {
        LocationManager m = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (m == null) return false;
        try {
            for (String p : m.getProviders(true)) {
                if (!LocationManager.PASSIVE_PROVIDER.equals(p)) return true;
            }
        } catch (RuntimeException ignored) {
        }
        return false;
    }

    /** En yeni "son bilinen konum" (yoksa null). */
    static Location lastKnown(Context c) {
        LocationManager m = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (m == null) return null;
        Location best = null;
        try {
            for (String p : m.getProviders(true)) {
                Location l = m.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
        } catch (RuntimeException ignored) {
        }
        return best;
    }

    /** Gösterilecek yer adı: en yakın il merkezi (Türkiye saat dilimindeyse ve yeterince yakınsa). */
    static String placeName(double lat, double lon, String tzId) {
        double[] dist = new double[1];
        int i = Cities.nearest(lat, lon, dist);
        return turkeyTime(tzId) && dist[0] <= NAME_KM ? Cities.NAMES[i] : OWN_PLACE;
    }

    /** Telefon Türkiye saat diliminde mi? (Aynı dilimin eski adları da kabul edilir.) */
    static boolean turkeyTime(String tzId) {
        return Prefs.DEFAULT_TZ.equals(tzId) || "Asia/Istanbul".equals(tzId) || "Turkey".equals(tzId);
    }

    /**
     * Konumu kaydeder, otomatik konumu açar, alarmları ve ana ekran bileşenini yeniler.
     * Kayıtlı konumla hemen hemen aynıysa hiçbir şey yapmaz ve false döner.
     */
    static boolean apply(Context c, double lat, double lon) {
        String tz = TimeZone.getDefault().getID();
        String name = placeName(lat, lon, tz);
        if (Prefs.hasLocation(c) && Prefs.autoLocation(c) && tz.equals(Prefs.tzId(c))
                && name.equals(Prefs.placeName(c))
                && Cities.distanceKm(lat, lon, Prefs.lat(c), Prefs.lon(c)) < MOVE_KM) {
            return false;
        }
        Prefs.setLocation(c, name, lat, lon, tz);
        Prefs.setAutoLocation(c, true);
        AlarmScheduler.schedule(c);
        NextPrayerWidget.updateAll(c);
        return true;
    }

    /** Uygulama öne gelince çağrılır. */
    void start() {
        stop();
        if (lm == null || !Prefs.autoLocation(context) || !hasPermission(context)) return;
        Location last = lastKnown(context);
        long now = Clock.now();
        if (last != null && now - last.getTime() < FRESH_MS) {
            use(last);
            return;
        }
        fallback = last != null && now - last.getTime() < STALE_MS ? last : null;
        boolean started = false;
        try {
            for (String p : lm.getProviders(true)) {
                try {
                    lm.requestLocationUpdates(p, 0, 0, locationListener, Looper.getMainLooper());
                    started = true;
                } catch (RuntimeException ignored) {
                }
            }
        } catch (RuntimeException ignored) {
        }
        if (!started) {
            Location f = fallback;
            fallback = null;
            if (f != null) use(f);
            return;
        }
        waiting = true;
        handler.postDelayed(timeout, TIMEOUT_MS);
    }

    /** Uygulama arka plana gidince çağrılır: konum dinlemeyi bırakır. */
    void stop() {
        handler.removeCallbacks(timeout);
        fallback = null;
        if (waiting) {
            waiting = false;
            try {
                lm.removeUpdates(locationListener);
            } catch (RuntimeException ignored) {
            }
        }
    }

    boolean isWaiting() { return waiting; }

    private void use(Location l) {
        if (apply(context, l.getLatitude(), l.getLongitude()) && listener != null) {
            listener.onPlaceChanged(Prefs.placeName(context));
        }
    }
}
