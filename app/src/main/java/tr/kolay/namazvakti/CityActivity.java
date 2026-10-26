package tr.kolay.namazvakti;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Şehir seçimi: önce 81 il, bir ile dokununca o ilin ilçeleri (vakitler ilçenin kendi
 * konumuna göre hesaplanır). Arama kutusu hem il hem ilçe bulur. Otomatik konum da vardır.
 */
public class CityActivity extends Activity {
    private static final int REQ_LOCATION = 7;
    private static final long LOCATION_TIMEOUT_MS = 30000;

    /** Listedeki satırlar: il sırası ve ilçe sırası (il merkezi için -1). */
    private final List<String> shown = new ArrayList<String>();
    private final List<int[]> shownRefs = new ArrayList<int[]>();
    private int openIl = -1; // ilçeleri gösterilen il (-1: il listesi)
    private EditText search;
    private ArrayAdapter<String> adapter;
    private TextView status;
    private LocationManager lm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean waiting;

    private final LocationListener listener = new LocationListener() {
        @Override public void onLocationChanged(Location l) { useLocation(l); }
        @Override public void onStatusChanged(String p, int s, Bundle b) {}
        @Override public void onProviderEnabled(String p) {}
        @Override public void onProviderDisabled(String p) {}
    };

    private final Runnable timeout = new Runnable() {
        @Override public void run() {
            stopUpdates();
            status.setText("Konum bulunamadı. Lütfen listeden şehrinizi seçin.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tema.uygula(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        Ui.header(findViewById(R.id.city_page), "Kolay Namaz Vakti", getString(R.string.city_title));
        status = (TextView) findViewById(R.id.city_status);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ListView list = (ListView) findViewById(R.id.city_list);
        adapter = new ArrayAdapter<String>(this, R.layout.item_city, R.id.item_city_name, shown);
        list.setAdapter(adapter);
        filter("");
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                tap(pos);
            }
        });

        search = (EditText) findViewById(R.id.city_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        findViewById(R.id.btn_find_me).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { findMe(); }
        });
        findViewById(R.id.btn_city_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { back(); }
        });
        Ui.scaleText(findViewById(R.id.city_root), Prefs.textScale(this));
    }

    /** Türkçe harf farkı gözetmeden arar: "izmir" yazınca "İzmir" bulunur. */
    static String simplify(String s) {
        String t = s.replace('İ', 'i').replace('I', 'ı').toLowerCase(Times.TR)
                .replace('ı', 'i');
        t = Normalizer.normalize(t, Normalizer.Form.NFD);
        return t.replaceAll("\\p{M}", "").trim();
    }

    static final String ARROW = "  ›";

    /** Liste: arama boşsa iller (veya açık ilin ilçeleri); doluysa eşleşen il ve ilçeler. */
    void filter(String q) {
        String sq = simplify(q);
        shown.clear();
        shownRefs.clear();
        if (sq.length() == 0 && openIl >= 0) {
            String il = Cities.NAMES[openIl];
            add(il + " (il merkezi)", openIl, -1);
            for (int k = 0; k < Ilceler.AD.length; k++) {
                if (Ilceler.IL[k] == openIl && !Ilceler.AD[k].equals("Merkez")) add(Ilceler.AD[k], openIl, k);
            }
        } else {
            for (int i = 0; i < Cities.NAMES.length; i++) {
                if (sq.length() == 0 || simplify(Cities.NAMES[i]).contains(sq)) add(Cities.NAMES[i] + ARROW, i, -1);
            }
            if (sq.length() >= 2) {
                for (int k = 0; k < Ilceler.AD.length; k++) {
                    String n = Ilceler.AD[k];
                    if (!n.equals("Merkez") && simplify(n).contains(sq)) {
                        add(n + " – " + Cities.NAMES[Ilceler.IL[k]], Ilceler.IL[k], k);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        String title = openIl >= 0 && sq.length() == 0 ? Cities.NAMES[openIl] + " ilçeleri"
                : getString(R.string.city_title);
        Ui.header(findViewById(R.id.city_page), "Kolay Namaz Vakti", title);
    }

    private void add(String text, int il, int ilce) {
        shown.add(text);
        shownRefs.add(new int[] {il, ilce});
    }

    /** Listeden bir satır seçildi. */
    void tap(int pos) {
        int[] r = shownRefs.get(pos);
        boolean listingIlce = openIl >= 0 && search.getText().length() == 0;
        if (r[1] < 0 && !listingIlce) {
            // İle dokunuldu: ilçeleri aç.
            openIl = r[0];
            search.setText("");
            filter("");
            ((ListView) findViewById(R.id.city_list)).setSelection(0);
            return;
        }
        if (r[1] < 0) {
            chooseIl(r[0]);
        } else {
            chooseIlce(r[1]);
        }
    }

    void back() {
        if (openIl >= 0) {
            openIl = -1;
            search.setText("");
            filter("");
        } else {
            finish();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() { back(); }

    void choose(String name) {
        int i = Cities.indexOf(name);
        if (i >= 0) chooseIl(i);
    }

    void chooseIl(int i) {
        Prefs.setLocation(this, Cities.NAMES[i], Cities.LAT[i], Cities.LON[i], Prefs.DEFAULT_TZ);
        afterChange();
    }

    void chooseIlce(int k) {
        Prefs.setLocation(this, Ilceler.AD[k] + ", " + Cities.NAMES[Ilceler.IL[k]],
                Ilceler.LAT[k], Ilceler.LON[k], Prefs.DEFAULT_TZ);
        afterChange();
    }

    /** Bir noktaya en yakın ilçe ("Merkez" ilçeleri hariç; onlar il merkezi sayılır). */
    static int nearestIlce(double lat, double lon, double[] distanceKmOut) {
        int best = -1;
        double bd = Double.MAX_VALUE;
        for (int k = 0; k < Ilceler.AD.length; k++) {
            double d = Cities.distanceKm(lat, lon, Ilceler.LAT[k], Ilceler.LON[k]);
            if (d < bd) {
                bd = d;
                best = k;
            }
        }
        if (distanceKmOut != null) distanceKmOut[0] = bd;
        return best;
    }

    private void afterChange() {
        AlarmScheduler.schedule(this);
        NextPrayerWidget.updateAll(this);
        finish();
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    void findMe() {
        if (!hasLocationPermission()) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }
        status.setText("Konumunuz aranıyor, lütfen bekleyin…");
        Location best = null;
        try {
            for (String p : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
        } catch (SecurityException ignored) {
        }
        if (best != null && Clock.now() - best.getTime() < 6 * 3600 * 1000L) {
            useLocation(best);
            return;
        }
        boolean started = false;
        try {
            for (String p : lm.getProviders(true)) {
                try {
                    lm.requestLocationUpdates(p, 0, 0, listener, Looper.getMainLooper());
                    started = true;
                } catch (SecurityException | IllegalArgumentException ignored) {
                }
            }
        } catch (SecurityException ignored) {
        }
        if (!started) {
            status.setText("Telefonunuzun konum özelliği kapalı. Açın ya da listeden şehrinizi seçin.");
            return;
        }
        waiting = true;
        handler.postDelayed(timeout, LOCATION_TIMEOUT_MS);
    }

    void useLocation(Location l) {
        stopUpdates();
        double[] dist = new double[1];
        int k = nearestIlce(l.getLatitude(), l.getLongitude(), dist);
        String name;
        if (k >= 0 && dist[0] <= 30) {
            String il = Cities.NAMES[Ilceler.IL[k]];
            String ilce = Ilceler.AD[k];
            boolean merkez = ilce.equals("Merkez") || Ilceler.LAT[k] == Cities.LAT[Ilceler.IL[k]]
                    && Ilceler.LON[k] == Cities.LON[Ilceler.IL[k]];
            name = (merkez ? il : ilce + ", " + il) + " (konumum)";
        } else {
            int i = Cities.nearest(l.getLatitude(), l.getLongitude(), dist);
            name = dist[0] <= 60 ? Cities.NAMES[i] + " (konumum)" : "Konumum";
        }
        Prefs.setLocation(this, name, l.getLatitude(), l.getLongitude(),
                TimeZone.getDefault().getID());
        afterChange();
    }

    private void stopUpdates() {
        handler.removeCallbacks(timeout);
        if (waiting) {
            try {
                lm.removeUpdates(listener);
            } catch (SecurityException ignored) {
            }
            waiting = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        if (code != REQ_LOCATION) return;
        if (hasLocationPermission()) {
            findMe();
        } else {
            status.setText("Konum izni verilmedi. Lütfen listeden şehrinizi seçin.");
        }
    }

    @Override
    protected void onDestroy() {
        stopUpdates();
        super.onDestroy();
    }
}
