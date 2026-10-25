package tr.kolay.namazvakti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Kıble pusulası çizimi: dönen pusula halkası (K/D/G/B), Kâbe yönüne dönen bir seccade,
 * seccadenin üzerinde Kâbe'yi gösteren büyük ok ve en üstte telefonun baktığı yönü gösteren
 * sabit işaret. Ok bu işaretle aynı hizaya gelince kişi Kıble'ye dönmüş olur.
 */
public class QiblaView extends View {
    static final float ALIGNED_DEG = 5f;

    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final float density;
    private float heading = 0;   // telefonun baktığı yön (kuzeyden derece)
    private float qibla = 0;     // Kâbe yönü (kuzeyden derece)
    private boolean hasHeading;

    public QiblaView(Context c, AttributeSet a) {
        super(c, a);
        density = getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
    }

    void set(float qiblaDeg, float headingDeg, boolean headingKnown) {
        qibla = qiblaDeg;
        heading = headingDeg;
        hasHeading = headingKnown;
        invalidate();
    }

    /** Okun ekrandaki açısı: 0 ise ok tam yukarıyı (telefonun baktığı yönü) gösterir. */
    float arrowAngle() {
        float a = qibla - (hasHeading ? heading : 0);
        return ((a % 360f) + 360f) % 360f;
    }

    boolean aligned() {
        if (!hasHeading) return false;
        float d = arrowAngle();
        return d <= ALIGNED_DEG || d >= 360f - ALIGNED_DEG;
    }

    @Override
    protected void onMeasure(int w, int h) {
        int size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
        if (size == 0) size = MeasureSpec.getSize(w);
        setMeasuredDimension(MeasureSpec.getSize(w), size);
    }

    private int color(int id) { return getContext().getColor(id); }

    @Override
    protected void onDraw(Canvas c) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) / 2f * 0.84f;
        boolean ok = aligned();

        // Pusula halkası ve yön harfleri (telefon dönünce harfler de döner)
        ring.setStrokeWidth((ok ? 7 : 4) * density);
        ring.setColor(color(ok ? R.color.accent : R.color.border));
        c.drawCircle(cx, cy, r, ring);
        float rot = hasHeading ? -heading : 0;
        text.setTextSize(r * 0.13f);
        String[] dirs = {"K", "D", "G", "B"};
        for (int i = 0; i < 4; i++) {
            c.save();
            c.rotate(rot + i * 90, cx, cy);
            text.setColor(color(i == 0 ? R.color.warn : R.color.text_main));
            c.drawText(dirs[i], cx, cy - r + r * 0.17f, text);
            c.restore();
        }

        // Telefonun baktığı yön: en üstte sabit yeşil üçgen
        fill.setColor(color(R.color.accent));
        path.reset();
        path.moveTo(cx, cy - r - 2 * density);
        path.lineTo(cx - r * 0.07f, cy - r - r * 0.13f);
        path.lineTo(cx + r * 0.07f, cy - r - r * 0.13f);
        path.close();
        c.drawPath(path, fill);

        c.save();
        c.rotate(arrowAngle(), cx, cy);
        drawRug(c, cx, cy, r);
        drawArrow(c, cx, cy, r);
        drawKaaba(c, cx, cy - r * 0.79f, r * 0.12f);
        c.restore();
    }

    /** Seccade: kırmızı zemin, altın kenar, üstte mihrap kemeri, uçlarda püsküller. */
    private void drawRug(Canvas c, float cx, float cy, float r) {
        float w = r * 0.40f, top = cy - r * 0.62f, bottom = cy + r * 0.62f;
        rect.set(cx - w, top, cx + w, bottom);
        fill.setColor(color(R.color.rug));
        c.drawRoundRect(rect, r * 0.04f, r * 0.04f, fill);

        line.setColor(color(R.color.gold));
        line.setStrokeWidth(3 * density);
        float in = r * 0.05f;
        rect.set(cx - w + in, top + in, cx + w - in, bottom - in);
        c.drawRoundRect(rect, r * 0.03f, r * 0.03f, line);

        // Mihrap kemeri (sivri kemer)
        float aw = w - in * 2.2f, aTop = top + in * 2.2f, aBase = top + r * 0.42f;
        path.reset();
        path.moveTo(cx - aw, aBase);
        path.lineTo(cx - aw, aTop + r * 0.16f);
        path.quadTo(cx - aw, aTop + r * 0.02f, cx, aTop);
        path.quadTo(cx + aw, aTop + r * 0.02f, cx + aw, aTop + r * 0.16f);
        path.lineTo(cx + aw, aBase);
        path.close();
        fill.setColor(color(R.color.rug_dark));
        c.drawPath(path, fill);
        c.drawPath(path, line);

        // Alt kısımda süsleme: baklava dilimi
        float dy = cy + r * 0.38f, ds = r * 0.12f;
        path.reset();
        path.moveTo(cx, dy - ds);
        path.lineTo(cx + ds * 0.8f, dy);
        path.lineTo(cx, dy + ds);
        path.lineTo(cx - ds * 0.8f, dy);
        path.close();
        c.drawPath(path, line);

        // Püsküller
        line.setStrokeWidth(2 * density);
        int n = 9;
        for (int i = 0; i < n; i++) {
            float x = cx - w + (2 * w) * (i + 0.5f) / n;
            c.drawLine(x, top, x, top - r * 0.05f, line);
            c.drawLine(x, bottom, x, bottom + r * 0.05f, line);
        }
    }

    /** Seccadenin ortasında, Kâbe yönünü gösteren beyaz ok. */
    private void drawArrow(Canvas c, float cx, float cy, float r) {
        path.reset();
        path.moveTo(cx, cy - r * 0.55f);
        path.lineTo(cx + r * 0.22f, cy - r * 0.22f);
        path.lineTo(cx + r * 0.08f, cy - r * 0.22f);
        path.lineTo(cx + r * 0.08f, cy + r * 0.45f);
        path.lineTo(cx - r * 0.08f, cy + r * 0.45f);
        path.lineTo(cx - r * 0.08f, cy - r * 0.22f);
        path.lineTo(cx - r * 0.22f, cy - r * 0.22f);
        path.close();
        fill.setColor(color(R.color.on_accent));
        c.drawPath(path, fill);
        line.setColor(color(R.color.text_main));
        line.setStrokeWidth(2 * density);
        c.drawPath(path, line);
    }

    /** Okun ucunda küçük Kâbe simgesi: siyah küp ve altın kuşak. */
    private void drawKaaba(Canvas c, float x, float y, float s) {
        rect.set(x - s, y - s, x + s, y + s);
        fill.setColor(color(R.color.text_main));
        c.drawRect(rect, fill);
        fill.setColor(color(R.color.gold));
        rect.set(x - s, y - s * 0.45f, x + s, y - s * 0.2f);
        c.drawRect(rect, fill);
    }
}
