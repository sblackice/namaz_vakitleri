package tr.kolay.namazvakti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * Kullanıcının seçtiği ezan ses dosyasını çalar.
 * Ön planda çalışan bir servistir; bildirimdeki "Durdur" düğmesiyle veya ses bitince durur.
 * Ses, telefonun ALARM ses seviyesinde çalar. En fazla 10 dakika çalar (güvenlik sınırı).
 */
public class AdhanService extends Service {
    static final String ACTION_PLAY = "tr.kolay.namazvakti.EZAN_CAL";
    static final String ACTION_STOP = "tr.kolay.namazvakti.EZAN_DURDUR";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_TEXT = "text";
    static final String EXTRA_TEST = "test";
    static final String CHANNEL_ID = "ezan";
    static final int NOTIFICATION_ID = 2;
    static final long MAX_PLAY_MS = 10 * 60 * 1000L;

    /** Ekranda "Durdur" düğmesini göstermek için. */
    static volatile boolean playing;

    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String title = "", text = "";
    private boolean test;
    private final Runnable safetyStop = new Runnable() {
        @Override public void run() { finish(true); }
    };

    /** Ezanı başlatır. Başlatılamazsa (ör. sistem izin vermezse) false döner. */
    static boolean start(Context c, String title, String text) {
        return start(c, title, text, false);
    }

    /** Ayarlar ekranındaki "Ezanı Dene" düğmesi için: bitince bildirim bırakmaz. */
    static boolean startTest(Context c) {
        return start(c, "Ezan sesi denemesi", "", true);
    }

    private static boolean start(Context c, String title, String text, boolean test) {
        Intent i = new Intent(c, AdhanService.class).setAction(ACTION_PLAY)
                .putExtra(EXTRA_TITLE, title).putExtra(EXTRA_TEXT, text).putExtra(EXTRA_TEST, test);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(i);
            } else {
                c.startService(i);
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static void stop(Context c) {
        try {
            c.startService(new Intent(c, AdhanService.class).setAction(ACTION_STOP));
        } catch (RuntimeException e) {
            c.stopService(new Intent(c, AdhanService.class));
        }
    }

    /**
     * Şu an ezan sesi çalınabilir mi? Telefonda konuşulurken çalınmaz; kullanıcı istemişse
     * telefon sessiz veya titreşimdeyken de çalınmaz.
     */
    static boolean canPlayNow(Context c) {
        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return true;
        int mode = am.getMode();
        if (mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION
                || mode == AudioManager.MODE_RINGTONE) {
            return false;
        }
        return !(Prefs.quietWhenSilent(c) && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
    }

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Ezan okunurken",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setSound(null, null); // ses, uygulamanın kendisi tarafından çalınıyor
        ch.enableVibration(false);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            finish(true);
            return START_NOT_STICKY;
        }
        title = intent == null ? "" : String.valueOf(intent.getStringExtra(EXTRA_TITLE));
        text = intent == null ? "" : String.valueOf(intent.getStringExtra(EXTRA_TEXT));
        test = intent != null && intent.getBooleanExtra(EXTRA_TEST, false);
        startInForeground();
        if (!play()) finish(true);
        return START_NOT_STICKY;
    }

    private void startInForeground() {
        Notification n = build(true);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, n);
        }
    }

    private boolean play() {
        releasePlayer();
        String uri = Prefs.adhanUri(this);
        if (uri == null) return false;
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            player.setDataSource(this, Uri.parse(uri));
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer mp) { finish(false); }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer mp, int what, int extra) {
                    finish(false);
                    return true;
                }
            });
            player.prepare();
            player.start();
            playing = true;
            handler.removeCallbacks(safetyStop);
            handler.postDelayed(safetyStop, MAX_PLAY_MS);
            return true;
        } catch (Exception e) {
            releasePlayer();
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private Notification build(boolean withStop) {
        ensureChannel(this);
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(this, 3,
                new Intent(this, AdhanService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this).setPriority(Notification.PRIORITY_HIGH);
        b.setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText(withStop ? "Ezan okunuyor. Durdurmak için “Durdur”a dokunun." : text)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(getColor(R.color.accent))
                .setContentIntent(withStop ? stop : open)
                .setAutoCancel(!withStop)
                .setOngoing(withStop);
        if (withStop) {
            b.addAction(new Notification.Action.Builder(null, "Durdur", stop).build());
        }
        return b.build();
    }

    /** Sesi durdurur. Vakit bildirimi (Durdur düğmesi olmadan) bildirimlerde kalır. */
    @SuppressWarnings("deprecation")
    void finish(boolean byUser) {
        handler.removeCallbacks(safetyStop);
        releasePlayer();
        playing = false;
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(false);
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            if (!test && title.length() > 0) {
                nm.notify(NOTIFICATION_ID, build(false));
            } else {
                nm.cancel(NOTIFICATION_ID);
            }
        }
        stopSelf();
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {
                // hazırlanmamış oynatıcı durdurulamaz; yine de serbest bırakılır
            }
            player.release();
            player = null;
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(safetyStop);
        releasePlayer();
        playing = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
