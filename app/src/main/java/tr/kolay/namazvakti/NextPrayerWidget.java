package tr.kolay.namazvakti;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** Ana ekran bileşeni: sıradaki vakit (büyük) ve günün 6 vakti. */
public class NextPrayerWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        updateAll(c);
    }

    static void updateAll(Context c) {
        AppWidgetManager m;
        try {
            m = AppWidgetManager.getInstance(c);
        } catch (Exception e) {
            return;
        }
        if (m == null) return;
        int[] ids = m.getAppWidgetIds(new ComponentName(c, NextPrayerWidget.class));
        if (ids == null || ids.length == 0) return;
        m.updateAppWidget(ids, build(c));
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_next);
        PendingIntent open = PendingIntent.getActivity(c, 1, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.w_root, open);
        if (!Prefs.hasLocation(c)) {
            rv.setTextViewText(R.id.w_place, c.getString(R.string.app_name));
            rv.setTextViewText(R.id.w_next, "Şehir seçmek için dokunun");
            rv.setTextViewText(R.id.w_line1, "");
            rv.setTextViewText(R.id.w_line2, "");
            return rv;
        }
        Times.Next n = Times.next(c, Clock.now());
        rv.setTextViewText(R.id.w_place, Prefs.placeName(c));
        rv.setTextViewText(R.id.w_next, "Sıradaki: " + Times.upper(Times.NAMES[n.index]) + "  "
                + Times.hhmm(c, n.time));
        rv.setTextViewText(R.id.w_line1, line(c, n.dayTimes, 0));
        rv.setTextViewText(R.id.w_line2, line(c, n.dayTimes, 3));
        return rv;
    }

    private static String line(Context c, long[] t, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + 3; i++) {
            if (sb.length() > 0) sb.append("   ");
            sb.append(Times.NAMES[i]).append(' ').append(Times.hhmm(c, t[i]));
        }
        return sb.toString();
    }
}
