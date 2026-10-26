package tr.kolay.namazvakti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Kıble pusulası çizimi:
 * - Telefonla birlikte dönen pusula halkası (2°'de bir çentik, 30°'de bir uzun çentik) ve
 *   her zaman dik okunan yön harfleri (K, D, G, B),
 * - Kâbe yönüne dönen desenli bir seccade, üstünde Kâbe ve beyaz ok,
 * - En üstte, telefonun baktığı yönü gösteren sabit turkuaz işaret.
 * Ok bu işaretle aynı hizaya gelince kişi Kıble'ye dönmüş olur.
 */
public class QiblaView extends View {
    static final float ALIGNED_DEG = 5f;
    private static final String[] DIRS = {"K", "D", "G", "B"};

    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final float density;
    private final Bitmap rug, kaaba;
    private float heading = 0;   // telefonun baktığı yön (gerçek kuzeyden derece)
    private float qibla = 0;     // Kâbe yönü (gerçek kuzeyden derece)
    private boolean hasHeading;

    public QiblaView(Context c, AttributeSet a) {
        super(c, a);
        density = getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        tick.setStyle(Paint.Style.STROKE);
        tick.setStrokeCap(Paint.Cap.ROUND);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeJoin(Paint.Join.ROUND);
        text.setTextAlign(Paint.Align.CENTER);
        rug = BitmapFactory.decodeResource(getResources(), R.drawable.qibla_rug);
        kaaba = BitmapFactory.decodeResource(getResources(), R.drawable.qibla_kaaba);
    }

    void set(float qiblaDeg, float headingDeg, boolean headingKnown) {
        qibla = qiblaDeg;
        heading = headingDeg;
        hasHeading = headingKnown;
        invalidate();
    }

    /** Okun ekrandaki açısı (saat yönünde): 0 ise ok tam yukarıyı, yani telefonun baktığı yönü gösterir. */
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
        int width = MeasureSpec.getSize(w);
        int size = Math.min(width, MeasureSpec.getSize(h));
        if (size == 0) size = width;
        setMeasuredDimension(width, size);
    }

    private int color(int attr) { return Ui.color(getContext(), attr); }

    @Override
    protected void onDraw(Canvas c) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) / 2f - 14 * density;
        float rot = hasHeading ? -heading : 0; // kuzeyin ekrandaki açısı
        int strong = color(R.attr.colText), soft = color(R.attr.colTextPast);
        int faint = (soft & 0x00FFFFFF) | 0x99000000;

        // Halka
        ring.setStrokeWidth(2.5f * density);
        ring.setColor(strong);
        c.drawCircle(cx, cy, r, ring);

        // Çentikler (telefonla birlikte döner)
        for (int deg = 0; deg < 360; deg += 2) {
            float len;
            if (deg % 30 == 0) {
                len = r * 0.075f;
                tick.setStrokeWidth(2.2f * density);
                tick.setColor(strong);
            } else if (deg % 10 == 0) {
                len = r * 0.05f;
                tick.setStrokeWidth(1.5f * density);
                tick.setColor(soft);
            } else {
                len = r * 0.03f;
                tick.setStrokeWidth(1f * density);
                tick.setColor(faint);
            }
            double a = Math.toRadians(deg + rot);
            float sx = (float) Math.sin(a), sy = (float) -Math.cos(a);
            float r0 = r - 3 * density - len, r1 = r - 3 * density;
            c.drawLine(cx + sx * r0, cy + sy * r0, cx + sx * r1, cy + sy * r1, tick);
        }

        // Yön harfleri: konumları döner, harfler hep dik durur
        text.setTextSize(r * 0.13f);
        text.setColor(strong);
        float lr = r * 0.83f;
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(i * 90 + rot);
            float x = cx + (float) Math.sin(a) * lr;
            float y = cy - (float) Math.cos(a) * lr + text.getTextSize() * 0.36f;
            c.drawText(DIRS[i], x, y, text);
        }

        // Telefonun baktığı yön: en üstte sabit turkuaz üçgen
        fill.setColor(color(R.attr.colAccent));
        path.reset();
        float tip = cy - r + 4 * density;
        path.moveTo(cx, tip);
        path.lineTo(cx - 11 * density, tip - 16 * density);
        path.lineTo(cx + 11 * density, tip - 16 * density);
        path.close();
        c.drawPath(path, fill);

        // Seccade, Kâbe ve ok: Kıble yönüne döner
        c.save();
        c.rotate(arrowAngle(), cx, cy);
        float rugH = r * 1.12f;
        float rugW = rugH * rug.getWidth() / rug.getHeight();
        float rugTop = cy - rugH * 0.46f;
        rect.set(cx - rugW / 2, rugTop, cx + rugW / 2, rugTop + rugH);
        c.drawBitmap(rug, null, rect, image);
        float ks = r * 0.22f;
        rect.set(cx - ks / 2, rugTop - ks * 0.98f, cx + ks / 2, rugTop - ks * 0.02f);
        c.drawBitmap(kaaba, null, rect, image);
        drawArrow(c, cx, rugTop + rugH * 0.16f, r * 0.40f);
        c.restore();
    }

    /** Ucu (x, tipY) noktasında olan, aşağı doğru "len" uzunluğunda beyaz ok. */
    private void drawArrow(Canvas c, float x, float tipY, float len) {
        float head = len * 0.42f, half = len * 0.30f, shaft = len * 0.11f;
        path.reset();
        path.moveTo(x, tipY);
        path.lineTo(x + half, tipY + head);
        path.lineTo(x + shaft, tipY + head);
        path.lineTo(x + shaft, tipY + len);
        path.lineTo(x - shaft, tipY + len);
        path.lineTo(x - shaft, tipY + head);
        path.lineTo(x - half, tipY + head);
        path.close();
        fill.setColor(0xFFFFFFFF);
        c.drawPath(path, fill);
        line.setColor(0xFF1E1E1E);
        line.setStrokeWidth(2f * density);
        c.drawPath(path, line);
    }
}
