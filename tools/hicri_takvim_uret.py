"""Diyanet'in Hicrî ay başı ölçütüyle Hicrî takvim tablosu üretir ve Diyanet'in yayımladığı
tarihlerle karşılaştırır.

Ölçüt (Diyanet İşleri Başkanlığı; 1978 İstanbul Hilal Konferansı, 2016 İstanbul Uluslararası
Hicrî Takvim Birliği Kongresi'nde yeniden teyit edildi):
  Kavuşumdan (içtima) sonra, yeryüzünün herhangi bir yerinde güneş battığı anda
  - Ay'ın ufuktan yüksekliği en az 5°,
  - Güneş ile Ay arasındaki açı (elongasyon) en az 8° ise
  ertesi gün yeni Hicrî ay başlar.
  Kaynak: vakithesaplama.diyanet.gov.tr (icerik=101) ve kurul.diyanet.gov.tr 2026 Ramazan açıklaması.

Ayrıntılar (resmî metinde yazmıyor; aşağıdaki Diyanet tarihlerinin HEPSİNİ veren yorum seçildi):
  - Gün, Greenwich'e (UTC) göre sayılır: şart, 24:00 UTC'den önce bir yerde sağlanmalıdır.
  - İstisna: şart 24:00 UTC'den sonra Amerika anakarasında sağlanıyor ve kavuşum Wellington'da
    (Yeni Zelanda) imsaktan önce gerçekleşmişse ay yine ertesi gün başlar. Bu kural, Diyanet Din
    İşleri Yüksek Kurulu'nun 18 Şubat 2026 tarihli 1447 Ramazan açıklamasında yazılıdır.
  - Yükseklik ve elongasyon yer merkezlidir (geocentric), kırılma (refraksiyon) yoktur.
  Diğer yorumlar (yüzey merkezli ölçüm, istisnasız kural vb.) Diyanet tarihlerinden 1-7 tanesini
  bir gün kaydırıyor; bu yüzden seçilmedi.

Sonuç (Ekim 2026): Diyanet'in 38 ay başından 37'si hesapla aynı çıktı. Farklı olan tek ay
1 Ramazan 1447'dir: hesapta hilal şartı yalnızca Alaska Yarımadası'nda 0,08° farkla sağlanıyor,
Diyanet ise o akşamı kabul etmedi (19 Şubat 2026). Tabloya Diyanet'in yayımladığı tarihler
aynen yazılır; hesap yalnızca listelerde geçmeyen aylar (Safer, Rebiülahir, Cemaziyelevvel,
Cemaziyelahir, Zilkade) ve 2031-2032 için kullanılır.

Kullanım:
  pip install ephem==4.2.1 shapely==2.2.0
  npm pack world-atlas@2.0.2 && tar -xzf world-atlas-2.0.2.tgz   # package/land-50m.json
  python3 -I tools/hicri_takvim_uret.py package/land-50m.json --kontrol   # Diyanet ile karşılaştır
  python3 -I tools/hicri_takvim_uret.py package/land-50m.json > app/src/main/java/tr/kolay/namazvakti/HicriTablo.java

Amerika anakarası: Natural Earth 1:50m kara verisi (kamu malı), npm "world-atlas" 2.0.2 paketi (ISC).
"""
import datetime as dt
import math
import sys

import ephem

ALT_MIN = 5.0
ELONG_MIN = 8.0
ILK_YIL, SON_YIL = 2025, 2032
AYLAR = ["Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir",
         "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"]

# Diyanet'in dini günler listelerinden çıkan ay başları (Hicrî yıl, ay 1-12, miladi tarih).
# Kaynak: vakithesaplama.diyanet.gov.tr/icerik.php?icerik=153 (2026), 154 (2027), 185 (2028),
# 186 (2029), 187 (2030). Bir ayın başka bir günü verildiyse (ör. 14 Şaban), ay başı oradan
# geri sayılarak bulundu.
DIYANET = [
    (1447, 7, dt.date(2025, 12, 21)),   # 26 Recep 1447 = 15 Ocak 2026 (Miraç)
    (1447, 8, dt.date(2026, 1, 20)),    # 14 Şaban 1447 = 2 Şubat 2026 (Berat)
    (1447, 9, dt.date(2026, 2, 19)),    # 1 Ramazan 1447
    (1447, 10, dt.date(2026, 3, 20)),   # 1 Şevval 1447
    (1447, 12, dt.date(2026, 5, 18)),   # 10 Zilhicce 1447 = 27 Mayıs 2026
    (1448, 1, dt.date(2026, 6, 16)),    # 1 Muharrem 1448
    (1448, 3, dt.date(2026, 8, 14)),    # 11 Rebiülevvel 1448 = 24 Ağustos 2026 (Mevlid)
    (1448, 7, dt.date(2026, 12, 10)),   # 1 Recep 1448 = Üç Ayların Başlangıcı
    (1448, 8, dt.date(2027, 1, 9)),     # 14 Şaban 1448 = 22 Ocak 2027 (Berat)
    (1448, 9, dt.date(2027, 2, 8)),     # 1 Ramazan 1448
    (1448, 10, dt.date(2027, 3, 9)),    # 1 Şevval 1448
    (1448, 12, dt.date(2027, 5, 7)),    # 10 Zilhicce 1448 = 16 Mayıs 2027
    (1449, 1, dt.date(2027, 6, 6)),     # 1 Muharrem 1449
    (1449, 3, dt.date(2027, 8, 3)),     # 11 Rebiülevvel 1449 = 13 Ağustos 2027
    (1449, 7, dt.date(2027, 11, 29)),   # 1 Recep 1449
    (1449, 8, dt.date(2027, 12, 29)),   # 14 Şaban 1449 = 11 Ocak 2028
    (1449, 9, dt.date(2028, 1, 28)),    # 1 Ramazan 1449
    (1449, 10, dt.date(2028, 2, 26)),   # 1 Şevval 1449
    (1449, 12, dt.date(2028, 4, 26)),   # 10 Zilhicce 1449 = 5 Mayıs 2028
    (1450, 1, dt.date(2028, 5, 25)),    # 1 Muharrem 1450
    (1450, 3, dt.date(2028, 7, 23)),    # 11 Rebiülevvel 1450 = 2 Ağustos 2028
    (1450, 7, dt.date(2028, 11, 18)),   # 1 Recep 1450
    (1450, 8, dt.date(2028, 12, 17)),   # 14 Şaban 1450 = 30 Aralık 2028
    (1450, 9, dt.date(2029, 1, 16)),    # 1 Ramazan 1450
    (1450, 10, dt.date(2029, 2, 14)),   # 1 Şevval 1450
    (1450, 12, dt.date(2029, 4, 15)),   # 10 Zilhicce 1450 = 24 Nisan 2029
    (1451, 1, dt.date(2029, 5, 14)),    # 1 Muharrem 1451
    (1451, 3, dt.date(2029, 7, 13)),    # 11 Rebiülevvel 1451 = 23 Temmuz 2029
    (1451, 7, dt.date(2029, 11, 7)),    # 1 Recep 1451
    (1451, 8, dt.date(2029, 12, 7)),    # 14 Şaban 1451 = 20 Aralık 2029
    (1451, 9, dt.date(2030, 1, 5)),     # 1 Ramazan 1451
    (1451, 10, dt.date(2030, 2, 4)),    # 1 Şevval 1451
    (1451, 12, dt.date(2030, 4, 4)),    # 10 Zilhicce 1451 = 13 Nisan 2030
    (1452, 1, dt.date(2030, 5, 3)),     # 1 Muharrem 1452
    (1452, 3, dt.date(2030, 7, 2)),     # 11 Rebiülevvel 1452 = 12 Temmuz 2030
    (1452, 7, dt.date(2030, 10, 28)),   # 1 Recep 1452
    (1452, 8, dt.date(2030, 11, 26)),   # 14 Şaban 1452 = 9 Aralık 2030
    (1452, 9, dt.date(2030, 12, 26)),   # 1 Ramazan 1452
]

LATS = [x * 2.0 for x in range(-30, 36)]       # 60°G … 70°K
LONS = [x * 2.0 for x in range(-90, 90)]       # 180°B … 178°D


def load_americas_mainland(path):
    """Natural Earth 1:50m kara verisinden (npm world-atlas, land-50m.json) Amerika anakarasını
    (Kuzey + Orta + Güney Amerika'yı içine alan en büyük kara parçası; adalar hariç) yükler ve
    üzerindeki 1°'lik ızgara noktalarını döndürür."""
    import json
    from shapely.geometry import Point, Polygon
    from shapely.prepared import prep
    t = json.load(open(path, encoding='utf8'))
    sc, tr = t['transform']['scale'], t['transform']['translate']
    arcs = []
    for a in t['arcs']:
        x = y = 0
        pts = []
        for dx, dy in a:
            x += dx
            y += dy
            pts.append((x * sc[0] + tr[0], y * sc[1] + tr[1]))
        arcs.append(pts)

    def ring(idx):
        pts = []
        for i in idx:
            a = arcs[i] if i >= 0 else arcs[~i][::-1]
            pts.extend(a if not pts else a[1:])
        return pts

    geom = t['objects']['land']['geometries'][0]
    polys = [Polygon(ring(p[0]), [ring(h) for h in p[1:]]) for p in geom['arcs']]
    west = [p for p in polys if p.centroid.x < -30]
    mainland = prep(max(west, key=lambda p: p.area))
    return [(float(lat), float(lon)) for lon in range(-170, -33) for lat in range(-56, 73)
            if mainland.contains(Point(lon, lat))]


def geo_alt(obs, body):
    """Cismin yer merkezli (paralakssız) yüksekliği, derece."""
    ha = obs.sidereal_time() - body.g_ra
    s = (math.sin(obs.lat) * math.sin(body.g_dec)
         + math.cos(obs.lat) * math.cos(body.g_dec) * math.cos(ha))
    return math.degrees(math.asin(s))


def nz_fajr(day):
    """Wellington'da (Yeni Zelanda) day+1 yerel günü için imsak (güneş 18° altında), UTC."""
    obs = ephem.Observer()
    obs.lat, obs.lon = '-41.29', '174.78'
    obs.pressure = 0
    obs.horizon = '-18'
    obs.date = ephem.Date(dt.datetime(day.year, day.month, day.day, 12))  # Wellington gece yarısı
    return obs.next_rising(ephem.Sun(), use_center=True)


def _margin(obs, sun, moon, lat, lon, local_noon, conj, lo, hi):
    """Güneş batışı [lo, hi) aralığındaysa hilal şartının payı: min(yükseklik-5°, elongasyon-8°).
    Pay ≥ 0 ise şart sağlanır. Batış aralıkta değilse None."""
    obs.lat, obs.lon = str(lat), str(lon)
    obs.date = local_noon
    try:
        ss = obs.next_setting(sun)
    except (ephem.AlwaysUpError, ephem.NeverUpError):
        return None
    if ss <= conj or ss < lo or ss >= hi:
        return None
    obs.date = ss
    moon.compute(obs)
    sun.compute(obs)
    alt = geo_alt(obs, moon)
    el = math.degrees(ephem.separation((moon.g_ra, moon.g_dec), (sun.g_ra, sun.g_dec)))
    return min(alt - ALT_MIN, el - ELONG_MIN)


def best_margin(day, conj, americas):
    """day (UTC takvim günü) akşamı için en iyi hilal payı. Pay ≥ 0 ise ertesi gün ay başıdır."""
    d0 = dt.datetime(day.year, day.month, day.day)
    end = ephem.Date(d0 + dt.timedelta(days=1))
    obs = ephem.Observer()
    obs.pressure = 0          # kırılma yok
    obs.horizon = '-0:50'     # güneşin üst kenarı ufukta
    sun, moon = ephem.Sun(), ephem.Moon()
    best = -99.0

    def noon(lon):
        return ephem.Date(d0 + dt.timedelta(hours=12 - lon / 15.0))

    # 1) Yeryüzünün herhangi bir yerinde, 24:00 UTC'den önce
    for lon in LONS:
        n = noon(lon)
        for lat in LATS:
            m = _margin(obs, sun, moon, lat, lon, n, conj, ephem.Date(0), end)
            if m is not None and m > best:
                best = m
    # 2) İstisna: 24:00 UTC'den sonra Amerika anakarasında, kavuşum Wellington imsakından önce
    if conj < nz_fajr(day):
        for lat, lon in americas:
            m = _margin(obs, sun, moon, lat, lon, noon(lon), conj, end, ephem.Date(end + 1))
            if m is not None and m > best:
                best = m
    return best


def month_starts(americas):
    """[(kavuşum, ay başı tarihi, sınırda mı)] listesi. Karar günündeki pay veya bir önceki
    günün payı 0,25°'den küçükse "sınırda" sayılır (Diyanet'ten 1 gün farklı olabilir)."""
    out = []
    d = ephem.Date(dt.datetime(ILK_YIL, 1, 1))
    while True:
        conj = ephem.next_new_moon(d)
        cdt = conj.datetime()
        if cdt.year > SON_YIL:
            break
        day = cdt.date()
        prev = -99.0
        for _ in range(3):
            m = best_margin(day, conj, americas)
            if m >= 0:
                break
            prev = m
            day += dt.timedelta(days=1)
        out.append((cdt, day + dt.timedelta(days=1), m < 0.25 or prev > -0.25))
        d = ephem.Date(conj + 20)
    return out


def number_months(starts):
    """Ay başlarına Hicrî yıl/ay numarası verir (1 Recep 1448 = 10 Aralık 2026 çapasıyla)."""
    anchor = dt.date(2026, 12, 10)
    idx = [s for _, s, _ in starts].index(anchor)
    res = []
    for i, (_, s, edge) in enumerate(starts):
        k = (1448 * 12 + 6) + (i - idx)  # 0 tabanlı ay sayacı
        res.append([k // 12, k % 12 + 1, s, edge, 'hesap'])
    return res


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    land = args[0] if args else 'package/land-50m.json'
    table = number_months(month_starts(load_americas_mainland(land)))
    rows = {(r[0], r[1]): r for r in table}
    bad = 0
    for y, m, s in DIYANET:
        r = rows[(y, m)]
        if r[2] != s:
            bad += 1
            print('FARKLI %d %-14s Diyanet: %s  hesap: %s%s'
                  % (y, AYLAR[m - 1], s, r[2], ' (sınırda)' if r[3] else ''), file=sys.stderr)
        r[2], r[4] = s, 'Diyanet'   # yayımlanmış Diyanet tarihi her zaman esastır
    for i in range(1, len(table)):
        n = (table[i][2] - table[i - 1][2]).days
        if n not in (29, 30):
            raise SystemExit('ay uzunluğu hatalı: %s %s' % (table[i - 1], n))
    print('%d Diyanet ay başından %d tanesi hesaptan farklı; Diyanet tarihi kullanıldı.'
          % (len(DIYANET), bad), file=sys.stderr)
    if '--kontrol' in sys.argv:
        for y, m, s, edge, src in table:
            print('1 %-14s %d = %s  %s%s' % (AYLAR[m - 1], y, s, src, ' (sınırda)' if edge and src == 'hesap' else ''))
        return
    print_java(table, bad)


def print_java(table, bad):
    print('package tr.kolay.namazvakti;')
    print()
    print('/**')
    print(' * Diyanet ölçütüyle hesaplanmış Hicrî ay başları (%d-%d). OTOMATİK ÜRETİLDİ:' % (ILK_YIL, SON_YIL))
    print(' * tools/hicri_takvim_uret.py (elle değiştirmeyin, betiği yeniden çalıştırın).')
    print(' * Diyanet\'in dini günler listelerinde yayımlanan %d ay başı aynen yazıldı ("Diyanet");' % len(DIYANET))
    print(' * hesap bunların %d tanesini doğru buldu. Diğer aylar hesaptır ("hesap"); "sınırda"' % (len(DIYANET) - bad))
    print(' * yazanlarda hilal şartı çok az farkla sağlandığı için Diyanet\'ten 1 gün farklı olabilir.')
    print(' * Her satır: Hicrî yıl, ay (1-12), ayın 1. gününün miladi tarihi (yıl, ay, gün).')
    print(' */')
    print('final class HicriTablo {')
    print('    private HicriTablo() {}')
    print()
    print('    static final int[][] AY_BASLARI = {')
    for y, m, s, edge, src in table:
        note = src + (', sınırda' if edge and src == 'hesap' else '')
        print('        {%d, %d, %d, %d, %d}, // %s %d (%s)' % (y, m, s.year, s.month, s.day, AYLAR[m - 1], y, note))
    print('    };')
    print('}')


if __name__ == '__main__':
    main()
