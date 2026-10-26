package tr.kolay.namazvakti;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * En Yakın Cami: bulunduğunuz yerin çevresindeki camileri haritada ve listede gösterir.
 * Bir camiye dokununca "Yol Tarifi" ile Google Haritalar açılır ve yolu tarif eder.
 *
 * Veri: OpenStreetMap (amenity=place_of_worship + religion=muslim), Overpass API ile.
 * Harita: Leaflet 1.9.4 (assets/harita, BSD-2) ve OpenStreetMap harita karoları.
 * Bu sayfa uygulamanın internet kullanan tek bölümüdür.
 */
final class CamiPage extends Page {
    static final int REQ_LOCATION = 51;
    static final String USER_AGENT = "KolayNamazVakti/2.2 (Android; github.com/sblackice/namaz_vakitleri)";
    static final String[] OVERPASS = {
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
    };
    private static final long LOCATION_TIMEOUT_MS = 20000;
    private static final int MAX_ON_MAP = 40;
    private static final int MAX_IN_LIST = 10;

    /** Bir cami. */
    static final class Cami {
        final String ad;
        final double lat, lon;
        final int metre;

        Cami(String ad, double lat, double lon, int metre) {
            this.ad = ad;
            this.lat = lat;
            this.lon = lon;
            this.metre = metre;
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView status;
    private final LinearLayout list;
    private final Button searchInMaps;
    private WebView web;
    private boolean pageReady;
    private double lat = Double.NaN, lon = Double.NaN;
    private final List<Cami> found = new ArrayList<Cami>();
    private LocationManager lm;
    private boolean waitingLocation;
    private int generation; // eski aramaların sonuçlarını yok saymak için

    private final LocationListener listener = new LocationListener() {
        @Override public void onLocationChanged(Location l) {
            stopLocation();
            useLocation(l.getLatitude(), l.getLongitude(), true);
        }
        @Override public void onStatusChanged(String p, int s, Bundle b) {}
        @Override public void onProviderEnabled(String p) {}
        @Override public void onProviderDisabled(String p) {}
    };

    private final Runnable locationTimeout = new Runnable() {
        @Override public void run() {
            stopLocation();
            useSavedPlace("Konumunuz bulunamadı.");
        }
    };

    CamiPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_cami);
        Ui.header(view, "Menü", a.getString(R.string.menu_cami));
        Ui.headerAction(view, R.drawable.ic_pin, "Yenile", new View.OnClickListener() {
            @Override public void onClick(View v) { refresh(); }
        });
        status = (TextView) view.findViewById(R.id.cami_status);
        list = (LinearLayout) view.findViewById(R.id.cami_list);
        searchInMaps = (Button) view.findViewById(R.id.cami_google);
        searchInMaps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openMapsSearch(); }
        });
        lm = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);
        try {
            web = createWebView(a);
            ((ViewGroup) view.findViewById(R.id.cami_map_box)).addView(web);
        } catch (RuntimeException e) {
            // WebView yoksa (çok nadir) yalnızca liste gösterilir.
            web = null;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private WebView createWebView(Context c) {
        WebView w = new WebView(c);
        w.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(false);
        s.setAllowContentAccess(false);
        s.setUserAgentString(USER_AGENT); // OpenStreetMap kurallarına göre uygulama kendini tanıtır
        w.addJavascriptInterface(new Bridge(), "Android");
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                // Haritadaki bağlantılar (ör. OpenStreetMap telif sayfası) tarayıcıda açılır.
                openExternal(r.getUrl());
                return true;
            }
        });
        w.loadUrl("file:///android_asset/harita/harita.html");
        return w;
    }

    /** Harita sayfasından (JavaScript) gelen çağrılar. */
    final class Bridge {
        @JavascriptInterface
        public void hazir() {
            handler.post(new Runnable() {
                @Override public void run() {
                    pageReady = true;
                    pushToMap();
                }
            });
        }

        @JavascriptInterface
        public void yolTarifi(final int index) {
            handler.post(new Runnable() {
                @Override public void run() { directions(index); }
            });
        }
    }

    Bridge bridge() { return new Bridge(); }

    @Override
    void onShow() {
        if (Double.isNaN(lat)) refresh();
    }

    @Override
    void onHide() { stopLocation(); }

    @Override
    void onDestroy() {
        stopLocation();
        if (web != null) {
            web.destroy();
            web = null;
        }
    }

    @Override
    void onPermissionResult(int requestCode) {
        if (requestCode == REQ_LOCATION) refresh();
    }

    // ---- Konum ----

    private boolean hasLocationPermission() {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    void refresh() {
        stopLocation();
        if (!hasLocationPermission()) {
            if (!Prefs.sp(activity).getBoolean("askedLocCami", false)) {
                Prefs.sp(activity).edit().putBoolean("askedLocCami", true).apply();
                status.setText("En yakın camileri bulmak için konum izni gerekir.");
                activity.requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
                return;
            }
            useSavedPlace("Konum izni verilmedi.");
            return;
        }
        if (lm == null) {
            useSavedPlace("Telefonunuzda konum hizmeti bulunamadı.");
            return;
        }
        status.setText("Konumunuz aranıyor…");
        Location best = null;
        try {
            for (String p : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
        } catch (SecurityException ignored) {
            // izin yoksa aşağıda kayıtlı şehir kullanılır
        }
        if (best != null && Clock.now() - best.getTime() < 10 * 60 * 1000L) {
            useLocation(best.getLatitude(), best.getLongitude(), true);
            return;
        }
        boolean started = false;
        try {
            for (String p : lm.getProviders(true)) {
                try {
                    lm.requestLocationUpdates(p, 0, 0, listener, Looper.getMainLooper());
                    started = true;
                } catch (SecurityException | IllegalArgumentException ignored) {
                    // bu sağlayıcı kullanılamıyor
                }
            }
        } catch (SecurityException ignored) {
            // izin yok
        }
        if (!started) {
            if (best != null) {
                useLocation(best.getLatitude(), best.getLongitude(), true);
            } else {
                useSavedPlace("Telefonunuzun konum özelliği kapalı.");
            }
            return;
        }
        waitingLocation = true;
        handler.postDelayed(locationTimeout, LOCATION_TIMEOUT_MS);
    }

    private void stopLocation() {
        handler.removeCallbacks(locationTimeout);
        if (waitingLocation) {
            try {
                lm.removeUpdates(listener);
            } catch (SecurityException ignored) {
                // izin geri alınmış olabilir
            }
            waitingLocation = false;
        }
    }

    /** GPS yoksa seçili şehrin merkezi kullanılır. */
    private void useSavedPlace(String why) {
        if (Prefs.hasLocation(activity)) {
            status.setText(why + " " + Prefs.placeName(activity)
                    + " merkezine göre aranıyor (gerçek konumunuzdan uzak olabilir).");
            useLocation(Prefs.lat(activity), Prefs.lon(activity), false);
        } else {
            status.setText(why + " Önce Ana Sayfa'dan şehrinizi seçin veya konumu açın.");
        }
    }

    void useLocation(double la, double lo, boolean gps) {
        lat = la;
        lon = lo;
        if (gps) status.setText("Yakındaki camiler aranıyor…");
        pushToMap();
        search();
    }

    // ---- Cami arama (OpenStreetMap / Overpass) ----

    static String query(double lat, double lon, int radius) {
        String around = "(around:" + radius + "," + String.format(Locale.US, "%.6f,%.6f", lat, lon) + ")";
        return "[out:json][timeout:20];("
                + "nwr[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"]" + around + ";"
                + ");out center 120;";
    }

    private void search() {
        final int gen = ++generation;
        final double la = lat, lo = lon;
        new Thread(new Runnable() {
            @Override public void run() {
                List<Cami> res = null;
                String error = null;
                for (int radius : new int[] {3000, 10000}) {
                    try {
                        res = parse(fetch(query(la, lo, radius)), la, lo);
                        if (res.size() >= 3) break;
                    } catch (Exception e) {
                        error = e.getClass().getSimpleName();
                        res = null;
                        break;
                    }
                }
                final List<Cami> result = res;
                final String err = error;
                handler.post(new Runnable() {
                    @Override public void run() {
                        if (gen == generation) showResult(result, err);
                    }
                });
            }
        }, "cami-arama").start();
    }

    private static String fetch(String q) throws Exception {
        Exception last = null;
        for (String server : OVERPASS) {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(server).openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(25000);
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setRequestProperty("User-Agent", USER_AGENT);
                c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                OutputStream out = c.getOutputStream();
                out.write(("data=" + URLEncoder.encode(q, "UTF-8")).getBytes("UTF-8"));
                out.close();
                if (c.getResponseCode() != 200) throw new java.io.IOException("HTTP " + c.getResponseCode());
                InputStream in = c.getInputStream();
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) buf.write(b, 0, n);
                in.close();
                return buf.toString("UTF-8");
            } catch (Exception e) {
                last = e;
            } finally {
                if (c != null) c.disconnect();
            }
        }
        throw last;
    }

    /** Overpass cevabını mesafeye göre sıralı cami listesine çevirir. */
    static List<Cami> parse(String json, double lat, double lon) throws JSONException {
        List<Cami> out = new ArrayList<Cami>();
        JSONArray els = new JSONObject(json).optJSONArray("elements");
        if (els == null) return out;
        for (int i = 0; i < els.length(); i++) {
            JSONObject e = els.getJSONObject(i);
            double la, lo;
            if (e.has("lat")) {
                la = e.getDouble("lat");
                lo = e.getDouble("lon");
            } else if (e.has("center")) {
                la = e.getJSONObject("center").getDouble("lat");
                lo = e.getJSONObject("center").getDouble("lon");
            } else {
                continue;
            }
            JSONObject tags = e.optJSONObject("tags");
            String name = "";
            if (tags != null) {
                name = tags.optString("name:tr", "");
                if (name.length() == 0) name = tags.optString("name", "");
            }
            name = name.trim();
            if (name.length() == 0) name = "Cami (adı girilmemiş)";
            if (name.length() > 80) name = name.substring(0, 80);
            out.add(new Cami(name, la, lo, (int) Math.round(distance(lat, lon, la, lo))));
        }
        Collections.sort(out, new Comparator<Cami>() {
            @Override public int compare(Cami x, Cami y) { return Integer.compare(x.metre, y.metre); }
        });
        return out;
    }

    /** İki nokta arası kuş uçuşu mesafe (metre). */
    static double distance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000, p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2);
        double dp = p2 - p1, dl = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dp / 2) * Math.sin(dp / 2)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * r * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    static String distanceText(int m) {
        if (m < 1000) return Math.max(10, Math.round(m / 10f) * 10) + " m";
        return String.format(new Locale("tr"), "%.1f km", m / 1000f);
    }

    void showResult(List<Cami> res, String error) {
        found.clear();
        list.removeAllViews();
        if (res == null) {
            status.setText("Camiler yüklenemedi. İnternet bağlantınızı kontrol edip “Yenile”ye "
                    + "dokunun ya da aşağıdaki düğmeyle Google Haritalar'da arayın.");
            return;
        }
        int n = Math.min(MAX_ON_MAP, res.size());
        found.addAll(res.subList(0, n));
        if (found.isEmpty()) {
            status.setText("Yakınınızda kayıtlı cami bulunamadı. Google Haritalar'da arayabilirsiniz.");
        } else {
            status.setText("En yakın " + found.size() + " cami bulundu. Yol tarifi için birine dokunun.");
        }
        LayoutInflater inf = activity.getLayoutInflater();
        for (int i = 0; i < Math.min(MAX_IN_LIST, found.size()); i++) {
            final int idx = i;
            Cami c = found.get(i);
            View item = inf.inflate(R.layout.item_cami, list, false);
            ((TextView) item.findViewById(R.id.cami_ad)).setText((i + 1) + ". " + c.ad);
            ((TextView) item.findViewById(R.id.cami_mesafe)).setText(distanceText(c.metre) + " uzakta");
            item.setContentDescription(c.ad + ", " + distanceText(c.metre));
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showOnMap(idx); }
            });
            item.findViewById(R.id.cami_tarif).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { directions(idx); }
            });
            activity.scale((ViewGroup) item);
            list.addView(item);
        }
        pushToMap();
    }

    List<Cami> found() { return found; }

    // ---- Harita ----

    /** Haritada işaret rengi: temanın ana rengi çok açıksa (siyah-beyaz tema) koyu turkuaz. */
    private String markerColor() {
        int c = Ui.color(activity, R.attr.colAccent);
        double lum = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255;
        if (lum > 0.6) c = 0xFF0A7275;
        return String.format("#%06X", c & 0xFFFFFF);
    }

    private void pushToMap() {
        if (web == null || !pageReady || Double.isNaN(lat)) return;
        StringBuilder js = new StringBuilder();
        js.append("baslat(").append(String.format(Locale.US, "%.6f,%.6f", lat, lon))
                .append(",'").append(markerColor()).append("');");
        JSONArray arr = new JSONArray();
        try {
            for (Cami c : found) {
                arr.put(new JSONObject().put("a", c.ad).put("e", c.lat).put("b", c.lon).put("m", c.metre));
            }
        } catch (JSONException ignored) {
            // sayılar her zaman geçerlidir
        }
        js.append("camiler(").append(arr.toString()).append(");");
        web.evaluateJavascript(js.toString(), null);
    }

    void showOnMap(int idx) {
        if (web != null && pageReady) web.evaluateJavascript("goster(" + idx + ");", null);
    }

    /** Google Haritalar'da bu camiye yol tarifi açar. */
    void directions(int idx) {
        if (idx < 0 || idx >= found.size()) return;
        Cami c = found.get(idx);
        String mode = c.metre <= 2000 ? "walking" : "driving";
        Uri u = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                + String.format(Locale.US, "%.6f,%.6f", c.lat, c.lon) + "&travelmode=" + mode);
        openExternal(u);
    }

    /** Konumu bilinmese de Google Haritalar'da "cami" araması açar. */
    void openMapsSearch() {
        String q = Double.isNaN(lat) ? "geo:0,0?q=cami"
                : "geo:" + String.format(Locale.US, "%.6f,%.6f", lat, lon) + "?q=cami";
        openExternal(Uri.parse(q));
    }

    private void openExternal(Uri u) {
        Intent i = new Intent(Intent.ACTION_VIEW, u);
        try {
            activity.startActivity(i);
        } catch (RuntimeException e) {
            status.setText("Harita uygulaması açılamadı. Google Haritalar'ı yükleyin.");
        }
    }
}
