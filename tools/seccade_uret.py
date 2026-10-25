"""Kıble pusulasındaki seccade ve Kâbe resimlerini üretir (tamamen bu betikle çizilir, telif yoktur).

Kullanım:
  python3 -I tools/seccade_uret.py app/src/main/res/drawable-nodpi
Çıktı: qibla_rug.png (seccade, üstte mihrap kemeri; resmin üstü Kıble yönüdür) ve qibla_kaaba.png.
"""
import math
import os
import random
import sys

import numpy as np
from PIL import Image, ImageDraw

OUT = sys.argv[1]
S = 2  # büyük çizip küçülterek kenarları yumuşatırız
random.seed(7)

RED = (139, 26, 31)
RED_DARK = (104, 17, 24)
NAVY = (30, 35, 80)
NAVY_DARK = (20, 22, 52)
CREAM = (232, 215, 176)
GOLD = (196, 154, 85)
BLUE = (96, 128, 168)
OUTLINE = (42, 26, 18)
FRINGE = (229, 214, 184)


def flower(d, cx, cy, r, petal, center, n=8):
    for k in range(n):
        a = 2 * math.pi * k / n
        px, py = cx + r * 0.58 * math.cos(a), cy + r * 0.58 * math.sin(a)
        pr = r * 0.36
        d.ellipse([px - pr, py - pr, px + pr, py + pr], fill=petal)
    cr = r * 0.34
    d.ellipse([cx - cr, cy - cr, cx + cr, cy + cr], fill=center)


def diamond(d, cx, cy, hw, hh, fill, outline=None, width=0):
    pts = [(cx, cy - hh), (cx + hw, cy), (cx, cy + hh), (cx - hw, cy)]
    d.polygon(pts, fill=fill)
    if outline:
        d.line(pts + [pts[0]], fill=outline, width=width, joint='curve')


def band_motifs(d, x0, y0, x1, y1, step, draw_one):
    """Dikdörtgen bir bordür bandının ortasından geçen hat boyunca motif yerleştirir."""
    for x in np.arange(x0, x1 + 0.1, step):
        draw_one(x, y0)
        draw_one(x, y1)
    for y in np.arange(y0 + step, y1 - step + 0.1, step):
        draw_one(x0, y)
        draw_one(x1, y)


def bezier(p0, p1, p2, p3, n=40):
    pts = []
    for i in range(n + 1):
        t = i / n
        u = 1 - t
        x = u ** 3 * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t ** 3 * p3[0]
        y = u ** 3 * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t ** 3 * p3[1]
        pts.append((x, y))
    return pts


def rug():
    W, H, F = 560 * S, 860 * S, 34 * S
    img = Image.new('RGBA', (W, H + 2 * F), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Püsküller (üst ve alt)
    for x in range(10 * S, W - 10 * S, 8 * S):
        for top in (True, False):
            wob = random.uniform(-2.5, 2.5) * S
            ln = F - random.uniform(0, 5) * S
            c = tuple(min(255, max(0, v + random.randint(-14, 8))) for v in FRINGE)
            if top:
                d.line([(x, F), (x + wob, F - ln)], fill=c + (255,), width=3 * S)
            else:
                d.line([(x, F + H), (x + wob, F + H + ln)], fill=c + (255,), width=3 * S)

    oy = F  # gövde başlangıcı
    d.rectangle([0, oy, W - 1, oy + H - 1], fill=OUTLINE)

    # Dış bordür: lacivert bant, çiçek ve baklava motifleri
    b1 = 64 * S
    d.rectangle([5 * S, oy + 5 * S, W - 5 * S, oy + H - 5 * S], fill=NAVY)
    mid = 5 * S + b1 / 2
    band_motifs(d, mid, oy + mid, W - mid, oy + H - mid, 62 * S,
                lambda x, y: flower(d, x, y, 19 * S, CREAM, RED))
    band_motifs(d, mid + 31 * S, oy + mid + 31 * S, W - mid - 31 * S, oy + H - mid - 31 * S, 1e9,
                lambda x, y: None)
    for x in np.arange(mid + 31 * S, W - mid - 20 * S, 62 * S):
        diamond(d, x, oy + mid, 8 * S, 12 * S, GOLD)
        diamond(d, x, oy + H - mid, 8 * S, 12 * S, GOLD)
    for y in np.arange(oy + mid + 31 * S, oy + H - mid - 20 * S, 62 * S):
        diamond(d, mid, y, 12 * S, 8 * S, GOLD)
        diamond(d, W - mid, y, 12 * S, 8 * S, GOLD)

    # İnce şeritler ve iç bordür (krem bant, kırmızı küçük motifler)
    x0, y0, x1, y1 = 5 * S + b1, oy + 5 * S + b1, W - 5 * S - b1, oy + H - 5 * S - b1
    d.rectangle([x0, y0, x1, y1], fill=GOLD)
    x0, y0, x1, y1 = x0 + 5 * S, y0 + 5 * S, x1 - 5 * S, y1 - 5 * S
    d.rectangle([x0, y0, x1, y1], fill=RED_DARK)
    x0, y0, x1, y1 = x0 + 5 * S, y0 + 5 * S, x1 - 5 * S, y1 - 5 * S
    b2 = 30 * S
    d.rectangle([x0, y0, x1, y1], fill=CREAM)
    m2 = b2 / 2
    for x in np.arange(x0 + m2, x1 - m2 + 1, 26 * S):
        diamond(d, x, y0 + m2, 7 * S, 7 * S, RED)
        diamond(d, x, y1 - m2, 7 * S, 7 * S, RED)
    for y in np.arange(y0 + m2 + 26 * S, y1 - m2 - 13 * S, 26 * S):
        diamond(d, x0 + m2, y, 7 * S, 7 * S, RED)
        diamond(d, x1 - m2, y, 7 * S, 7 * S, RED)
    x0, y0, x1, y1 = x0 + b2, y0 + b2, x1 - b2, y1 - b2
    d.rectangle([x0, y0, x1, y1], fill=NAVY_DARK)
    fx0, fy0, fx1, fy1 = x0 + 4 * S, y0 + 4 * S, x1 - 4 * S, y1 - 4 * S
    fw, fh = fx1 - fx0, fy1 - fy0
    cx = (fx0 + fx1) / 2

    # Zemin: kırmızı, düzenli küçük çiçekler
    field = Image.new('RGBA', img.size, (0, 0, 0, 0))
    fd = ImageDraw.Draw(field)
    fd.rectangle([fx0, fy0, fx1, fy1], fill=RED)
    step = 36 * S
    for j, y in enumerate(np.arange(fy0 + step / 2, fy1, step)):
        for i, x in enumerate(np.arange(fx0 + step / 2 + (step / 2 if j % 2 else 0), fx1, step)):
            if (i + j) % 2:
                flower(fd, x, y, 8 * S, CREAM, NAVY, n=4)
            else:
                diamond(fd, x, y, 5 * S, 7 * S, GOLD)
    # Mihrap kemeri: kemerin üstündeki köşeler (spandrel) krem
    base_y = fy0 + 0.40 * fh
    shoulder_y = fy0 + 0.20 * fh
    apex_y = fy0 + 0.05 * fh
    m = 18 * S
    left = bezier((fx0 + m, shoulder_y), (fx0 + m, shoulder_y - 0.10 * fh),
                  (cx - 0.20 * fw, apex_y + 0.05 * fh), (cx, apex_y))
    right = [(2 * cx - x, y) for (x, y) in reversed(left)]
    arch = [(fx0 + m, base_y)] + left + right + [(fx1 - m, base_y)]
    spandrel = Image.new('L', img.size, 0)
    sd = ImageDraw.Draw(spandrel)
    sd.rectangle([fx0, fy0, fx1, base_y], fill=255)
    sd.polygon(arch, fill=0)
    cream_layer = Image.new('RGBA', img.size, CREAM + (255,))
    field = Image.composite(cream_layer, field, spandrel)
    fd = ImageDraw.Draw(field)
    # Köşelerde mavi-lacivert süsler
    for sx in (fx0 + 0.13 * fw, fx1 - 0.13 * fw):
        flower(fd, sx, fy0 + 0.065 * fh, 22 * S, BLUE, NAVY)
        flower(fd, sx, fy0 + 0.065 * fh, 9 * S, CREAM, RED, n=6)
        diamond(fd, sx, fy0 + 0.15 * fh, 9 * S, 14 * S, NAVY)
    # Kemer çizgisi
    fd.line(arch, fill=GOLD, width=10 * S, joint='curve')
    fd.line(arch, fill=NAVY, width=4 * S, joint='curve')
    diamond(fd, cx, apex_y - 4 * S, 9 * S, 13 * S, GOLD)

    # Orta madalyon (baklava) ve sarkıtlar
    my = fy0 + 0.66 * fh
    hw, hh = 0.36 * fw, 0.22 * fh
    fd.line([(cx, my - hh - 46 * S), (cx, my + hh + 46 * S)], fill=GOLD, width=4 * S)
    diamond(fd, cx, my - hh - 46 * S, 14 * S, 20 * S, NAVY, GOLD, 3 * S)
    diamond(fd, cx, my + hh + 46 * S, 14 * S, 20 * S, NAVY, GOLD, 3 * S)
    diamond(fd, cx, my, hw, hh, NAVY, GOLD, 5 * S)
    for k in range(1, 12):  # kenarlarda krem noktalar
        t = k / 12
        for (ax, ay, bx, by) in ((cx, my - hh, cx + hw, my), (cx + hw, my, cx, my + hh),
                                 (cx, my + hh, cx - hw, my), (cx - hw, my, cx, my - hh)):
            px, py = ax + (bx - ax) * t, ay + (by - ay) * t
            fd.ellipse([px - 4 * S, py - 4 * S, px + 4 * S, py + 4 * S], fill=CREAM)
    diamond(fd, cx, my, hw * 0.78, hh * 0.78, CREAM)
    diamond(fd, cx, my, hw * 0.62, hh * 0.62, RED)
    for k in range(8):
        a = 2 * math.pi * k / 8
        flower(fd, cx + hw * 0.42 * math.cos(a), my + hh * 0.42 * math.sin(a), 8 * S, CREAM, NAVY, n=5)
    diamond(fd, cx, my, hw * 0.30, hh * 0.30, NAVY, GOLD, 3 * S)
    flower(fd, cx, my, 20 * S, CREAM, RED)

    # Alt köşelerde krem üçgenler
    for (ax, sgn) in ((fx0, 1), (fx1, -1)):
        tri = [(ax, fy1), (ax + sgn * 0.30 * fw, fy1), (ax, fy1 - 0.18 * fh)]
        fd.polygon(tri, fill=CREAM)
        flower(fd, ax + sgn * 0.08 * fw, fy1 - 0.05 * fh, 13 * S, BLUE, NAVY)
    mask = Image.new('L', img.size, 0)
    ImageDraw.Draw(mask).rectangle([fx0, fy0, fx1, fy1], fill=255)
    img.paste(field, (0, 0), mask)

    # Hafif dokuma dokusu
    arr = np.array(img).astype(np.int16)
    noise = np.random.default_rng(3).normal(0, 3, arr.shape[:2])
    body = arr[:, :, 3] > 0
    for ch in range(3):
        arr[:, :, ch] = np.where(body, np.clip(arr[:, :, ch] + noise, 0, 255), arr[:, :, ch])
    img = Image.fromarray(arr.astype(np.uint8), 'RGBA')
    return img.resize((img.width // S, img.height // S), Image.LANCZOS)


def kaaba():
    N = 240 * S
    img = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    k = N / 480.0
    P = lambda pts: [(x * k, y * k) for (x, y) in pts]
    top = P([(240, 60), (430, 125), (240, 190), (50, 125)])
    lf = P([(50, 125), (240, 190), (240, 440), (50, 375)])
    rf = P([(240, 190), (430, 125), (430, 375), (240, 440)])
    d.polygon(top, fill=(58, 58, 58))
    d.polygon(lf, fill=(24, 24, 24))
    d.polygon(rf, fill=(12, 12, 12))
    # Altın kuşak
    d.polygon(P([(50, 168), (240, 233), (240, 268), (50, 203)]), fill=(201, 151, 28))
    d.polygon(P([(240, 233), (430, 168), (430, 203), (240, 268)]), fill=(176, 130, 22))
    for i in range(1, 12):  # kuşaktaki yazı izlenimi veren küçük çizgiler
        x = 50 + i * 190 / 12
        y = 185 + (x - 50) / 190 * 65
        d.line(P([(x - 5, y - 3), (x + 5, y + 3)]), fill=(120, 86, 12), width=int(3 * k))
        x2 = 240 + i * 190 / 12
        y2 = 250 - (x2 - 240) / 190 * 65
        d.line(P([(x2 - 5, y2 + 3), (x2 + 5, y2 - 3)]), fill=(110, 78, 10), width=int(3 * k))
    # Kapı (altın)
    yb = lambda x: 375 + (x - 50) / 190 * 65 - 22
    d.polygon(P([(178, yb(178) - 120), (220, yb(220) - 120), (220, yb(220)), (178, yb(178))]),
              fill=(201, 151, 28))
    # Kenar parlaklığı
    d.line(P([(50, 125), (240, 60), (430, 125)]), fill=(110, 110, 110), width=int(4 * k))
    d.line(P([(240, 190), (240, 440)]), fill=(70, 70, 70), width=int(3 * k))
    return img.resize((N // S, N // S), Image.LANCZOS)


os.makedirs(OUT, exist_ok=True)
r = rug()
# 256 renge indirerek dosyayı küçült (saydamlık korunur)
r.quantize(256, method=Image.Quantize.FASTOCTREE).save(os.path.join(OUT, 'qibla_rug.png'), optimize=True)
kaaba().save(os.path.join(OUT, 'qibla_kaaba.png'), optimize=True)
print('seccade', r.size, 'kaydedildi:', OUT)
