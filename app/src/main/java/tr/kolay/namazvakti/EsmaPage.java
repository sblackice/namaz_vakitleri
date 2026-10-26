package tr.kolay.namazvakti;

import android.content.Intent;
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
 * Esmâ-ül Hüsnâ: Allah'ın 99 ismi. Her kartta sıra numarası, Arapça yazılış, Türkçe okunuş ve
 * anlam vardır. ▶ düğmesi ismin Arapçasını telefonun konuşma (metin okuma) motoruyla seslendirir.
 * İnternet gerekmez; ancak telefonda Arapça ses paketi yüklü olmalıdır.
 */
final class EsmaPage extends Page implements TextToSpeech.OnInitListener {
    static final int TTS_NONE = 0, TTS_STARTING = 1, TTS_READY = 2, TTS_NO_ARABIC = 3,
            TTS_ERROR = 4;
    static final String UTT = "esma_";
    static final Locale ARABIC = new Locale("ar");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final View[] cards = new View[Esma.LISTE.length];
    private final TextView status;
    private final Button playAll, fixVoice;
    private final ScrollView scroll;
    private final LinearLayout list;
    private TextToSpeech tts;
    private int ttsState = TTS_NONE;
    private int speaking = -1;
    private boolean playingAll;
    private int pending = -1; // motor hazır olunca okunacak isim

    EsmaPage(MainActivity a, ViewGroup parent) {
        super(a, parent, R.layout.page_list);
        Ui.header(view, "Allah'ın 99 İsmi", a.getString(R.string.menu_esma));
        ((TextView) view.findViewById(R.id.list_intro)).setText(R.string.esma_intro);
        ((TextView) view.findViewById(R.id.list_note)).setText(R.string.esma_note);
        scroll = (ScrollView) view.findViewById(R.id.list_scroll);
        list = (LinearLayout) view.findViewById(R.id.list_items);
        LayoutInflater inf = a.getLayoutInflater();

        LinearLayout top = (LinearLayout) view.findViewById(R.id.list_top);
        View controls = inf.inflate(R.layout.esma_controls, top, false);
        top.addView(controls);
        playAll = (Button) controls.findViewById(R.id.esma_play_all);
        fixVoice = (Button) controls.findViewById(R.id.esma_fix_voice);
        status = (TextView) controls.findViewById(R.id.esma_status);
        playAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (playingAll || speaking >= 0) stop(); else playFrom(0);
            }
        });
        fixVoice.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openVoiceSettings(); }
        });

        for (int i = 0; i < Esma.LISTE.length; i++) {
            final int idx = i;
            Esma.Isim e = Esma.LISTE[i];
            View card = inf.inflate(R.layout.item_esma, list, false);
            ((TextView) card.findViewById(R.id.esma_no)).setText(String.valueOf(i + 1));
            ((TextView) card.findViewById(R.id.esma_arapca)).setText(e.arapca);
            ((TextView) card.findViewById(R.id.esma_okunus)).setText(e.okunus);
            ((TextView) card.findViewById(R.id.esma_anlam)).setText(e.anlam);
            Button b = (Button) card.findViewById(R.id.esma_dinle);
            b.setContentDescription(e.okunus + " ismini dinle");
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (speaking == idx) stop(); else speakOne(idx);
                }
            });
            cards[i] = card;
            list.addView(card);
        }
    }

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
        if (speaking >= 0 && speaking < cards.length) {
            cards[speaking].setBackgroundResource(R.drawable.row_normal);
            ((Button) cards[speaking].findViewById(R.id.esma_dinle)).setText("▶");
        }
        speaking = idx;
        if (idx >= 0) {
            final View card = cards[idx];
            card.setBackgroundResource(R.drawable.card_outline);
            ((Button) card.findViewById(R.id.esma_dinle)).setText("■");
            if (playingAll) {
                scroll.post(new Runnable() {
                    @Override public void run() {
                        scroll.smoothScrollTo(0, list.getTop() + card.getTop() - 24);
                    }
                });
            }
        }
    }

    private void updateControls() {
        boolean busy = playingAll || speaking >= 0;
        playAll.setText(busy ? R.string.esma_stop : R.string.esma_play_all);
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
                status.setText(busy && speaking >= 0
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
