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

/** Şehir seçimi: 81 il listesi, arama kutusu ve otomatik konum. */
public class CityActivity extends Activity {
    private static final int REQ_LOCATION = 7;
    private static final long LOCATION_TIMEOUT_MS = 30000;

    private final List<String> shown = new ArrayList<String>();
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
                choose(shown.get(pos));
            }
        });

        EditText search = (EditText) findViewById(R.id.city_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        findViewById(R.id.btn_find_me).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { findMe(); }
        });
        findViewById(R.id.btn_city_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
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

    void filter(String q) {
        String sq = simplify(q);
        shown.clear();
        for (String n : Cities.NAMES) {
            if (sq.length() == 0 || simplify(n).startsWith(sq) || simplify(n).contains(sq)) {
                shown.add(n);
            }
        }
        adapter.notifyDataSetChanged();
    }

    void choose(String name) {
        int i = Cities.indexOf(name);
        if (i < 0) return;
        Prefs.setLocation(this, name, Cities.LAT[i], Cities.LON[i], Prefs.DEFAULT_TZ);
        afterChange();
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
        int i = Cities.nearest(l.getLatitude(), l.getLongitude(), dist);
        String name = dist[0] <= 60 ? Cities.NAMES[i] + " (konumum)" : "Konumum";
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
