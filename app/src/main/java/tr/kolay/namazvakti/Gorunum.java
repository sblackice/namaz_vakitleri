package tr.kolay.namazvakti;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Görünüm (tema) seçimi: "Koyu Yeşil" veya "Açık".
 * Renkler res/values (açık) ve res/values-night (koyu yeşil) klasörlerindedir; hangi klasörün
 * kullanılacağı telefonun gece modu ayarına göre değil, kullanıcının uygulamadaki seçimine göre belirlenir.
 */
final class Gorunum {
    private Gorunum() {}

    /** Her pencerenin (Activity) attachBaseContext'inde applyOverrideConfiguration ile uygulanır. */
    static Configuration config(Context base) {
        Configuration cfg = new Configuration();
        int night = Prefs.darkTheme(base) ? Configuration.UI_MODE_NIGHT_YES
                : Configuration.UI_MODE_NIGHT_NO;
        int current = base.getResources().getConfiguration().uiMode;
        cfg.uiMode = (current & ~Configuration.UI_MODE_NIGHT_MASK) | night;
        return cfg;
    }
}
