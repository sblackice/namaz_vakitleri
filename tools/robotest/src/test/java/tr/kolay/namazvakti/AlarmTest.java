package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AlarmTest {
    private Application app;
    private ShadowAlarmManager alarms;
    private NotificationManager nm;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        alarms = shadowOf((AlarmManager) app.getSystemService(Context.ALARM_SERVICE));
        nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        TestUtil.setCity(app, "Ankara");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    private long[] today() { return Times.forDay(app, Clock.now(), 0); }

    /** Kurulu alarmı, alarm saatinde çalmış gibi tetikler. */
    private Intent fire() {
        ShadowAlarmManager.ScheduledAlarm a = alarms.peekNextScheduledAlarm();
        assertNotNull("alarm kurulmalı", a);
        Intent i = shadowOf(a.operation).getSavedIntent();
        Clock.fixedNow = a.getTriggerAtMs();
        ((AlarmManager) app.getSystemService(Context.ALARM_SERVICE)).cancel(a.operation);
        new AlarmReceiver().onReceive(app, i);
        return i;
    }

    private Notification lastNotification() {
        return shadowOf(nm).getNotification(Notifier.NOTIFICATION_ID);
    }

    @Test
    public void siradakiVakitIcinKesinAlarmKurulur() {
        AlarmScheduler.schedule(app);
        ShadowAlarmManager.ScheduledAlarm a = alarms.peekNextScheduledAlarm();
        assertEquals(today()[PrayerCalc.OGLE], a.getTriggerAtMs());
        assertEquals(AlarmManager.RTC_WAKEUP, a.getType());
        assertTrue("Doze modunda da çalmalı", a.isAllowWhileIdle());
    }

    @Test
    public void vakitGirinceBildirimGelirVeSonrakiKurulur() {
        AlarmScheduler.schedule(app);
        fire();
        Notification n = lastNotification();
        assertNotNull("bildirim gösterilmeli", n);
        String title = n.extras.getString(Notification.EXTRA_TITLE);
        System.out.println("Bildirim: " + title + " / " + n.extras.getCharSequence(Notification.EXTRA_TEXT));
        assertEquals("Öğle vakti girdi", title);
        assertEquals(Notifier.CHANNEL_ID, n.getChannelId());
        assertEquals(today()[PrayerCalc.IKINDI], alarms.peekNextScheduledAlarm().getTriggerAtMs());
    }

    @Test
    public void onceHatirlatmaCalisir() {
        Prefs.setRemindBefore(app, 15);
        AlarmScheduler.schedule(app);
        assertEquals(today()[PrayerCalc.OGLE] - 15 * 60000L,
                alarms.peekNextScheduledAlarm().getTriggerAtMs());
        fire();
        assertEquals("Öğle vaktine 15 dakika kaldı",
                lastNotification().extras.getString(Notification.EXTRA_TITLE));
    }

    @Test
    public void ezaniKapatilanVakitteBildirimYokAmaAlarmDevamEder() {
        Prefs.setAdhanOn(app, PrayerCalc.OGLE, false);
        AlarmScheduler.schedule(app);
        fire(); // öğle
        assertNull(lastNotification());
        assertNotNull(alarms.peekNextScheduledAlarm());
        fire(); // ikindi açık kaldı
        assertEquals("İkindi vakti girdi", lastNotification().extras.getString(Notification.EXTRA_TITLE));
    }

    @Test
    public void ezanDosyasiSeciliyseEzanCalarBildirimSesiCalmaz() {
        Prefs.setAdhanSound(app, "content://deneme/ezan.mp3", "ezan.mp3");
        AlarmScheduler.schedule(app);
        fire();
        Intent started = shadowOf(app).getNextStartedService();
        assertNotNull("ezan servisi başlamalı", started);
        assertEquals(AdhanService.ACTION_PLAY, started.getAction());
        assertEquals("Öğle vakti girdi", started.getStringExtra(AdhanService.EXTRA_TITLE));
        assertNull("ses kanallı bildirim gösterilmemeli", lastNotification());
    }

    @Test
    public void telefonSessizdeyseEzanCalmazSadeceBildirimGelir() {
        Prefs.setAdhanSound(app, "content://deneme/ezan.mp3", "ezan.mp3");
        ((AudioManager) app.getSystemService(Context.AUDIO_SERVICE))
                .setRingerMode(AudioManager.RINGER_MODE_SILENT);
        AlarmScheduler.schedule(app);
        fire();
        assertNull(shadowOf(app).getNextStartedService());
        assertEquals("Öğle vakti girdi", lastNotification().extras.getString(Notification.EXTRA_TITLE));

        // Kullanıcı "sessizdeyken de okunsun" derse ezan çalar.
        Prefs.setQuietWhenSilent(app, false);
        nm.cancelAll();
        fire();
        assertNotNull(shadowOf(app).getNextStartedService());
    }

    @Test
    public void imsakBildirimiSabahNamaziniSoyler() {
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 4, 0);
        AlarmScheduler.schedule(app);
        fire();
        String text = lastNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        assertTrue(text, text.startsWith("Sabah namazının vakti başladı"));
    }

    @Test
    public void ezanServisiDosyaYoksaKendiniDurdururVeBildirimBirakir() {
        Intent i = new Intent(app, AdhanService.class).setAction(AdhanService.ACTION_PLAY)
                .putExtra(AdhanService.EXTRA_TITLE, "Akşam vakti girdi")
                .putExtra(AdhanService.EXTRA_TEXT, "Akşam namazı vakti: 18:00");
        AdhanService svc = Robolectric.buildService(AdhanService.class, i).create()
                .startCommand(0, 1).get();
        assertTrue(shadowOf(svc).isStoppedBySelf());
        assertFalse(AdhanService.playing);
        Notification n = shadowOf(nm).getNotification(AdhanService.NOTIFICATION_ID);
        assertNotNull(n);
        assertEquals("Akşam vakti girdi", n.extras.getString(Notification.EXTRA_TITLE));
    }

    @Test
    public void gunesVaktindeBildirimYapilmaz() {
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 4, 0);
        AlarmScheduler.schedule(app);
        fire(); // imsak
        assertEquals("İmsak vakti girdi", lastNotification().extras.getString(Notification.EXTRA_TITLE));
        nm.cancelAll();
        fire(); // güneş
        assertNull(lastNotification());
    }

    @Test
    public void gecKalanAlarmIcinBildirimYapilmaz() {
        AlarmScheduler.schedule(app);
        ShadowAlarmManager.ScheduledAlarm a = alarms.peekNextScheduledAlarm();
        Intent i = shadowOf(a.operation).getSavedIntent();
        Clock.fixedNow = a.getTriggerAtMs() + 30 * 60000L; // telefon yarım saat kapalı kaldı
        new AlarmReceiver().onReceive(app, i);
        assertNull(lastNotification());
    }

    @Test
    public void telefonYenidenAcilincaAlarmYenidenKurulur() {
        assertNull(alarms.peekNextScheduledAlarm());
        new BootReceiver().onReceive(app, new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertEquals(today()[PrayerCalc.OGLE], alarms.peekNextScheduledAlarm().getTriggerAtMs());
    }

    @Test
    public void yatsidanSonraYarininImsakiKurulur() {
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 23, 0);
        AlarmScheduler.schedule(app);
        long tomorrowImsak = Times.forDay(app, Clock.now(), 1)[PrayerCalc.IMSAK];
        assertEquals(tomorrowImsak, alarms.peekNextScheduledAlarm().getTriggerAtMs());
    }
}
