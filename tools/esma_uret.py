"""Esma.java dosyasını üretir (Esmâ-ül Hüsnâ: Allah'ın 99 ismi).

Arapça isimler, elle yazılmadan, Tirmizî'nin Sünen'indeki hadisin (Daavât bölümü, 3507)
Arapça metninden alınır. Veri: sunnah.com verisini içeren npm "hadith" 1.3.0 paketi (MIT).
Hadisin metninde "هُوَ اللَّهُ الَّذِي لاَ إِلَهَ إِلاَّ هُوَ" sözünden sonra 98 isim sayılır;
"Allah" ismiyle birlikte 99 eder.

Türkçe okunuşlar Türkiye'de yaygın yazımla, anlamlar kısa açıklama olarak bu dosyada yazılıdır.
Bunlar resmî bir kaynaktan harf harf alınmadı (Diyanet sitesine bu ortamdan erişilemedi).

Kullanım:
  npm pack hadith@1.3.0 && tar -xzf hadith-1.3.0.tgz      # package/data/hadith.db
  python3 -I tools/esma_uret.py package/data/hadith.db > app/src/main/java/tr/kolay/namazvakti/Esma.java
"""
import sqlite3
import sys
import unicodedata

TR = [
    ("Allah", "Bütün güzel isimleri kendinde toplayan, eşi ve benzeri olmayan, ibadete layık tek ilah."),
    ("Er-Rahmân", "Dünyada bütün yaratılmışlara merhamet eden; rahmeti her şeyi kuşatan."),
    ("Er-Rahîm", "Ahirette müminlere sonsuz merhamet ve ikramda bulunan."),
    ("El-Melik", "Bütün kâinatın ve mülkün gerçek sahibi ve hükümdarı."),
    ("El-Kuddûs", "Her türlü eksiklik ve kusurdan uzak, tertemiz olan."),
    ("Es-Selâm", "Her türlü tehlikeden kullarını esenliğe çıkaran; esenlik veren."),
    ("El-Mü'min", "Güven veren; kalplere iman nuru veren."),
    ("El-Müheymin", "Her şeyi görüp gözeten ve koruyan."),
    ("El-Azîz", "İzzet sahibi; yenilmesi mümkün olmayan, her şeye galip olan."),
    ("El-Cebbâr", "Dilediğini yapmaya gücü yeten; kırılanı onaran."),
    ("El-Mütekebbir", "Büyüklükte eşi olmayan; büyüklüğü her şeyde görünen."),
    ("El-Hâlık", "Her şeyi yoktan var eden, yaratan."),
    ("El-Bâri'", "Her şeyi bir ölçüye göre kusursuz ve uyumlu yaratan."),
    ("El-Musavvir", "Yarattıklarına şekil ve suret veren."),
    ("El-Gaffâr", "Günahları çokça bağışlayan."),
    ("El-Kahhâr", "Her şeye galip ve hâkim olan."),
    ("El-Vehhâb", "Karşılıksız olarak bol bol nimet veren."),
    ("Er-Rezzâk", "Bütün yaratılmışların rızkını veren."),
    ("El-Fettâh", "Her türlü darlığı açan, kolaylaştıran; hak ile hükmeden."),
    ("El-Alîm", "Her şeyi en ince ayrıntısına kadar bilen."),
    ("El-Kâbıd", "Dilediğine darlık veren, rızkı daraltan."),
    ("El-Bâsıt", "Dilediğine bolluk veren, rızkı genişleten."),
    ("El-Hâfıd", "Dilediğini alçaltan."),
    ("Er-Râfi'", "Dilediğini yükselten."),
    ("El-Muiz", "Dilediğine izzet ve şeref veren."),
    ("El-Müzil", "Dilediğini zillete düşüren."),
    ("Es-Semî'", "Her şeyi işiten."),
    ("El-Basîr", "Her şeyi gören."),
    ("El-Hakem", "Hükmeden; hakkı batıldan ayıran."),
    ("El-Adl", "Mutlak adalet sahibi."),
    ("El-Latîf", "Lütuf ve ihsan sahibi; en ince işleri bilen."),
    ("El-Habîr", "Her şeyin iç yüzünden haberdar olan."),
    ("El-Halîm", "Cezalandırmakta acele etmeyen, yumuşak davranan."),
    ("El-Azîm", "Pek büyük, azamet sahibi."),
    ("El-Gafûr", "Affı ve bağışlaması çok olan."),
    ("Eş-Şekûr", "Az amele çok karşılık veren."),
    ("El-Aliyy", "Yüceler yücesi."),
    ("El-Kebîr", "Pek büyük."),
    ("El-Hafîz", "Her şeyi koruyup gözeten."),
    ("El-Mukît", "Yaratılmışların azığını veren; onları gözetip koruyan."),
    ("El-Hasîb", "Kulların hesabını gören; kuluna yeten."),
    ("El-Celîl", "Celâl ve azamet sahibi."),
    ("El-Kerîm", "Lütfu ve ikramı bol olan."),
    ("Er-Rakîb", "Her şeyi her an gözetleyen."),
    ("El-Mücîb", "Dualara karşılık veren."),
    ("El-Vâsi'", "Rahmeti, ilmi ve kudreti her şeyi kuşatan."),
    ("El-Hakîm", "Her işi hikmetli olan."),
    ("El-Vedûd", "Kullarını seven, sevilmeye layık olan."),
    ("El-Mecîd", "Şanı yüce, şerefi büyük olan."),
    ("El-Bâis", "Ölüleri dirilten; peygamber gönderen."),
    ("Eş-Şehîd", "Her şeye şahit olan."),
    ("El-Hakk", "Varlığı hiç değişmeden duran; gerçeğin ta kendisi."),
    ("El-Vekîl", "Kendisine güvenilip dayanılan, işleri yoluna koyan."),
    ("El-Kaviyy", "Pek güçlü."),
    ("El-Metîn", "Çok sağlam; kuvveti hiç azalmayan."),
    ("El-Veliyy", "Müminlerin dostu ve yardımcısı."),
    ("El-Hamîd", "Her türlü övgüye layık olan."),
    ("El-Muhsî", "Her şeyin sayısını bilen."),
    ("El-Mübdi'", "Örneksiz olarak ilk defa yaratan."),
    ("El-Muîd", "Öldükten sonra yeniden dirilten."),
    ("El-Muhyî", "Hayat veren."),
    ("El-Mümît", "Ölümü yaratan."),
    ("El-Hayy", "Diri; ezelî ve ebedî hayat sahibi."),
    ("El-Kayyûm", "Varlığı kendinden olan, her şeyi ayakta tutan."),
    ("El-Vâcid", "Dilediğini dilediği an bulan; hiçbir şeye muhtaç olmayan."),
    ("El-Mâcid", "Kadri ve şanı büyük olan."),
    ("El-Vâhid", "Bir olan; eşi ve benzeri olmayan."),
    ("Es-Samed", "Hiçbir şeye muhtaç olmayan; her şeyin O'na muhtaç olduğu."),
    ("El-Kâdir", "Her şeye gücü yeten."),
    ("El-Muktedir", "Kudret sahibi; dilediğini dilediği gibi yapan."),
    ("El-Mukaddim", "Dilediğini öne alan."),
    ("El-Muahhir", "Dilediğini geriye bırakan."),
    ("El-Evvel", "Başlangıcı olmayan; ezelî."),
    ("El-Âhir", "Sonu olmayan; ebedî."),
    ("Ez-Zâhir", "Varlığı apaçık olan."),
    ("El-Bâtın", "Zâtının hakikati gizli olan."),
    ("El-Vâlî", "Bütün kâinatı yöneten."),
    ("El-Müteâlî", "Yarattıklarına benzemekten yüce olan."),
    ("El-Berr", "İyiliği ve ihsanı bol olan."),
    ("Et-Tevvâb", "Tövbeleri kabul eden."),
    ("El-Müntekım", "Suçluları adaletle cezalandıran."),
    ("El-Afüvv", "Affı çok olan."),
    ("Er-Raûf", "Çok şefkatli."),
    ("Mâlikü'l-Mülk", "Mülkün ebedî sahibi."),
    ("Zü'l-Celâli ve'l-İkrâm", "Celâl (yücelik) ve ikram (cömertlik) sahibi."),
    ("El-Muksit", "Her işi adaletle yapan."),
    ("El-Câmi'", "Dilediğini bir araya getiren; kıyamet günü herkesi toplayan."),
    ("El-Ganiyy", "Hiçbir şeye muhtaç olmayan, zengin."),
    ("El-Mugnî", "Dilediğini zengin eden."),
    ("El-Mâni'", "Dilemediği şeye engel olan."),
    ("Ed-Dârr", "Zarar veren şeyleri de yaratan (imtihan için)."),
    ("En-Nâfi'", "Fayda veren şeyleri yaratan."),
    ("En-Nûr", "Âlemleri nurlandıran; dilediğine hidayet nuru veren."),
    ("El-Hâdî", "Hidayet veren, doğru yolu gösteren."),
    ("El-Bedî'", "Eşi benzeri olmayan güzellikte yaratan."),
    ("El-Bâkî", "Varlığının sonu olmayan."),
    ("El-Vâris", "Her şey yok olduktan sonra kalan; her şeyin asıl sahibi."),
    ("Er-Reşîd", "Doğru yolu gösteren; her işi yerli yerinde olan."),
    ("Es-Sabûr", "Çok sabırlı; cezalandırmakta acele etmeyen."),
]

LEAD = "هُوَ اللَّهُ الَّذِي لاَ إِلَهَ إِلاَّ هُوَ"
JOIN = {"مَالِكُ": 1, "ذُو": 2}  # sonraki kaç kelimeyle birleşir


def names_from_hadith(db_path):
    db = sqlite3.connect('file:' + db_path + '?mode=ro', uri=True)
    rows = list(db.execute("select c7 from hadith_content where c1=30 and c4='3507'"))
    if len(rows) != 1:
        raise SystemExit('Tirmizî 3507 bulunamadı')
    words = rows[0][0].replace('\u200f', ' ').split()
    # Hareke sırası metinde farklı olabilir (şedde/üstün); karşılaştırma NFC ile yapılır,
    # isimler ise metindeki hâliyle aynen alınır.
    norm = [unicodedata.normalize('NFC', w) for w in words]
    lead = [unicodedata.normalize('NFC', w) for w in LEAD.split()]
    i = next(k for k in range(len(norm)) if norm[k:k + len(lead)] == lead)
    allah = words[i + 1]
    words = words[i + len(lead):]
    names = [allah]
    k = 0
    while k < len(words):
        n = JOIN.get(words[k], 0)
        names.append(' '.join(words[k:k + n + 1]))
        k += n + 1
    return names


def jstr(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"') + '"'


def main():
    ar = names_from_hadith(sys.argv[1])
    if len(ar) != 99 or len(TR) != 99:
        raise SystemExit('99 isim bekleniyordu: Arapça %d, Türkçe %d' % (len(ar), len(TR)))
    print('package tr.kolay.namazvakti;')
    print()
    print('/**')
    print(' * Esmâ-ül Hüsnâ: Allah\'ın 99 ismi. OTOMATİK ÜRETİLDİ: tools/esma_uret.py')
    print(' * Arapça yazılış ve sıra: Tirmizî, Daavât, 3507 (npm "hadith" 1.3.0 verisinden aynen).')
    print(' * Türkçe okunuş ve kısa anlamlar: tools/esma_uret.py içinde.')
    print(' */')
    print('final class Esma {')
    print('    private Esma() {}')
    print()
    print('    static final class Isim {')
    print('        final String arapca, okunus, anlam;')
    print()
    print('        Isim(String arapca, String okunus, String anlam) {')
    print('            this.arapca = arapca;')
    print('            this.okunus = okunus;')
    print('            this.anlam = anlam;')
    print('        }')
    print('    }')
    print()
    print('    static final Isim[] LISTE = {')
    for a, (o, m) in zip(ar, TR):
        print('        new Isim(%s, %s,' % (jstr(a), jstr(o)))
        print('                %s),' % jstr(m))
    print('    };')
    print('}')


if __name__ == '__main__':
    main()
