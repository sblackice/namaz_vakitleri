package tr.kolay.namazvakti;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** Ekran yardımcıları. */
final class Ui {
    private Ui() {}

    /** Seçili renk temasındaki bir rengi okur (ör. R.attr.colAccent). */
    static int color(Context c, int attr) {
        TypedValue tv = new TypedValue();
        if (!c.getTheme().resolveAttribute(attr, tv, true)) return 0xFF000000;
        if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return tv.data;
        }
        return c.getColor(tv.resourceId);
    }

    /** Açık/kapalı düğme: açıkken tema rengi, kapalıyken gri (yazı rengi zemine göre). */
    static void onOff(TextView b, boolean on) {
        b.setBackgroundResource(on ? R.drawable.button : R.drawable.button_off);
        b.setTextColor(on ? color(b.getContext(), R.attr.colOnAccent) : 0xFFFFFFFF);
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

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

    /** Sayfa başlığını doldurur (görsel: cami simgesi, küçük üst yazı, büyük başlık). */
    static void header(View root, String caption, String title) {
        ((TextView) root.findViewById(R.id.header_caption)).setText(Times.upper(caption));
        ((TextView) root.findViewById(R.id.header_title)).setText(Times.upper(title));
    }

    /** Başlığın sağındaki düğme (ör. şehir değiştir, geri). */
    static void headerAction(View root, int icon, String label, View.OnClickListener click) {
        View action = root.findViewById(R.id.header_action);
        ((ImageView) action.findViewById(R.id.header_action_icon)).setImageResource(icon);
        ((TextView) action.findViewById(R.id.header_action_label)).setText(label);
        action.setContentDescription(label);
        action.setOnClickListener(click);
        action.setVisibility(View.VISIBLE);
    }
}
