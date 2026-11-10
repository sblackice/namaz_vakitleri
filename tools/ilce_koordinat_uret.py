"""Ilceler.java dosyasını üretir: Türkiye'nin 973 ilçesi ve merkez koordinatları.

Kaynaklar:
  - İlçe listesi (il başına, güncel): npm "turkey-neighbourhoods" 4.0.3 (MIT),
    src/data/districtsByCityCode.json ve cityList.json.
  - Koordinatlar: GeoNames (geonames.org, CC BY 4.0), npm "cities.json" 1.1.65 paketi
    (cities.json: nüfusu 1000'i aşan ve ilçe merkezi olan yerleşimler; admin1/admin2.json:
    il ve ilçe sınır kodları).
  - "Merkez" ilçeleri ve bulunamayan büyükşehir merkez ilçeleri için il merkezi
    (Cities.java, yine GeoNames).

Yöntem (her ilçe için, sırayla):
  1. GeoNames'te bu ilin içinde aynı adlı ilçe sınırı (admin2) bulunur.
  2. O ilçe sınırı içinde ilçeyle aynı adlı yerleşim varsa onun koordinatı alınır ("yerleşim").
  3. Yoksa ilin içinde aynı adlı yerleşim alınır ("il içi ad").
  4. Yoksa ilçe sınırı içindeki bütün yerleşimlerin ortalaması alınır ("ilçe ortası").
     İlçe sınırında hiç yerleşim kaydı yoksa (büyükşehrin içindeki ilçeler) il merkezi alınır.
  5. "Merkez" ilçeleri il merkezini kullanır.
Bulunamayan ilçe olursa betik hata verir. Her ilçenin il merkezine uzaklığı yazdırılır;
120 km'yi aşanlar ayrıca listelenir (elle kontrol için).

Kullanım:
  npm pack turkey-neighbourhoods@4.0.3 cities.json@1.1.65  (ve açın)
  python3 -I tools/ilce_koordinat_uret.py <turkey-neighbourhoods/package> <cities.json/package> \\
      app/src/main/java/tr/kolay/namazvakti/Cities.java > app/src/main/java/tr/kolay/namazvakti/Ilceler.java
"""
import json
import math
import re
import sys
import unicodedata

# GeoNames'te farklı yazılan ilçe adları (bizim ad -> GeoNames adı)
ALIAS = {
    "19mayis": "ondokuzmayis",
    "kahramankazan": "kazan",
}


# GeoNames'te yerleşim kaydı olmayan ilçeler için elle yazılmış koordinatlar (kaynağıyla).
ELLE = {
    ("Muğla", "Köyceğiz"): (36.9714, 28.6889, "Vikipedi: Köyceğiz (36°58′17″K 28°41′20″D)"),
    ("İzmir", "Beydağ"): (38.0833, 28.2333, "Vikipedi: Beydağ (38°05′K 28°14′D)"),
    ("Hakkari", "Derecik"): (37.0831, 44.3111, "Vikipedi: Derecik, Hakkâri (37°04′59″K 44°18′40″D)"),
    # GeoNames'te yanlış yere (aynı adlı köye) düşen veya ilçe sınırında tek uzak köy bulunan ilçeler:
    ("İstanbul", "Kadıköy"): (40.9934, 29.0374, "Vikipedi: Kadıköy"),
    ("İstanbul", "Avcılar"): (40.9792, 28.7214, "Vikipedi: Avcılar"),
    ("İstanbul", "Beykoz"): (41.1342, 29.0922, "Vikipedi: Beykoz"),
    ("İstanbul", "Sarıyer"): (41.1669, 29.0572, "Vikipedi: Sarıyer"),
    ("İstanbul", "Kartal"): (40.8872, 29.1900, "Vikipedi: Kartal"),
    ("Ankara", "Sincan"): (39.9594, 32.5767, "Vikipedi: Sincan"),
    ("Erzurum", "Aziziye"): (39.9458, 41.1053, "Vikipedi: Ilıca (Aziziye merkezi)"),
    ("İzmir", "Güzelbahçe"): (38.3356, 26.8911, "Vikipedi: Güzelbahçe"),
    ("Kırklareli", "Demirköy"): (41.8224, 27.7631, "Vikipedi: Demirköy"),
}

# Büyükşehirlerin il merkezini oluşturan şehir içi ilçeler: ilçe merkezi il merkezine çok yakın
# (yaklaşık 10-15 km), GeoNames'te ise bu ilçeler için ayrı yerleşim kaydı yok veya ilçe sınırının
# kırsal ucundaki köyler var. Bunlar için il merkezi kullanılır (fark 1 dakikanın altında).
SEHIR_ICI = {
    ("Adana", "Çukurova"), ("Ankara", "Altındağ"), ("Ankara", "Keçiören"), ("Ankara", "Yenimahalle"),
    ("Aydın", "Efeler"), ("Balıkesir", "Altıeylül"), ("Balıkesir", "Karesi"),
    ("Diyarbakır", "Yenişehir"), ("Erzurum", "Yakutiye"), ("Gaziantep", "Şahinbey"), ("Hatay", "Defne"),
    ("Kahramanmaraş", "Dulkadiroğlu"), ("Kahramanmaraş", "Onikişubat"), ("Kayseri", "Melikgazi"),
    ("Manisa", "Şehzadeler"), ("Manisa", "Yunusemre"), ("Mardin", "Artuklu"),
    ("Mersin", "Akdeniz"), ("Mersin", "Toroslar"), ("Mersin", "Yenişehir"), ("Sakarya", "Erenler"),
    ("Samsun", "Atakum"), ("Samsun", "Canik"), ("Tekirdağ", "Süleymanpaşa"), ("Trabzon", "Ortahisar"),
    ("Van", "İpekyolu"), ("Van", "Tuşba"), ("Şanlıurfa", "Eyyübiye"), ("Şanlıurfa", "Haliliye"),
    ("İstanbul", "Bakırköy"), ("İstanbul", "Bayrampaşa"), ("İstanbul", "Beyoğlu"),
    ("İstanbul", "Beşiktaş"), ("İstanbul", "Eyüpsultan"), ("İstanbul", "Gaziosmanpaşa"),
    ("İstanbul", "Güngören"), ("İstanbul", "Kağıthane"), ("İzmir", "Balçova"), ("İzmir", "Narlıdere"),
}


def fold(s):
    s = s.replace('İ', 'I').replace('ı', 'i').lower()
    s = unicodedata.normalize('NFD', s)
    s = ''.join(ch for ch in s if unicodedata.category(ch) != 'Mn')
    s = re.sub(r'\s+(ilcesi|province)$', '', s.strip())
    return re.sub(r'[^a-z0-9]', '', s)


def dist(a, b, c, d):
    p = math.pi / 180
    x = math.sin((c - a) * p / 2) ** 2 + math.cos(a * p) * math.cos(c * p) * math.sin((d - b) * p / 2) ** 2
    return 12742 * math.asin(math.sqrt(x))


def load_centers(path):
    src = open(path, encoding='utf8').read()
    names = re.findall(r'^\s+"([^"]+)",?$', src, re.M)

    def arr(n):
        body = re.search(n + r' = \{(.*?)\}', src, re.S).group(1)
        return [float(x) for x in body.replace('\n', '').split(',') if x.strip()]

    return names, arr('LAT'), arr('LON')


def main():
    tn, cj, cities_java = sys.argv[1], sys.argv[2], sys.argv[3]
    city_list = json.load(open(tn + '/src/data/cityList.json', encoding='utf8'))
    dbc = json.load(open(tn + '/src/data/districtsByCityCode.json', encoding='utf8'))
    places = [c for c in json.load(open(cj + '/cities.json', encoding='utf8')) if c['country'] == 'TR']
    admin1 = {fold(a['name']): a['code'].split('.')[1]
              for a in json.load(open(cj + '/admin1.json', encoding='utf8')) if a['code'].startswith('TR.')}
    admin2 = [a for a in json.load(open(cj + '/admin2.json', encoding='utf8')) if a['code'].startswith('TR.')]
    names, lats, lons = load_centers(cities_java)
    il_index = {fold(n): i for i, n in enumerate(names)}

    rows, far, methods = [], [], {}
    for c in sorted(city_list, key=lambda c: names[il_index[fold(c['name'])]]):
        il = c['name']
        ii = il_index[fold(il)]
        clat, clon = lats[ii], lons[ii]
        a1 = admin1.get(fold(il))
        if a1 is None:
            raise SystemExit('GeoNames ili bulunamadı: ' + il)
        in_il = [p for p in places if p['admin1'] == a1]
        for ilce in sorted(dbc[c['code']], key=fold):
            if ilce == 'Merkez':
                rows.append((ii, ilce, clat, clon, 'il merkezi'))
                methods['il merkezi'] = methods.get('il merkezi', 0) + 1
                continue
            if (il, ilce) in ELLE:
                la, lo, src = ELLE[(il, ilce)]
                rows.append((ii, ilce, la, lo, 'elle'))
                methods['elle'] = methods.get('elle', 0) + 1
                continue
            if (il, ilce) in SEHIR_ICI:
                rows.append((ii, ilce, clat, clon, 'şehir içi'))
                methods['şehir içi'] = methods.get('şehir içi', 0) + 1
                continue
            key = ALIAS.get(fold(ilce), fold(ilce))
            a2 = [a for a in admin2 if a['code'].split('.')[1] == a1 and fold(a['name']) == key]
            a2id = a2[0]['code'].split('.')[2] if a2 else None
            same = [p for p in in_il if fold(p['name']) == key]
            pick, how = None, None
            if a2id:
                s2 = [p for p in same if p['admin2'] == a2id]
                if s2:
                    pick, how = s2[0], 'yerleşim'
            if pick is None and not same:
                # GeoNames'te ili eski kayıtla (ör. Osmaniye kurulmadan önceki il) yazılmış olabilir:
                # aynı adlı yerleşim başka ilde ama il merkezine 100 km'den yakınsa kabul edilir.
                same = [p for p in places if fold(p['name']) == key
                        and dist(float(p['lat']), float(p['lng']), clat, clon) < 100]
            if pick is None and same:
                same.sort(key=lambda p: dist(float(p['lat']), float(p['lng']), clat, clon))
                pick, how = same[0], 'il içi ad'
            if pick is not None:
                la, lo = float(pick['lat']), float(pick['lng'])
            elif a2id:
                inside = [p for p in in_il if p['admin2'] == a2id]
                if inside:
                    la = sum(float(p['lat']) for p in inside) / len(inside)
                    lo = sum(float(p['lng']) for p in inside) / len(inside)
                    how = 'ilçe ortası (%d yerleşim)' % len(inside)
                elif (il, ilce) in ELLE:
                    la, lo = ELLE[(il, ilce)][:2]
                    how = 'elle: ' + ELLE[(il, ilce)][2]
                else:
                    # Büyükşehrin içindeki (şehir içi) ilçe: GeoNames'te ayrı yerleşim yok.
                    la, lo, how = clat, clon, 'il merkezi (şehir içi ilçe)'
                    print('şehir içi: %s / %s' % (il, ilce), file=sys.stderr)
            elif (il, ilce) in ELLE:
                la, lo = ELLE[(il, ilce)][:2]
                how = 'elle: ' + ELLE[(il, ilce)][2]
            else:
                raise SystemExit('bulunamadı: %s / %s' % (il, ilce))
            d = dist(la, lo, clat, clon)
            if d > 120:
                far.append('%s / %s: %d km' % (il, ilce, d))
            rows.append((ii, ilce, round(la, 4), round(lo, 4), how))
            if how != 'yerleşim':
                print('  %s / %s -> %s (%.4f, %.4f), il merkezine %d km'
                      % (il, ilce, how, la, lo, d), file=sys.stderr)
            k = how.split(' (')[0].split(':')[0]
            methods[k] = methods.get(k, 0) + 1

    print('İlçe: %d, yöntemler: %s' % (len(rows), methods), file=sys.stderr)
    print('İl merkezine 120 km\'den uzak: %s' % far, file=sys.stderr)
    if len(rows) != 973:
        raise SystemExit('973 ilçe bekleniyordu, %d bulundu' % len(rows))

    def jstr(s):
        return '"' + s.replace('"', '\\"') + '"'

    print('package tr.kolay.namazvakti;')
    print()
    print('/**')
    print(' * Türkiye\'nin 973 ilçesi. OTOMATİK ÜRETİLDİ: tools/ilce_koordinat_uret.py')
    print(' * İlçe listesi: npm turkey-neighbourhoods 4.0.3 (MIT). Koordinatlar: GeoNames (geonames.org,')
    print(' * CC BY 4.0), npm cities.json 1.1.65. "Merkez" ilçeleri il merkezini kullanır.')
    print(' */')
    print('final class Ilceler {')
    print('    private Ilceler() {}')
    print()
    print('    /** Cities.NAMES içindeki il sırası */')
    print('    static final short[] IL = {' + ', '.join(str(r[0]) for r in rows) + '};')
    print()
    print('    static final String[] AD = {')
    for r in rows:
        print('        %s,' % jstr(r[1]))
    print('    };')
    print()
    print('    static final double[] LAT = {' + ', '.join(repr(r[2]) for r in rows) + '};')
    print()
    print('    static final double[] LON = {' + ', '.join(repr(r[3]) for r in rows) + '};')
    print('}')


if __name__ == '__main__':
    main()
