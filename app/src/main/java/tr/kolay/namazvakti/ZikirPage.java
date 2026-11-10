package tr.kolay.namazvakti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Zikirmatik. Ortadaki büyük düğmeye her dokunuşta sayı bir artar; sayı ve hedef telefonda
 * saklanır (uygulama kapansa da kaybolmaz). Hedefe ulaşınca telefon uzun titrer.
 */
final class ZikirPage extends Page {
    private final ZikirView dial;
    private final TextView targetText;

    ZikirPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_zikir);
        Ui.header(view, "Menü", a.getString(R.string.menu_zikir));
        dial = (ZikirView) view.findViewById(R.id.zikir_view);
        targetText = (TextView) view.findViewById(R.id.zikir_target);
        dial.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { add(1); }
        });
        view.findViewById(R.id.zikir_plus).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { add(1); }
        });
        view.findViewById(R.id.zikir_minus).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { add(-1); }
        });
        view.findViewById(R.id.zikir_reset).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { askReset(); }
        });
        view.findViewById(R.id.zikir_target_box).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { askTarget(); }
        });
    }

    @Override
    void onShow() {
        view.setKeepScreenOn(true); // zikir çekerken ekran kararmasın
        show();
    }

    @Override
    void onHide() { view.setKeepScreenOn(false); }

    void show() {
        int n = Prefs.zikirCount(activity), t = Prefs.zikirTarget(activity);
        dial.set(n, t);
        targetText.setText(String.valueOf(t));
    }

    void add(int delta) {
        int before = Prefs.zikirCount(activity);
        int after = Math.max(0, before + delta);
        Prefs.setZikirCount(activity, after);
        show();
        if (delta > 0) {
            int t = Prefs.zikirTarget(activity);
            if (before < t && after >= t) {
                vibrate(600);
            } else {
                dial.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrate(long ms) {
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(ms);
        }
    }

    AlertDialog askReset() {
        AlertDialog d = new AlertDialog.Builder(activity)
                .setTitle("Sayaç sıfırlansın mı?")
                .setMessage("Şu anki sayı: " + Prefs.zikirCount(activity))
                .setPositiveButton("Evet, sıfırla", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface di, int w) {
                        Prefs.setZikirCount(activity, 0);
                        show();
                    }
                })
                .setNegativeButton("Hayır", null)
                .create();
        d.show();
        bigButtons(d);
        return d;
    }

    /** Hedef sayısını girme penceresi: hazır seçenekler ve rakam kutusu. */
    AlertDialog askTarget() {
        float d = activity.getResources().getDisplayMetrics().density;
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * d);
        box.setPadding(pad, Math.round(8 * d), pad, 0);

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        input.setGravity(Gravity.CENTER);
        input.setText(String.valueOf(Prefs.zikirTarget(activity)));
        input.setSelectAllOnFocus(true);
        input.setContentDescription("Hedef zikir sayısı");

        LinearLayout quick = new LinearLayout(activity);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        for (final int q : Prefs.ZIKIR_HEDEFLERI) {
            Button b = new Button(activity);
            b.setText(String.valueOf(q));
            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setPadding(0, 0, 0, 0);
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { input.setText(String.valueOf(q)); }
            });
            quick.addView(b, new LinearLayout.LayoutParams(0, Math.round(56 * d), 1f));
        }
        box.addView(quick);
        box.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Hedeflenen zikir sayısı")
                .setView(box)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface di, int w) {
                        setTarget(input.getText().toString());
                    }
                })
                .setNegativeButton("Vazgeç", null)
                .create();
        dialog.show();
        bigButtons(dialog);
        return dialog;
    }

    void setTarget(String s) {
        try {
            int n = Integer.parseInt(s.trim());
            if (n > 0) Prefs.setZikirTarget(activity, n);
        } catch (NumberFormatException ignored) {
            // boş veya geçersiz giriş: hedef değişmez
        }
        show();
    }

    private static void bigButtons(AlertDialog d) {
        for (int w : new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE}) {
            Button b = d.getButton(w);
            if (b != null) b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
    }
}
