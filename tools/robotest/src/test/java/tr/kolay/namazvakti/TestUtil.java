package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.TimeZone;
import org.robolectric.shadows.ShadowLooper;

final class TestUtil {
    private TestUtil() {}

    /** İstanbul saatine göre sabit bir an. */
    static long istanbul(int y, int m, int d, int h, int min) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"));
        c.clear();
        c.set(y, m - 1, d, h, min, 0);
        return c.getTimeInMillis();
    }

    static void setCity(Context c, String name) {
        int i = Cities.indexOf(name);
        Prefs.setLocation(c, name, Cities.LAT[i], Cities.LON[i], Prefs.DEFAULT_TZ);
    }

    static String hhmm(long t) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"));
        c.setTimeInMillis(t);
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    /** Ekranın görüntüsünü PNG olarak kaydeder (gerçek çizim motoruyla). */
    static File screenshot(Activity a, String name) throws Exception {
        ShadowLooper.idleMainLooper();
        View root = a.getWindow().getDecorView();
        int w = root.getWidth(), h = root.getHeight();
        if (w == 0 || h == 0) {
            w = a.getResources().getDisplayMetrics().widthPixels;
            h = a.getResources().getDisplayMetrics().heightPixels;
            root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
            root.layout(0, 0, w, h);
        }
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(b));
        File dir = new File(System.getProperty("screenshotDir", "build/screenshots"));
        dir.mkdirs();
        File f = new File(dir, name + ".png");
        FileOutputStream out = new FileOutputStream(f);
        b.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
        return f;
    }

    /** Yazı boyutunu "sp" cinsinden verir. */
    static float sp(android.widget.TextView t) {
        return t.getTextSize() / t.getResources().getDisplayMetrics().scaledDensity;
    }
}
