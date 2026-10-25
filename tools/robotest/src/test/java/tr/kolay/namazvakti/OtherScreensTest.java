package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.view.View;
import android.widget.Button;
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
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowAppWidgetManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w400dp-h860dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class OtherScreensTest {
    private Application app;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.getApplication();
        TestUtil.setCity(app, "Ankara");
        Clock.fixedNow = TestUtil.istanbul(2026, 10, 15, 12, 0);
    }

    @After
    public void tearDown() { Clock.fixedNow = 0; }

    @Test
    public void ayarlarDugmeleriCalisir() throws Exception {
        SettingsActivity a = Robolectric.buildActivity(SettingsActivity.class).setup().get();
        Button notify = (Button) a.findViewById(R.id.btn_notify);
        Button remind = (Button) a.findViewById(R.id.btn_remind);
        assertEquals("Vakit girince bildirim: AÇIK", notify.getText().toString());
        TestUtil.screenshot(a, "5-ayarlar");
        notify.performClick();
        assertEquals("Vakit girince bildirim: KAPALI", notify.getText().toString());
        remind.performClick();
        assertEquals("Vakitten önce hatırlat: 10 dakika", remind.getText().toString());
        assertEquals(10, Prefs.remindBefore(app));
    }

    @Test
    public void dakikaDuzeltmesiAnaEkranaYansir() {
        long before = Times.forDay(app, Clock.now(), 0)[PrayerCalc.AKSAM];
        SettingsActivity s = Robolectric.buildActivity(SettingsActivity.class).setup().get();
        View row = ((LinearLayout) s.findViewById(R.id.offset_rows)).getChildAt(PrayerCalc.AKSAM);
        row.findViewById(R.id.off_plus).performClick();
        row.findViewById(R.id.off_plus).performClick();
        assertEquals("+2", ((TextView) row.findViewById(R.id.off_value)).getText().toString());

        MainActivity m = Robolectric.buildActivity(MainActivity.class).setup().get();
        TextView t = (TextView) ((LinearLayout) m.findViewById(R.id.rows)).getChildAt(PrayerCalc.AKSAM)
                .findViewById(R.id.row_time);
        assertEquals(TestUtil.hhmm(before + 2 * 60000L), t.getText().toString());
    }

    @Test
    public void kibleEkraniAciVerir() throws Exception {
        QiblaActivity a = Robolectric.buildActivity(QiblaActivity.class).setup().get();
        String text = ((TextView) a.findViewById(R.id.qibla_angle)).getText().toString();
        Ref r = Ref.find(Ref.load(), "Ankara", 2026, 10, 15);
        System.out.println(text + " | adhan: " + r.qibla);
        assertTrue(text, text.contains(Math.round(r.qibla) + "°"));
        TestUtil.screenshot(a, "6-kible");
    }

    @Test
    public void widgetSiradakiVaktiGosterir() {
        ShadowAppWidgetManager w = shadowOf(AppWidgetManager.getInstance(app));
        int id = w.createWidget(NextPrayerWidget.class, R.layout.widget_next);
        NextPrayerWidget.updateAll(app);
        View v = w.getViewFor(id);
        String next = ((TextView) v.findViewById(R.id.w_next)).getText().toString();
        String line2 = ((TextView) v.findViewById(R.id.w_line2)).getText().toString();
        System.out.println("Widget: " + next + " | " + ((TextView) v.findViewById(R.id.w_line1)).getText()
                + " | " + line2);
        assertTrue(next, next.startsWith("Sıradaki: ÖĞLE"));
        assertTrue(line2, line2.contains("Yatsı"));
    }
}
