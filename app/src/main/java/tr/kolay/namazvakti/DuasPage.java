package tr.kolay.namazvakti;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Namazda okunan dualar: sırayla, her birinin adı, Arapçası, okunuşu ve anlamı. */
final class DuasPage extends Page {
    DuasPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_duas);
        Ui.header(view, "Kolay Namaz Vakti", "Namazda Okunan Dualar");
        LinearLayout list = (LinearLayout) view.findViewById(R.id.duas_list);
        LayoutInflater inf = a.getLayoutInflater();
        String section = null;
        for (Dualar.Dua d : Dualar.LISTE) {
            if (!d.bolum.equals(section)) {
                section = d.bolum;
                TextView h = (TextView) inf.inflate(R.layout.item_section, list, false);
                h.setText(section);
                list.addView(h);
            }
            View item = inf.inflate(R.layout.item_dua, list, false);
            ((TextView) item.findViewById(R.id.dua_name)).setText(d.ad);
            ((TextView) item.findViewById(R.id.dua_arabic)).setText(d.arapca);
            ((TextView) item.findViewById(R.id.dua_reading)).setText(d.okunus);
            ((TextView) item.findViewById(R.id.dua_meaning)).setText(d.anlam);
            ((TextView) item.findViewById(R.id.dua_note)).setText(d.not);
            ((TextView) item.findViewById(R.id.dua_source)).setText("Kaynak: " + d.kaynak);
            list.addView(item);
        }
    }
}
