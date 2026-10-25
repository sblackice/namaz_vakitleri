package tr.kolay.namazvakti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/** Pusula çizimi: kuzey işareti ve Kâbe yönünü gösteren büyük yeşil ok. */
public class QiblaView extends View {
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float heading = 0;   // telefonun baktığı yön (kuzeyden derece)
    private float qibla = 0;     // Kâbe yönü (kuzeyden derece)
    private boolean hasHeading;

    public QiblaView(Context c, AttributeSet a) {
        super(c, a);
        float d = getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(4 * d);
        ring.setColor(c.getColor(R.color.border));
        arrow.setColor(c.getColor(R.color.accent));
        text.setColor(c.getColor(R.color.text_main));
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
    }

    void set(float qiblaDeg, float headingDeg, boolean headingKnown) {
        qibla = qiblaDeg;
        heading = headingDeg;
        hasHeading = headingKnown;
        invalidate();
    }

    @Override
    protected void onMeasure(int w, int h) {
        int size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
        if (size == 0) size = MeasureSpec.getSize(w);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas c) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(cx, cy) * 0.85f;
        c.drawCircle(cx, cy, r, ring);

        // Telefon kuzeye bakmıyorsa pusula döner; yön bilinmiyorsa kuzey yukarıda varsayılır.
        float rot = hasHeading ? -heading : 0;
        c.save();
        c.rotate(rot, cx, cy);
        text.setTextSize(r * 0.22f);
        c.drawText("K", cx, cy - r + r * 0.25f, text);
        c.restore();

        c.save();
        c.rotate(rot + qibla, cx, cy);
        path.reset();
        path.moveTo(cx, cy - r * 0.95f);
        path.lineTo(cx + r * 0.22f, cy - r * 0.45f);
        path.lineTo(cx + r * 0.08f, cy - r * 0.45f);
        path.lineTo(cx + r * 0.08f, cy + r * 0.55f);
        path.lineTo(cx - r * 0.08f, cy + r * 0.55f);
        path.lineTo(cx - r * 0.08f, cy - r * 0.45f);
        path.lineTo(cx - r * 0.22f, cy - r * 0.45f);
        path.close();
        c.drawPath(path, arrow);
        c.restore();
    }
}
