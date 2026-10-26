package tr.kolay.namazvakti;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;

/** Renk temaları (Menü → Renk Teması). Renkler res/values/styles.xml içindedir. */
final class Tema {
    private Tema() {}

    static final String[] ADLAR = {
        "Krem üzerine Turkuaz",
        "Beyaz üzerine Kırmızı",
        "Pudra üzerine Yeşil",
        "Pudra üzerine Turkuaz",
        "Siyah üzerine Beyaz",
        "Siyah üzerine Kırmızı",
    };

    static final int[] STILLER = {
        R.style.AppTheme,
        R.style.Tema_KirmiziBeyaz,
        R.style.Tema_YesilPudra,
        R.style.Tema_TurkuazPudra,
        R.style.Tema_BeyazSiyah,
        R.style.Tema_KirmiziSiyah,
    };

    /** Ana ekran bileşeninin (widget) zemini; widget tema özelliklerini okuyamaz. */
    static final int[] WIDGET_ZEMIN = {
        R.drawable.widget_bg,
        R.drawable.widget_bg_kirmizi,
        R.drawable.widget_bg_yesil,
        R.drawable.widget_bg_turkuaz,
        R.drawable.widget_bg_siyah,
        R.drawable.widget_bg_siyah_kirmizi,
    };

    static int secili(Context c) {
        int i = Prefs.theme(c);
        return i >= 0 && i < STILLER.length ? i : 0;
    }

    /** Pencere açılırken çağrılır (setContentView'dan önce). */
    static void uygula(Activity a) {
        a.setTheme(STILLER[secili(a)]);
    }

    /** Pencere dışında (bildirim, servis) tema rengi okumak için. */
    static Context baglam(Context c) {
        return new ContextThemeWrapper(c, STILLER[secili(c)]);
    }
}
