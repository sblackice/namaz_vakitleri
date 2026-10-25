"""Günün Hadisi listesindeki her kaynağı hadis veritabanıyla karşılaştırmak için yardımcı.

Kullanım:
  npm pack hadith@1.3.0 && tar -xzf hadith-1.3.0.tgz      # package/data/hadith.db
  python3 -I tools/hadis_dogrula.py package/data/hadith.db app/src/main/java/tr/kolay/namazvakti/Hadisler.java

Her hadis için Hadisler.java'daki Türkçe metni, veritabanındaki kitap adını, derecesini ve
İngilizce çevirinin başını yan yana yazdırır; bulunamayan numara varsa HATA verir.
Veritabanı: sunnah.com verisi (npm "hadith" paketi, MIT). Numaralar Fuad Abdülbâkî numaralarıdır.
"""
import re
import sqlite3
import sys

db_path, java_path = sys.argv[1], sys.argv[2]
db = sqlite3.connect('file:' + db_path + '?mode=ro', uri=True)
src = open(java_path, encoding='utf8').read()
entries = re.findall(r'new Hadis\((.*?)\),\n', src, re.S)
COLL = {'Buhârî': 1, 'Müslim': 2}
errors = 0


def find(coll, num):
    for urn, disp, grade in db.execute(
            "select c0, c4, c13 from hadith_content where c1=? and c4 like ?", (coll, '%' + num + '%')):
        for part in disp.replace(' ', '').split(','):
            if part == num or (part.startswith(num) and part[len(num):].isalpha()):
                return urn, disp, grade
    return None


for e in entries:
    strings = re.findall(r'"((?:[^"\\]|\\.)*)"', e)
    text = ''.join(strings[:-2])
    source = strings[-1]
    print('=' * 80)
    print('TR :', text[:200])
    print('KAYNAK:', source)
    for name, num in re.findall(r'(Buhârî|Müslim), (\d+)', source):
        hit = find(COLL[name], num)
        if not hit:
            print('  HATA: bulunamadı', name, num)
            errors += 1
            continue
        urn, disp, grade = hit
        en = db.execute("select c4 from hadith_en_content where c0=?", (urn,)).fetchone()
        print('  %s %s | derece: %s | EN: %s' % (name, disp, grade, (en[0] if en else '')[:220]))
print('\nToplam %d hadis, %d hata' % (len(entries), errors))
sys.exit(1 if errors else 0)
