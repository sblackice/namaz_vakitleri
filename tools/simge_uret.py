"""Uygulama simgesini kullanıcının verdiği logodan üretir.

Kullanım:
  python3 -I tools/simge_uret.py docs/logo/uygulama-logosu-kaynak.jpg app/src/main/res
Üretilenler:
  mipmap-*/ic_launcher.png      Android 7.x için köşeleri yuvarlatılmış kare simge (saydam köşeler)
  mipmap-*/ic_launcher_fg.png   Android 8+ uyarlanabilir simgenin ön katmanı (logo, güvenli alana sığdırılmış)
  Arka plan rengi values/colors.xml içindeki "navy" (#101F39) rengidir.
"""
import os
import sys

from PIL import Image, ImageDraw

src, res = sys.argv[1], sys.argv[2]
im = Image.open(src).convert('RGB')
W = im.width
NAVY = (16, 31, 57)
DENS = {'mdpi': 1, 'hdpi': 1.5, 'xhdpi': 2, 'xxhdpi': 3, 'xxxhdpi': 4}

# 1) Eski tip simge: logonun kendi yuvarlatılmış köşeleri saydam yapılır (köşe yarıçapı ≈ %17,4).
radius = int(W * 0.174)
mask = Image.new('L', (W * 2, W * 2), 0)
ImageDraw.Draw(mask).rounded_rectangle([0, 0, W * 2 - 1, W * 2 - 1], radius=radius * 2, fill=255)
mask = mask.resize((W, W), Image.LANCZOS)
legacy = im.convert('RGBA')
legacy.putalpha(mask)

# 2) Uyarlanabilir simge ön katmanı: çizim (kenarlardan %9 içeride) 108dp tuvalin ~%60'ına sığdırılır.
art = im.crop((150, 150, W - 150, W - 150))           # yuvarlak köşelerin içinde kalan alan
canvas = int(art.width / 0.60)
fg = Image.new('RGB', (canvas, canvas), NAVY)
fg.paste(art, ((canvas - art.width) // 2, (canvas - art.height) // 2))

for name, k in DENS.items():
    d = os.path.join(res, 'mipmap-' + name)
    os.makedirs(d, exist_ok=True)
    legacy.resize((round(48 * k), round(48 * k)), Image.LANCZOS).save(os.path.join(d, 'ic_launcher.png'), optimize=True)
    fg.resize((round(108 * k), round(108 * k)), Image.LANCZOS).save(os.path.join(d, 'ic_launcher_fg.png'), optimize=True)
print('simgeler üretildi:', res)
