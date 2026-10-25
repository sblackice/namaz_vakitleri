package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Eski Android sürümlerinde (7.0 ve 9) de açılıyor, bildirim gösteriyor mu? */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {24, 28})
public class OldAndroidTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        TestUtil.setCity(app, "Bursa");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    @Test
    public void anaEkranVeBildirimCalisir() {
        MainActivity m = Robolectric.buildActivity(MainActivity.class).setup().get();
        LinearLayout rows = (LinearLayout) m.findViewById(R.id.rows);
        assertEquals(6, rows.getChildCount());
        String t = ((TextView) rows.getChildAt(2).findViewById(R.id.row_time)).getText().toString();
        System.out.println("SDK " + android.os.Build.VERSION.SDK_INT + " Bursa öğle: " + t);

        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        Intent i = shadowOf(shadowOf(am).peekNextScheduledAlarm().operation).getSavedIntent();
        Clock.fixedNow = shadowOf(am).peekNextScheduledAlarm().getTriggerAtMs();
        new AlarmReceiver().onReceive(app, i);
        Notification n = shadowOf((NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE))
                .getNotification(Notifier.NOTIFICATION_ID);
        assertNotNull(n);
        assertEquals("Öğle vakti girdi", n.extras.getString(Notification.EXTRA_TITLE));
    }
}
