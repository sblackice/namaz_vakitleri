package tr.kolay.namazvakti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/**
 * Esmâ-ül Hüsnâ: Allah'ın 99 ismi. Koyu zemin üzerinde 3 sütunlu kartlar: altın renkli Arapça
 * isim (açık lisanslı Aref Ruqaa hat yazı tipiyle) ve altında Türkçe okunuşu. Bir karta
 * dokununca büyük bir pencerede anlamı çıkar ve Arapçası telefonun konuşma (metin okuma)
 * motoruyla seslendirilir. Sağ alttaki ▶ düğmesi 99 ismi sırayla okur; "Tesbih" Zikirmatik'i açar.
 * İnternet gerekmez; ancak telefonda Arapça ses paketi yüklü olmalıdır.
 */
final class EsmaPage extends Page implements TextToSpeech.OnInitListener {
    static final int TTS_NONE = 0, TTS_STARTING = 1, TTS_READY = 2, TTS_NO_ARABIC = 3,
            TTS_ERROR = 4;
    static final String UTT = "esma_";
    static final Locale ARABIC = new Locale("ar");
    static final int COLUMNS = 3;
    private static Typeface hat;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final View[] cards = new View[Esma.LISTE.length];
    private final TextView status, playAll;
    private final Button fixVoice;
    private final ScrollView scroll;
    private final LinearLayout list;
    private TextToSpeech tts;
    private int ttsState = TTS_NONE;
    private int speaking = -1;
    private boolean playingAll;
    private int pending = -1; // motor hazır olunca okunacak isim
    private AlertDialog detail;
    private int detailIndex = -1;

    /** Hat yazı tipi (assets/fonts, SIL Open Font License). Yüklenemezse telefonun yazı tipi kullanılır. */
    static Typeface hat(Context c) {
        if (hat == null) {
            try {
                hat = Typeface.createFromAsset(c.getAssets(), "fonts/aref_ruqaa_bold.ttf");
            } catch (RuntimeException e) {
                hat = Typeface.DEFAULT;
            }
        }
        return hat;
    }

    EsmaPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_esma);
        scroll = (ScrollView) view.findViewById(R.id.list_scroll);
        list = (LinearLayout) view.findViewById(R.id.list_items);
        playAll = (TextView) view.findViewById(R.id.esma_play_all);
        fixVoice = (Button) view.findViewById(R.id.esma_fix_voice);
        status = (TextView) view.findViewById(R.id.esma_status);
        LayoutInflater inf = a.getLayoutInflater();

        playAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (playingAll || speaking >= 0) stop(); else playFrom(0);
            }
        });
        fixVoice.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openVoiceSettings(); }
        });
        view.findViewById(R.id.esma_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { activity.select(MainActivity.HOME); }
        });
        view.findViewById(R.id.esma_tab_tesbih).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { activity.select(MainActivity.ZIKIR); }
        });
        view.findViewById(R.id.esma_info).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.menu_esma)
                        .setMessage(activity.getString(R.string.esma_intro) + "\n\n"
                                + activity.getString(R.string.esma_note))
                        .setPositiveButton("Tamam", null)
                        .show();
            }
        });

        Typeface font = hat(a);
        LinearLayout row = null;
        for (int i = 0; i < Esma.LISTE.length; i++) {
            if (i % COLUMNS == 0) {
                row = new LinearLayout(a);
                row.setOrientation(LinearLayout.HORIZONTAL);
                list.addView(row);
            }
            final int idx = i;
            Esma.Isim e = Esma.LISTE[i];
            View card = inf.inflate(R.layout.item_esma, row, false);
            TextView ar = (TextView) card.findViewById(R.id.esma_arapca);
            ar.setTypeface(font);
            ar.setText(e.arapca);
            ((TextView) card.findViewById(R.id.esma_okunus)).setText(e.okunus);
            card.setContentDescription((i + 1) + ". " + e.okunus + ": " + e.anlam);
            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { open(idx); }
            });
            cards[i] = card;
            row.addView(card);
        }
        // Son satır eksikse boşluk bırakılır (kartlar eşit genişlikte kalsın).
        while (row != null && row.getChildCount() < COLUMNS) {
            View gap = new View(a);
            row.addView(gap, new LinearLayout.LayoutParams(0, 1, 1f));
        }
    }

    View card(int i) { return cards[i]; }

    /** Bir ismin büyük penceresini açar ve okur. */
    void open(int idx) {
        if (detail == null) {
            View v = activity.getLayoutInflater().inflate(R.layout.dialog_esma, null);
            ((TextView) v.findViewById(R.id.esma_d_arapca)).setTypeface(hat(activity));
            Ui.scaleText(v, scale());
            v.findViewById(R.id.esma_d_prev).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View x) { open((detailIndex + Esma.LISTE.length - 1) % Esma.LISTE.length); }
            });
            v.findViewById(R.id.esma_d_next).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View x) { open((detailIndex + 1) % Esma.LISTE.length); }
            });
            v.findViewById(R.id.esma_d_play).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View x) {
                    if (speaking == detailIndex) stop(); else speakOne(detailIndex);
                }
            });
            v.findViewById(R.id.esma_d_close).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View x) { detail.dismiss(); }
            });
            detail = new AlertDialog.Builder(activity).setView(v).create();
            detail.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override public void onDismiss(DialogInterface d) {
                    if (!playingAll) stop();
                }
            });
            if (detail.getWindow() != null) {
                detail.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
        }
        detailIndex = idx;
        Esma.Isim e = Esma.LISTE[idx];
        detail.show();
        ((TextView) detail.findViewById(R.id.esma_d_no)).setText((idx + 1) + " / " + Esma.LISTE.length);
        ((TextView) detail.findViewById(R.id.esma_d_arapca)).setText(e.arapca);
        ((TextView) detail.findViewById(R.id.esma_d_okunus)).setText(e.okunus);
        ((TextView) detail.findViewById(R.id.esma_d_anlam)).setText(e.anlam);
        speakOne(idx);
    }

    AlertDialog detail() { return detail; }

    /** Arapça harekeler: tenvinler (0x064B-0x064D), üstün/ötre/esre (0x064E-0x0650), şedde, sükûn. */
    static final int HAREKE_ILK = 0x064B, SEDDE = 0x0651, HAREKE_SON = 0x0652;

    /**
     * Seslendirilecek metin: ismin son harfindeki ünlü harekesi (ötre, esre, üstün, tenvin)
     * atılır; tek başına okunan isim böylece durak hâliyle okunur. Şedde (çift harf) korunur:
     * "El-Aliyy" gibi isimler "El-Ali" diye okunmasın.
     */
    static String spoken(String arabic) {
        int start = arabic.length();
        boolean sedde = false;
        while (start > 0) {
            char c = arabic.charAt(start - 1);
            if (c < HAREKE_ILK || c > HAREKE_SON) break;
            if (c == SEDDE) sedde = true;
            start--;
        }
        String s = arabic.substring(0, start);
        return sedde ? s + (char) SEDDE : s;
    }

    @Override
    void onShow() {
        startEngine();
        updateControls();
    }

    @Override
    void onHide() { stop(); }

    @Override
    void onDestroy() {
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    int ttsState() { return ttsState; }

    int speaking() { return speaking; }

    private void startEngine() {
        if (tts != null || ttsState == TTS_STARTING) return;
        ttsState = TTS_STARTING;
        try {
            tts = new TextToSpeech(activity, this);
        } catch (RuntimeException e) {
            ttsState = TTS_ERROR;
        }
    }

    @Override
    public void onInit(int result) {
        if (tts == null) return;
        if (result != TextToSpeech.SUCCESS) {
            ttsState = TTS_ERROR;
        } else {
            int r = tts.setLanguage(ARABIC);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsState = TTS_NO_ARABIC;
            } else {
                ttsState = TTS_READY;
                tts.setSpeechRate(0.8f);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(final String id) {
                        handler.post(new Runnable() {
                            @Override public void run() { started(id); }
                        });
                    }

                    @Override public void onDone(final String id) {
                        handler.post(new Runnable() {
                            @Override public void run() { finished(id); }
                        });
                    }

                    @Override @SuppressWarnings("deprecation")
                    public void onError(final String id) {
                        handler.post(new Runnable() {
                            @Override public void run() { finished(id); }
                        });
                    }
                });
            }
        }
        handler.post(new Runnable() {
            @Override public void run() {
                updateControls();
                if (ttsState == TTS_READY && pending >= 0) {
                    int p = pending;
                    pending = -1;
                    if (playingAll) playFrom(p); else speakOne(p);
                }
            }
        });
    }

    void speakOne(int idx) {
        playingAll = false;
        say(idx);
    }

    void playFrom(int idx) {
        playingAll = true;
        say(idx);
    }

    private void say(int idx) {
        if (ttsState == TTS_STARTING || ttsState == TTS_NONE) {
            pending = idx;
            startEngine();
            status.setText("Konuşma motoru hazırlanıyor…");
            return;
        }
        if (ttsState != TTS_READY) {
            updateControls();
            return;
        }
        highlight(idx);
        tts.speak(spoken(Esma.LISTE[idx].arapca), TextToSpeech.QUEUE_FLUSH, null, UTT + idx);
        updateControls();
    }

    private void started(String id) {
        int idx = indexOf(id);
        if (idx >= 0) highlight(idx);
    }

    private void finished(String id) {
        int idx = indexOf(id);
        if (idx < 0 || idx != speaking) return;
        if (playingAll && idx + 1 < Esma.LISTE.length) {
            // İki isim arasında kısa bir ara
            final int next = idx + 1;
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (playingAll && speaking == next - 1) say(next);
                }
            }, 500);
        } else {
            playingAll = false;
            highlight(-1);
            updateControls();
        }
    }

    private static int indexOf(String id) {
        if (id == null || !id.startsWith(UTT)) return -1;
        try {
            return Integer.parseInt(id.substring(UTT.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    void stop() {
        playingAll = false;
        pending = -1;
        if (tts != null) tts.stop();
        highlight(-1);
        updateControls();
    }

    private void highlight(int idx) {
        if (speaking >= 0 && speaking < cards.length) cards[speaking].setSelected(false);
        speaking = idx;
        if (idx >= 0) {
            final View card = cards[idx];
            card.setSelected(true);
            if (playingAll) {
                scroll.post(new Runnable() {
                    @Override public void run() {
                        View row = (View) card.getParent();
                        scroll.smoothScrollTo(0, list.getTop() + row.getTop() - 24);
                    }
                });
            }
        }
    }

    private void updateControls() {
        boolean busy = playingAll || speaking >= 0;
        playAll.setText(busy ? "■" : "▶");
        playAll.setContentDescription(activity.getString(busy ? R.string.esma_stop : R.string.esma_play_all));
        if (detail != null && detail.isShowing()) {
            ((TextView) detail.findViewById(R.id.esma_d_play)).setText(
                    speaking == detailIndex && speaking >= 0 ? "■ Durdur" : "▶ Dinle");
        }
        fixVoice.setVisibility(ttsState == TTS_NO_ARABIC || ttsState == TTS_ERROR
                ? View.VISIBLE : View.GONE);
        switch (ttsState) {
            case TTS_NO_ARABIC:
                status.setText("Telefonunuzda Arapça ses paketi yüklü değil. Aşağıdaki düğmeyle "
                        + "yükleyebilirsiniz (internet gerekir, bir kez yüklenir).");
                break;
            case TTS_ERROR:
                status.setText("Telefonunuzda konuşma (metin okuma) motoru bulunamadı. "
                        + "Google'ın “Konuşma Hizmetleri” uygulamasını yükleyip açın.");
                break;
            default:
                status.setText(playingAll && speaking >= 0
                        ? "Okunuyor: " + (speaking + 1) + ". " + Esma.LISTE[speaking].okunus : "");
        }
        status.setVisibility(status.getText().length() == 0 ? View.GONE : View.VISIBLE);
    }

    private void openVoiceSettings() {
        Intent install = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(install);
            return;
        } catch (RuntimeException ignored) {
            // yükleme ekranı yoksa konuşma ayarları açılır
        }
        try {
            activity.startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
        } catch (RuntimeException e) {
            activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        }
    }
}
