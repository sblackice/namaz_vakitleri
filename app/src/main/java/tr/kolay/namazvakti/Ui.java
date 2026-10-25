package tr.kolay.namazvakti;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** Ekran yardımcıları. */
final class Ui {
    private Ui() {}

    /** Bütün yazıları kullanıcının seçtiği oranda büyütür. */
    static void scaleText(View v, float scale) {
        if (scale == 1.0f) return;
        if (v instanceof TextView) {
            TextView t = (TextView) v;
            t.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getTextSize() * scale);
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) scaleText(g.getChildAt(i), scale);
        }
    }
}
