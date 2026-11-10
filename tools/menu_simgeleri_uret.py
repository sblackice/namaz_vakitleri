#!/usr/bin/env python3
"""Alt menünün 3 boyutlu görünümlü simgelerini çizer (Python + Pillow + numpy).

Her simge, üst üste konan parçalardan oluşur. Her parçanın kenarları yuvarlatılıp
sol üstten gelen bir ışıkla aydınlatılır (kabartma), altına yumuşak gölge düşürülür.
Hazır resim kullanılmaz; her şey bu dosyadaki kodla çizilir.

Kullanım: python3 tools/menu_simgeleri_uret.py
Çıktı:    app/src/main/res/drawable-nodpi/tab3d_*.png
"""
import math
import os

import numpy as np
from PIL import Image, ImageDraw

S = 768          # çizim boyutu
OUT = 192        # kaydedilen boyut
HERE = os.path.dirname(os.path.abspath(__file__))
DEST = os.path.join(HERE, "..", "app", "src", "main", "res", "drawable-nodpi")

EMERALD = (26, 138, 88)
EMERALD_DARK = (14, 92, 58)
GOLD = (236, 180, 52)
GOLD_DARK = (190, 132, 24)
CREAM = (250, 243, 224)
INK = (38, 40, 44)
RED = (196, 52, 44)


def blur(a, sigma):
    """Ayrılabilir Gauss bulanıklığı."""
    if sigma <= 0:
        return a
    r = max(1, int(sigma * 3))
    x = np.arange(-r, r + 1)
    k = np.exp(-(x * x) / (2.0 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)), mode="edge")
    a = np.apply_along_axis(lambda m: np.convolve(m, k, mode="valid"), 0, p)
    p = np.pad(a, ((0, 0), (r, r)), mode="edge")
    return np.apply_along_axis(lambda m: np.convolve(m, k, mode="valid"), 1, p)


def mask(draw_fn):
    """draw_fn(ImageDraw) ile beyaz çizilen şeklin 0..1 maskesi."""
    im = Image.new("L", (S, S), 0)
    draw_fn(ImageDraw.Draw(im))
    return np.asarray(im, dtype=np.float64) / 255.0


class Canvas:
    def __init__(self):
        self.rgb = np.zeros((S, S, 3))
        self.a = np.zeros((S, S))

    def over(self, color, alpha):
        """color: (3,) veya (S,S,3); alpha: (S,S)."""
        color = np.asarray(color, dtype=np.float64)
        if color.ndim == 1:
            color = np.broadcast_to(color, (S, S, 3))
        out_a = alpha + self.a * (1 - alpha)
        safe = np.where(out_a > 1e-6, out_a, 1)
        self.rgb = (color * alpha[..., None] + self.rgb * (self.a * (1 - alpha))[..., None]) / safe[..., None]
        self.a = out_a

    def shadow(self, m, dx=10, dy=18, sigma=16, strength=0.38):
        sh = blur(np.roll(np.roll(m, dy, axis=0), dx, axis=1), sigma) * strength
        self.over((0, 0, 0), sh)

    def part(self, m, color, bevel=14, depth=1.0, gloss=0.55, shadow=True, flat=False):
        """Kabartmalı (3B görünümlü) bir parça ekler."""
        if shadow:
            self.shadow(m)
        base = np.asarray(color, dtype=np.float64) / 255.0
        if flat:
            self.over(np.broadcast_to(base, (S, S, 3)), m)
            return
        h = blur(m, bevel)
        gy, gx = np.gradient(h)
        scale = bevel * 2.2 * depth
        nx, ny, nz = -gx * scale, -gy * scale, np.ones_like(h)
        ln = np.sqrt(nx * nx + ny * ny + nz * nz)
        nx, ny, nz = nx / ln, ny / ln, nz / ln
        lx, ly, lz = -0.45, -0.62, 0.64          # ışık: sol üstten
        diff = np.clip(nx * lx + ny * ly + nz * lz, 0, 1)
        # Parlama (yansıma)
        hx, hy, hz = lx, ly, lz + 1.0
        hl = math.sqrt(hx * hx + hy * hy + hz * hz)
        spec = np.clip(nx * hx / hl + ny * hy / hl + nz * hz / hl, 0, 1) ** 26
        ys = np.linspace(0.10, -0.12, S)[:, None]    # yukarıdan aşağı hafif koyulaşma
        light = 0.50 + 0.75 * diff + ys
        col = base[None, None, :] * light[..., None] + gloss * spec[..., None]
        self.over(np.clip(col, 0, 1), m)

    def save(self, name):
        rgba = np.dstack([np.clip(self.rgb, 0, 1), np.clip(self.a, 0, 1)])
        im = Image.fromarray((rgba * 255 + 0.5).astype(np.uint8), "RGBA")
        im = im.resize((OUT, OUT), Image.LANCZOS)
        os.makedirs(DEST, exist_ok=True)
        path = os.path.join(DEST, name + ".png")
        im.save(path, optimize=True)
        print(name, os.path.getsize(path), "bayt")


def poly(points):
    return mask(lambda d: d.polygon(points, fill=255))


def rrect(box, r):
    return mask(lambda d: d.rounded_rectangle(box, radius=r, fill=255))


def ellipse(box):
    return mask(lambda d: d.ellipse(box, fill=255))


def crescent(cx, cy, r, inner=0.80, shift=0.34, angle=-35):
    """Hilal: büyük daireden kaydırılmış küçük daire çıkarılır."""
    big = ellipse((cx - r, cy - r, cx + r, cy + r))
    a = math.radians(angle)
    ox, oy = cx + r * shift * math.cos(a), cy + r * shift * math.sin(a)
    ri = r * inner
    small = ellipse((ox - ri, oy - ri, ox + ri, oy + ri))
    return np.clip(big - small, 0, 1)


def star(cx, cy, r, points=5, inner=0.42, rot=-90):
    pts = []
    for i in range(points * 2):
        rr = r if i % 2 == 0 else r * inner
        a = math.radians(rot + i * 180.0 / points)
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    return poly(pts)


def home():
    c = Canvas()
    # Baca
    c.part(rrect((500, 150, 580, 330), 12), EMERALD_DARK, bevel=8)
    # Gövde
    c.part(rrect((176, 330, 592, 660), 34), CREAM, bevel=16)
    # Çatı
    c.part(poly([(384, 96), (690, 372), (640, 420), (384, 196), (128, 420), (78, 372)]), EMERALD, bevel=12)
    c.part(poly([(384, 196), (640, 420), (128, 420)]), EMERALD, bevel=12, shadow=False)
    # Kapı (kemerli)
    door = np.clip(rrect((320, 500, 448, 664), 6) + ellipse((320, 436, 448, 564)), 0, 1)
    c.part(door, GOLD, bevel=9, shadow=False)
    # Pencere
    c.part(ellipse((470, 440, 548, 518)), EMERALD_DARK, bevel=6, shadow=False)
    c.save("tab3d_home")


def mosque():
    c = Canvas()
    # Minare
    c.part(rrect((560, 250, 632, 668), 10), CREAM, bevel=10)
    c.part(rrect((544, 330, 648, 366), 10), GOLD, bevel=7, shadow=False)
    c.part(poly([(596, 120), (640, 258), (552, 258)]), EMERALD, bevel=8)
    # Gövde
    c.part(rrect((112, 452, 508, 668), 26), CREAM, bevel=14)
    # Kubbe
    dome = mask(lambda d: d.pieslice((140, 250, 480, 664), 180, 360, fill=255))
    c.part(dome, EMERALD, bevel=22, depth=1.3)
    # Kasnak
    c.part(rrect((120, 440, 500, 478), 14), GOLD, bevel=8)
    # Alem (hilal)
    c.part(rrect((302, 196, 318, 262), 6), GOLD_DARK, bevel=4, shadow=False)
    c.part(crescent(310, 160, 44, inner=0.78, shift=0.36, angle=-90), GOLD, bevel=6)
    # Kapı
    door = np.clip(rrect((262, 560, 358, 670), 4) + ellipse((262, 512, 358, 608)), 0, 1)
    c.part(door, EMERALD_DARK, bevel=8, shadow=False)
    c.save("tab3d_adhan")


def days():
    c = Canvas()
    c.part(crescent(350, 400, 270, inner=0.82, shift=0.30, angle=-28), GOLD, bevel=24, depth=1.4)
    c.part(star(520, 300, 118), (255, 214, 110), bevel=12)
    c.part(star(610, 520, 46), GOLD, bevel=6)
    c.save("tab3d_days")


def qibla():
    """Kâbe: eşölçülü (izometrik) küp, altın kuşak ve kapı."""
    c = Canvas()
    cx, top, w, hgt, dy = 384, 150, 250, 300, 118
    t = (cx, top)                      # üst yüzün arka köşesi
    l = (cx - w, top + dy)
    r = (cx + w, top + dy)
    f = (cx, top + 2 * dy)             # üst yüzün ön köşesi
    lb, rb, fb = (l[0], l[1] + hgt), (r[0], r[1] + hgt), (f[0], f[1] + hgt)
    whole = poly([t, r, rb, fb, lb, l])
    c.shadow(whole, dx=8, dy=22, sigma=20, strength=0.45)
    c.part(poly([l, f, fb, lb]), (58, 60, 66), bevel=10, shadow=False, gloss=0.25)     # sol yüz (aydınlık)
    c.part(poly([f, r, rb, fb]), (24, 25, 29), bevel=10, shadow=False, gloss=0.15)     # sağ yüz (gölge)
    c.part(poly([t, r, f, l]), (96, 99, 106), bevel=10, shadow=False, gloss=0.3)       # üst yüz

    def band(y0, y1):
        left = poly([(l[0], l[1] + y0), (f[0], f[1] + y0), (f[0], f[1] + y1), (l[0], l[1] + y1)])
        right = poly([(f[0], f[1] + y0), (r[0], r[1] + y0), (r[0], r[1] + y1), (f[0], f[1] + y1)])
        c.part(left, GOLD, bevel=6, shadow=False)
        c.part(right, GOLD_DARK, bevel=6, shadow=False, gloss=0.3)

    band(52, 104)
    # Kapı (sağ yüzde)
    k = dy / float(w)
    x0, x1 = cx + 96, cx + 176
    y_base = lambda x: f[1] + hgt - (x - cx) * k
    c.part(poly([(x0, y_base(x0) - 150), (x1, y_base(x1) - 150), (x1, y_base(x1) - 4), (x0, y_base(x0) - 4)]),
           GOLD, bevel=6, shadow=False)
    c.save("tab3d_qibla")


def duas():
    """Açık kitap (Kur'an): yeşil kapak, krem sayfalar, altın ayraç."""
    c = Canvas()
    cover = poly([(84, 250), (384, 300), (684, 250), (684, 612), (384, 664), (84, 612)])
    c.part(cover, EMERALD, bevel=16)
    left = poly([(120, 214), (384, 268), (384, 628), (120, 574)])
    right = poly([(384, 268), (648, 214), (648, 574), (384, 628)])
    c.part(left, CREAM, bevel=20, depth=1.2)
    c.part(right, CREAM, bevel=20, depth=1.2)
    # Satırlar
    for i in range(4):
        y = 316 + i * 62
        c.part(poly([(164, y), (344, y + 37), (344, y + 55), (164, y + 18)]), (196, 184, 150),
               flat=True, shadow=False)
        c.part(poly([(424, y + 37), (604, y), (604, y + 18), (424, y + 55)]), (196, 184, 150),
               flat=True, shadow=False)
    # Ayraç
    c.part(poly([(356, 262), (412, 262), (412, 730), (384, 700), (356, 730)]), GOLD, bevel=8)
    c.save("tab3d_duas")


if __name__ == "__main__":
    home()
    mosque()
    days()
    qibla()
    duas()
