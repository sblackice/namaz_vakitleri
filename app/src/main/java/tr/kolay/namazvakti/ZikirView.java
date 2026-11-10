package tr.kolay.namazvakti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Zikirmatik kadranı ve ortadaki büyük düğme.
 * - Ortada yuvarlak, büyük bir düğme: içinde o anki zikir sayısı yazar. Dokununca sayı artar.
 * - Düğmenin sol alt tarafından başlayıp üstünden geçerek sağ alta inen bir kadran (gösterge).
 *   Başında "0", sonunda hedeflenen zikir sayısı yazar. Zikir çektikçe renkli kısım hedefe
 *   doğru ilerler.
 */
public class ZikirView extends View {
    /** Kadranın başladığı açı (saat 7 yönü) ve taradığı açı (saat yönünde, üstten geçerek). */
    static final float START = 150f, SWEEP = 240f;

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progress = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private int count, target = 33;
    private boolean pressed;
    private float cx, cy, radius, buttonR;

    public ZikirView(Context c, AttributeSet a) {
        super(c, a);
        density = getResources().getDisplayMetrics().density;
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        progress.setStyle(Paint.Style.STROKE);
        progress.setStrokeCap(Paint.Cap.ROUND);
        tick.setStyle(Paint.Style.STROKE);
        tick.setStrokeCap(Paint.Cap.ROUND);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
        setClickable(true);
        setFocusable(true);
    }

    void set(int count, int target) {
        this.count = count;
        this.target = Math.max(1, target);
        setContentDescription("Zikir düğmesi. Sayı " + count + ", hedef " + this.target
                + ". Saymak için dokunun.");
        invalidate();
    }

    /** Kadranın dolu kısmı (0..1). */
    float fraction() { return Math.min(1f, count / (float) target); }

    boolean done() { return count >= target; }

    @Override
    protected void onMeasure(int w, int h) {
        int width = MeasureSpec.getSize(w);
        int height = MeasureSpec.getSize(h);
        if (MeasureSpec.getMode(h) == MeasureSpec.UNSPECIFIED || height == 0) height = width;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        float label = 34 * density; // kadran yazıları için pay
        radius = Math.min(w / 2f - label, (h - label) / 2f) - 4 * density;
        radius = Math.max(radius, 40 * density);
        cx = w / 2f;
        cy = h / 2f + label * 0.15f;
        buttonR = radius * 0.70f;
    }

    private int color(int attr) { return Ui.color(getContext(), attr); }

    @Override
    protected void onDraw(Canvas c) {
        if (radius <= 0) onSizeChanged(getWidth(), getHeight(), 0, 0);
        float stroke = Math.max(10 * density, radius * 0.11f);
        float arcR = radius - stroke / 2f;
        rect.set(cx - arcR, cy - arcR, cx + arcR, cy + arcR);

        // Kadran zemini
        track.setStrokeWidth(stroke);
        track.setColor(color(R.attr.colAccentSoft));
        c.drawArc(rect, START, SWEEP, false, track);
        track.setStrokeWidth(2 * density);
        track.setColor(color(R.attr.colBorder));
        float outer = radius + 1 * density, inner = radius - stroke - 1 * density;
        rect.set(cx - outer, cy - outer, cx + outer, cy + outer);
        c.drawArc(rect, START, SWEEP, false, track);
        rect.set(cx - inner, cy - inner, cx + inner, cy + inner);
        c.drawArc(rect, START, SWEEP, false, track);

        // Çentikler (onda bir)
        tick.setColor(color(R.attr.colTextPast));
        tick.setStrokeWidth(2 * density);
        for (int i = 0; i <= 10; i++) {
            double a = Math.toRadians(START + SWEEP * i / 10f);
            float r0 = radius + 4 * density, r1 = radius + (i % 5 == 0 ? 12 : 8) * density;
            c.drawLine(cx + (float) Math.cos(a) * r0, cy + (float) Math.sin(a) * r0,
                    cx + (float) Math.cos(a) * r1, cy + (float) Math.sin(a) * r1, tick);
        }

        // Dolu kısım
        float f = fraction();
        rect.set(cx - arcR, cy - arcR, cx + arcR, cy + arcR);
        progress.setStrokeWidth(stroke);
        progress.setColor(color(done() ? R.attr.colAccentDark : R.attr.colAccent));
        if (f > 0) c.drawArc(rect, START, Math.max(0.5f, SWEEP * f), false, progress);
        // İlerleme ucundaki yuvarlak işaret
        double ea = Math.toRadians(START + SWEEP * f);
        float kx = cx + (float) Math.cos(ea) * arcR, ky = cy + (float) Math.sin(ea) * arcR;
        fill.setColor(color(R.attr.colAccentDark));
        c.drawCircle(kx, ky, stroke * 0.62f, fill);
        fill.setColor(color(R.attr.colOnAccent));
        c.drawCircle(kx, ky, stroke * 0.28f, fill);

        // Başlangıç ("0") ve hedef sayısı
        text.setColor(color(R.attr.colTextPast));
        text.setTextSize(16 * density);
        labelAt(c, "0", START, radius + 24 * density);
        text.setColor(color(R.attr.colAccentText));
        text.setTextSize(22 * density);
        labelAt(c, String.valueOf(target), START + SWEEP, radius + 26 * density);

        // Ortadaki büyük düğme
        fill.setColor(color(pressed ? R.attr.colAccentDark : R.attr.colAccent));
        c.drawCircle(cx, cy, buttonR, fill);
        track.setStrokeWidth(3 * density);
        track.setColor(color(R.attr.colAccentDark));
        c.drawCircle(cx, cy, buttonR, track);

        String num = String.valueOf(count);
        text.setColor(color(R.attr.colOnAccent));
        float size = buttonR * 0.62f;
        text.setTextSize(size);
        float w = text.measureText(num);
        if (w > buttonR * 1.55f) {
            size *= buttonR * 1.55f / w;
            text.setTextSize(size);
        }
        c.drawText(num, cx, cy + size * 0.30f, text);
        text.setTextSize(Math.max(14 * density, buttonR * 0.14f));
        c.drawText(done() ? "Hedef tamam ✓" : "Dokunun", cx, cy + size * 0.30f + buttonR * 0.34f, text);
    }

    private void labelAt(Canvas c, String s, float angle, float r) {
        double a = Math.toRadians(angle);
        float x = cx + (float) Math.cos(a) * r;
        float y = cy + (float) Math.sin(a) * r + text.getTextSize() * 0.35f;
        c.drawText(s, x, y, text);
    }

    private boolean inButton(float x, float y) {
        float dx = x - cx, dy = y - cy;
        return dx * dx + dy * dy <= buttonR * buttonR * 1.1f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!inButton(e.getX(), e.getY())) return false;
                pressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (pressed) {
                    pressed = false;
                    invalidate();
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                invalidate();
                return true;
            default:
                return pressed;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
