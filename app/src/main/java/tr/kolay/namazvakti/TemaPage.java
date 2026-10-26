package tr.kolay.namazvakti;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Renk Teması: 6 renk düzeninden birini seçer; seçince pencere yeni renklerle yeniden açılır. */
final class TemaPage extends Page {
    private final View[] items = new View[Tema.ADLAR.length];

    TemaPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_list);
        Ui.header(view, "Menü", a.getString(R.string.menu_tema));
        ((TextView) view.findViewById(R.id.list_intro)).setText(R.string.tema_intro);
        view.findViewById(R.id.list_note).setVisibility(View.GONE);
        LinearLayout list = (LinearLayout) view.findViewById(R.id.list_items);
        LayoutInflater inf = a.getLayoutInflater();
        float d = a.getResources().getDisplayMetrics().density;
        for (int i = 0; i < Tema.ADLAR.length; i++) {
            final int idx = i;
            View item = inf.inflate(R.layout.item_tema, list, false);
            ((TextView) item.findViewById(R.id.tema_ad)).setText(Tema.ADLAR[i]);
            item.setContentDescription(Tema.ADLAR[i]);

            // Küçük örnek: temanın zemini, yazısı ve düğme rengi
            Context t = new android.view.ContextThemeWrapper(a, Tema.STILLER[i]);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Ui.color(t, R.attr.colBg));
            bg.setStroke(Math.round(2 * d), Ui.color(t, R.attr.colBorder));
            bg.setCornerRadius(10 * d);
            item.findViewById(R.id.tema_ornek).setBackground(bg);
            ((TextView) item.findViewById(R.id.tema_ornek_yazi)).setTextColor(Ui.color(t, R.attr.colText));
            TextView btn = (TextView) item.findViewById(R.id.tema_ornek_dugme);
            GradientDrawable bb = new GradientDrawable();
            bb.setColor(Ui.color(t, R.attr.colAccent));
            bb.setCornerRadius(6 * d);
            btn.setBackground(bb);
            btn.setTextColor(Ui.color(t, R.attr.colOnAccent));

            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { choose(idx); }
            });
            items[i] = item;
            list.addView(item);
        }
    }

    @Override
    void onShow() {
        int sel = Tema.secili(activity);
        for (int i = 0; i < items.length; i++) {
            items[i].findViewById(R.id.tema_secili).setVisibility(i == sel ? View.VISIBLE : View.INVISIBLE);
            items[i].setSelected(i == sel);
        }
    }

    void choose(int idx) {
        if (idx == Tema.secili(activity)) return;
        Prefs.setTheme(activity, idx);
        NextPrayerWidget.updateAll(activity);
        activity.applySettingsIfChanged(); // pencere yeni renklerle, bu sayfada yeniden açılır
    }
}
