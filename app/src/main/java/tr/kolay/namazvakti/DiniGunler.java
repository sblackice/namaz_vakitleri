package tr.kolay.namazvakti;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Dini gün ve geceler.
 *
 * Tarihler: Diyanet İşleri Başkanlığı'nın yayımladığı dini günler listeleri
 * (vakithesaplama.diyanet.gov.tr, icerik=153 / 154 / 185 / 186 / 187 → 2026-2030).
 * Resmî sayfalar bu çalışma ortamından doğrudan açılamadı; tarihler arama motorunun bu
 * sayfalardan verdiği satırlardan alındı. Her tarihin haftanın günü Diyanet'in yazdığıyla
 * karşılaştırıldı ve Hicrî ay başları tools/hicri_takvim_uret.py hesabıyla kontrol edildi
 * (her kandil, belirtilen günün akşamı başlar). 2030'dan sonrası için uygulamanın
 * güncellenmesi gerekir.
 */
final class DiniGunler {
    private DiniGunler() {}

    // Türler (bilgi sayfası metnini seçer)
    static final int UC_AYLAR = 0, REGAIB = 1, MIRAC = 2, BERAT = 3, RAMAZAN = 4, KADIR = 5,
            RAMAZAN_AREFE = 6, RAMAZAN_BAYRAMI = 7, KURBAN_AREFE = 8, KURBAN_BAYRAMI = 9,
            HICRI_YILBASI = 10, ASURE = 11, MEVLID = 12;

    static final class Gun {
        final int yil, ay, gun, tur, sureGun;
        final String ad, hicri;

        Gun(int yil, int ay, int gun, int tur, String ad, String hicri, int sureGun) {
            this.yil = yil;
            this.ay = ay;
            this.gun = gun;
            this.tur = tur;
            this.ad = ad;
            this.hicri = hicri;
            this.sureGun = sureGun;
        }

        boolean gece() {
            return tur == REGAIB || tur == MIRAC || tur == BERAT || tur == KADIR || tur == MEVLID;
        }

        /** Bu günün yerel saatle başlangıcı (00:00). */
        long baslangic(TimeZone tz) {
            Calendar c = Calendar.getInstance(tz);
            c.clear();
            c.set(yil, ay - 1, gun, 0, 0, 0);
            return c.getTimeInMillis();
        }
    }

    static final Gun[] LISTE = {
        new Gun(2026, 12, 10, UC_AYLAR, "Üç Ayların Başlangıcı", "1 Recep 1448", 0),
        new Gun(2026, 12, 10, REGAIB, "Regaib Kandili", "Recep'in ilk Cuma gecesi", 0),
        new Gun(2027, 1, 4, MIRAC, "Miraç Kandili", "26 Recep 1448 (27. gece)", 0),
        new Gun(2027, 1, 22, BERAT, "Berat Kandili", "14 Şaban 1448 (15. gece)", 0),
        new Gun(2027, 2, 8, RAMAZAN, "Ramazan Başlangıcı (ilk oruç)", "1 Ramazan 1448", 0),
        new Gun(2027, 3, 5, KADIR, "Kadir Gecesi", "26 Ramazan 1448 (27. gece)", 0),
        new Gun(2027, 3, 8, RAMAZAN_AREFE, "Ramazan Bayramı Arefesi", "Ramazan'ın son günü", 0),
        new Gun(2027, 3, 9, RAMAZAN_BAYRAMI, "Ramazan Bayramı", "1 Şevval 1448", 3),
        new Gun(2027, 5, 15, KURBAN_AREFE, "Kurban Bayramı Arefesi", "9 Zilhicce 1448", 0),
        new Gun(2027, 5, 16, KURBAN_BAYRAMI, "Kurban Bayramı", "10 Zilhicce 1448", 4),
        new Gun(2027, 6, 6, HICRI_YILBASI, "Hicri Yılbaşı", "1 Muharrem 1449", 0),
        new Gun(2027, 6, 15, ASURE, "Aşure Günü", "10 Muharrem 1449", 0),
        new Gun(2027, 8, 13, MEVLID, "Mevlid Kandili", "11 Rebiülevvel 1449 (12. gece)", 0),
        new Gun(2027, 11, 29, UC_AYLAR, "Üç Ayların Başlangıcı", "1 Recep 1449", 0),
        new Gun(2027, 12, 2, REGAIB, "Regaib Kandili", "Recep'in ilk Cuma gecesi", 0),
        new Gun(2027, 12, 24, MIRAC, "Miraç Kandili", "26 Recep 1449 (27. gece)", 0),
        new Gun(2028, 1, 11, BERAT, "Berat Kandili", "14 Şaban 1449 (15. gece)", 0),
        new Gun(2028, 1, 28, RAMAZAN, "Ramazan Başlangıcı (ilk oruç)", "1 Ramazan 1449", 0),
        new Gun(2028, 2, 22, KADIR, "Kadir Gecesi", "26 Ramazan 1449 (27. gece)", 0),
        new Gun(2028, 2, 25, RAMAZAN_AREFE, "Ramazan Bayramı Arefesi", "Ramazan'ın son günü", 0),
        new Gun(2028, 2, 26, RAMAZAN_BAYRAMI, "Ramazan Bayramı", "1 Şevval 1449", 3),
        new Gun(2028, 5, 4, KURBAN_AREFE, "Kurban Bayramı Arefesi", "9 Zilhicce 1449", 0),
        new Gun(2028, 5, 5, KURBAN_BAYRAMI, "Kurban Bayramı", "10 Zilhicce 1449", 4),
        new Gun(2028, 5, 25, HICRI_YILBASI, "Hicri Yılbaşı", "1 Muharrem 1450", 0),
        new Gun(2028, 6, 3, ASURE, "Aşure Günü", "10 Muharrem 1450", 0),
        new Gun(2028, 8, 2, MEVLID, "Mevlid Kandili", "11 Rebiülevvel 1450 (12. gece)", 0),
        new Gun(2028, 11, 18, UC_AYLAR, "Üç Ayların Başlangıcı", "1 Recep 1450", 0),
        new Gun(2028, 11, 23, REGAIB, "Regaib Kandili", "Recep'in ilk Cuma gecesi", 0),
        new Gun(2028, 12, 13, MIRAC, "Miraç Kandili", "26 Recep 1450 (27. gece)", 0),
        new Gun(2028, 12, 30, BERAT, "Berat Kandili", "14 Şaban 1450 (15. gece)", 0),
        // 2029 (Diyanet: vakithesaplama.diyanet.gov.tr/icerik.php?icerik=186)
        new Gun(2029, 1, 16, RAMAZAN, "Ramazan Başlangıcı (ilk oruç)", "1 Ramazan 1450", 0),
        new Gun(2029, 2, 10, KADIR, "Kadir Gecesi", "26 Ramazan 1450 (27. gece)", 0),
        new Gun(2029, 2, 13, RAMAZAN_AREFE, "Ramazan Bayramı Arefesi", "Ramazan'ın son günü", 0),
        new Gun(2029, 2, 14, RAMAZAN_BAYRAMI, "Ramazan Bayramı", "1 Şevval 1450", 3),
        new Gun(2029, 4, 23, KURBAN_AREFE, "Kurban Bayramı Arefesi", "9 Zilhicce 1450", 0),
        new Gun(2029, 4, 24, KURBAN_BAYRAMI, "Kurban Bayramı", "10 Zilhicce 1450", 4),
        new Gun(2029, 5, 14, HICRI_YILBASI, "Hicri Yılbaşı", "1 Muharrem 1451", 0),
        new Gun(2029, 5, 23, ASURE, "Aşure Günü", "10 Muharrem 1451", 0),
        new Gun(2029, 7, 23, MEVLID, "Mevlid Kandili", "11 Rebiülevvel 1451 (12. gece)", 0),
        new Gun(2029, 11, 7, UC_AYLAR, "Üç Ayların Başlangıcı", "1 Recep 1451", 0),
        new Gun(2029, 11, 8, REGAIB, "Regaib Kandili", "Recep'in ilk Cuma gecesi", 0),
        new Gun(2029, 12, 2, MIRAC, "Miraç Kandili", "26 Recep 1451 (27. gece)", 0),
        new Gun(2029, 12, 20, BERAT, "Berat Kandili", "14 Şaban 1451 (15. gece)", 0),
        // 2030 (Diyanet: vakithesaplama.diyanet.gov.tr/icerik.php?icerik=187)
        new Gun(2030, 1, 5, RAMAZAN, "Ramazan Başlangıcı (ilk oruç)", "1 Ramazan 1451", 0),
        new Gun(2030, 1, 30, KADIR, "Kadir Gecesi", "26 Ramazan 1451 (27. gece)", 0),
        new Gun(2030, 2, 3, RAMAZAN_AREFE, "Ramazan Bayramı Arefesi", "Ramazan'ın son günü", 0),
        new Gun(2030, 2, 4, RAMAZAN_BAYRAMI, "Ramazan Bayramı", "1 Şevval 1451", 3),
        new Gun(2030, 4, 12, KURBAN_AREFE, "Kurban Bayramı Arefesi", "9 Zilhicce 1451", 0),
        new Gun(2030, 4, 13, KURBAN_BAYRAMI, "Kurban Bayramı", "10 Zilhicce 1451", 4),
        new Gun(2030, 5, 3, HICRI_YILBASI, "Hicri Yılbaşı", "1 Muharrem 1452", 0),
        new Gun(2030, 5, 12, ASURE, "Aşure Günü", "10 Muharrem 1452", 0),
        new Gun(2030, 7, 12, MEVLID, "Mevlid Kandili", "11 Rebiülevvel 1452 (12. gece)", 0),
        new Gun(2030, 10, 28, UC_AYLAR, "Üç Ayların Başlangıcı", "1 Recep 1452", 0),
        new Gun(2030, 10, 31, REGAIB, "Regaib Kandili", "Recep'in ilk Cuma gecesi", 0),
        new Gun(2030, 11, 22, MIRAC, "Miraç Kandili", "26 Recep 1452 (27. gece)", 0),
        new Gun(2030, 12, 9, BERAT, "Berat Kandili", "14 Şaban 1452 (15. gece)", 0),
        new Gun(2030, 12, 26, RAMAZAN, "Ramazan Başlangıcı (ilk oruç)", "1 Ramazan 1452", 0),
    };

    static final String TARIH_KAYNAGI = "Tarihler Diyanet İşleri Başkanlığı'nın dini günler "
            + "listesine göredir (2026-2030). Kandil geceleri, yazılı günün akşamı (akşam namazıyla) "
            + "başlar.";

    /** Bugün veya sonrasındaki günler (bayramlar sürdüğü sürece listede kalır), tarih sırasıyla. */
    static List<Gun> yaklasanlar(long now, TimeZone tz) {
        List<Gun> out = new ArrayList<>();
        Calendar today = Calendar.getInstance(tz);
        today.setTimeInMillis(now);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long start = today.getTimeInMillis();
        for (Gun g : LISTE) {
            long end = g.baslangic(tz) + Math.max(1, g.sureGun) * 86400000L;
            if (end > start) out.add(g);
        }
        return out;
    }

    /** Bugünden o güne kaç gün var (bugün = 0). */
    static int kalanGun(Gun g, long now, TimeZone tz) {
        Calendar a = Calendar.getInstance(tz);
        a.setTimeInMillis(now);
        Calendar b = Calendar.getInstance(tz);
        b.setTimeInMillis(g.baslangic(tz));
        long da = Date0.days(a), db = Date0.days(b);
        return (int) (db - da);
    }

    /** Takvim gününü saat dilimi farklarından etkilenmeden saymak için. */
    static final class Date0 {
        private Date0() {}

        static long days(Calendar c) {
            Calendar u = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            u.clear();
            u.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            return u.getTimeInMillis() / 86400000L;
        }
    }

    static final class Bilgi {
        final String metin, kaynak;

        Bilgi(String metin, String kaynak) {
            this.metin = metin;
            this.kaynak = kaynak;
        }
    }

    private static final String GECE_IBADET = "Bu geceye özgü belirli bir namaz veya ibadet "
            + "şekli sahih hadislerde bildirilmemiştir. Müslümanlar kandil gecelerini genel "
            + "olarak dua, tövbe, Kur'an okuma, kaza ve nafile namazları ile iyilik yaparak "
            + "değerlendirir.";

    static Bilgi bilgi(int tur) {
        switch (tur) {
            case UC_AYLAR:
                return new Bilgi("Üç Aylar, Hicrî takvimin Recep, Şaban ve Ramazan aylarına "
                        + "verilen addır. Recep ayının ilk günü başlar ve Ramazan Bayramı'na kadar "
                        + "sürer. Bu dönemde Regaib, Miraç ve Berat kandilleri ile Kadir Gecesi "
                        + "bulunur.\n\nMüslümanlar bu ayları namaz, oruç, dua, tövbe, Kur'an okuma "
                        + "ve iyilikle değerlendirmeye özen gösterir. Hz. Âişe'nin bildirdiğine "
                        + "göre Peygamberimiz, Ramazan dışında en çok (nafile) orucu Şaban ayında "
                        + "tutardı.",
                        "Buhârî, 1969; Müslim, 1156");
            case REGAIB:
                return new Bilgi("Regaib Kandili, Recep ayının ilk Cuma gecesidir (Perşembe'yi "
                        + "Cuma'ya bağlayan gece). Üç Aylar'ın ilk kandilidir.\n\n“Regâib” "
                        + "kelimesi, “değerli ve rağbet edilen şeyler” anlamına gelen "
                        + "“ragîbe” kelimesinin çoğuludur.\n\n" + GECE_IBADET,
                        "");
            case MIRAC:
                return new Bilgi("Miraç Kandili, Recep ayının 27. gecesidir. Peygamberimiz Hz. "
                        + "Muhammed'in (s.a.s.) bir gece Mescid-i Haram'dan Mescid-i Aksâ'ya "
                        + "götürülmesi (İsrâ) ve oradan göklere yükseltilmesi (Miraç) olayının "
                        + "yıldönümü olarak idrak edilir.\n\nKur'an'da şöyle bildirilir: “Kulunu "
                        + "(Muhammed'i) bir gece Mescidi Haram'dan (Mekke'den), kendisine bir kısım "
                        + "ayetlerimizi göstermek için, çevresini mübarek kıldığımız Mescidi Aksa'ya "
                        + "(Kudüs'e) götüren Allah'ın şanı yücedir. Doğrusu O, işitir ve görür.”"
                        + "\n\nBuhârî ve Müslim'deki rivayetlere göre beş vakit namaz Miraç'ta "
                        + "farz kılınmıştır.\n\n" + GECE_IBADET,
                        "İsrâ Sûresi 17:1 (meal: QuranEnc Türkçe çevirisi); Buhârî, 349; "
                                + "Müslim, 162");
            case BERAT:
                return new Bilgi("Berat Kandili, Şaban ayının 15. gecesidir (14'ünü 15'ine "
                        + "bağlayan gece). “Berat” kelimesi “kurtulma, temize çıkma” anlamına "
                        + "gelir.\n\nHz. Âişe'nin bildirdiğine göre Peygamberimiz Ramazan "
                        + "dışında hiçbir ayın tamamını oruçla geçirmemiş, (nafile) orucu en çok "
                        + "Şaban ayında tutmuştur.\n\n" + GECE_IBADET,
                        "Buhârî, 1969; Müslim, 1156");
            case RAMAZAN:
                return new Bilgi("Ramazan, Hicrî takvimin dokuzuncu ayıdır. Bu ayda oruç "
                        + "tutmak, akıllı ve ergenlik çağına girmiş her Müslümana farzdır; hasta "
                        + "ve yolcu olanlar tutamadıkları günleri sonra kaza eder.\n\nKur'an'da: "
                        + "“Ramazan ayı, ki onda Kuran, insanlara yol gösterici ve doğruyu "
                        + "yanlıştan ayırıcı belgeler olarak indirildi. Sizden bu ayı idrak eden, "
                        + "onda oruç tutsun...”\n\nPeygamberimiz: “Kim inanarak ve karşılığını "
                        + "Allah'tan bekleyerek Ramazan orucunu tutarsa, geçmiş günahları "
                        + "bağışlanır” buyurmuştur.\n\nİlk oruçtan önceki akşam ilk teravih "
                        + "namazı kılınır, gece de ilk sahura kalkılır.",
                        "Bakara Sûresi 2:185 (meal: QuranEnc Türkçe çevirisi); Buhârî, 38; "
                                + "Müslim, 760");
            case KADIR:
                return new Bilgi("Kadir Gecesi, Kur'an-ı Kerim'in indirilmeye başlandığı "
                        + "gecedir. Kadir Sûresi'nde şöyle buyrulur: “Doğrusu, Biz, Kuran'ı kadir "
                        + "gecesinde indirmişizdir. Kadir gecesinin ne olduğunu sen bilir misin? "
                        + "Kadir gecesi bin aydan hayırlıdır. Melekler ve Cebrail o gecede "
                        + "Rablerinin izniyle her türlü iş için inerler. O gece, tan yerinin "
                        + "ağarmasına kadar bir esenliktir.”\n\nPeygamberimiz bu geceyi "
                        + "Ramazan'ın son on gecesinin tek gecelerinde aramayı tavsiye etmiştir. "
                        + "Türkiye'de Kadir Gecesi, Ramazan'ın 27. gecesi olarak idrak edilir."
                        + "\n\nHadiste: “Kim inanarak ve karşılığını Allah'tan bekleyerek Kadir "
                        + "Gecesi'ni ibadetle geçirirse, geçmiş günahları bağışlanır.”",
                        "Kadir Sûresi 97:1-5 (meal: QuranEnc Türkçe çevirisi); Buhârî, 2017; "
                                + "Buhârî, 35; Müslim, 760");
            case RAMAZAN_AREFE:
                return new Bilgi("Ramazan Bayramı'ndan önceki gün, Ramazan'ın son günüdür; "
                        + "halk arasında “arefe” denir. Ramazan'ın son orucu bu gün tutulur."
                        + "\n\nFıtır sadakası (fitre) henüz verilmediyse bayram namazından önce "
                        + "verilmelidir: Peygamberimiz fitrenin, insanlar bayram namazına "
                        + "çıkmadan önce verilmesini emretmiştir.",
                        "Buhârî, 1503; Müslim, 984");
            case RAMAZAN_BAYRAMI:
                return new Bilgi("Ramazan Bayramı, Şevval ayının ilk üç günüdür ve Ramazan "
                        + "orucunun tamamlanmasının ardından kutlanır. Bayramın ilk sabahı, "
                        + "güneş doğduktan bir süre sonra bayram namazı kılınır.\n\nBayramda "
                        + "akrabalar ve büyükler ziyaret edilir, dargınlar barıştırılır.\n\n"
                        + "Peygamberimiz bayramın birinci günü oruç tutmayı yasaklamıştır.",
                        "Buhârî, 1990; Müslim, 1137");
            case KURBAN_AREFE:
                return new Bilgi("Kurban Bayramı'ndan önceki gün, Zilhicce ayının 9. günüdür. "
                        + "Hacılar bu gün Arafat'ta vakfe yapar.\n\nHac yapmayanlar için Arefe "
                        + "günü oruç tutmak tavsiye edilmiştir. Peygamberimiz: “Arefe günü "
                        + "tutulan orucun, önceki yılın ve sonraki yılın günahlarına kefaret "
                        + "olmasını Allah'tan umarım” buyurmuştur.\n\nTeşrik tekbirleri Arefe "
                        + "günü sabah namazından başlar ve bayramın dördüncü günü ikindi namazına "
                        + "kadar, farz namazlardan sonra getirilir (toplam 23 vakit):\n“Allâhü "
                        + "ekber Allâhü ekber, lâ ilâhe illallâhü vallâhü ekber, Allâhü ekber ve "
                        + "lillâhi'l-hamd.”",
                        "Müslim, 1162");
            case KURBAN_BAYRAMI:
                return new Bilgi("Kurban Bayramı, Zilhicce ayının 10, 11, 12 ve 13. günleridir "
                        + "(dört gün). Bayramın ilk sabahı bayram namazı kılınır.\n\nHanefî "
                        + "mezhebine göre belirli bir zenginliğe sahip olan Müslümanın bayram "
                        + "günlerinde kurban kesmesi vaciptir. Kur'an'da: “Bu hayvanların ne "
                        + "etleri ve ne de kanları Allah'a ulaşacaktır. Allah'a ulaşacak olan "
                        + "ancak sizin O'nun için yaptığınız gösterişten uzak amel ve "
                        + "ibadettir.”\n\nPeygamberimiz bayramın birinci günü oruç tutmayı "
                        + "yasaklamış; teşrik günleri (bayramın sonraki günleri) için de “yeme, "
                        + "içme ve Allah'ı anma günleridir” buyurmuştur.",
                        "Hac Sûresi 22:37 (meal: QuranEnc Türkçe çevirisi); Buhârî, 1990; "
                                + "Müslim, 1137; Müslim, 1141");
            case HICRI_YILBASI:
                return new Bilgi("Hicrî yılbaşı, Hicrî takvimin ilk ayı olan Muharrem'in "
                        + "birinci günüdür.\n\nHicrî takvim, Peygamberimizin Mekke'den Medine'ye "
                        + "hicretini (M. 622) başlangıç kabul eder ve Hz. Ömer'in halifeliği "
                        + "döneminde kullanılmaya başlanmıştır.\n\nHicrî takvim Ay'ın "
                        + "hareketlerine dayanır. Hicrî yıl, Miladi yıldan yaklaşık 11 gün kısa "
                        + "olduğu için dini günler her yıl yaklaşık 11 gün öne gelir.",
                        "");
            case ASURE:
                return new Bilgi("Âşûrâ, Muharrem ayının 10. günüdür.\n\nİbn Abbas'ın "
                        + "bildirdiğine göre Peygamberimiz Medine'ye geldiğinde Yahudilerin bu "
                        + "gün oruç tuttuğunu gördü. Onlar bu günün, Allah'ın Hz. Mûsâ'yı ve "
                        + "İsrâiloğulları'nı düşmanlarından kurtardığı gün olduğunu söylediler. "
                        + "Peygamberimiz de bu gün oruç tuttu ve tutulmasını emretti. Bir başka "
                        + "hadiste: “Âşûrâ günü tutulan orucun da önceki yılın günahlarına "
                        + "kefaret olmasını Allah'tan umarım” buyurmuştur.\n\nPeygamberimizin "
                        + "torunu Hz. Hüseyin, Hicrî 61 (M. 680) yılında bu günde Kerbelâ'da "
                        + "şehit edilmiştir.\n\nTürkiye'de bu günlerde aşure pişirip komşulara "
                        + "dağıtmak yaygın bir gelenektir.",
                        "Buhârî, 2004; Müslim, 1130; Müslim, 1162");
            case MEVLID:
                return new Bilgi("Mevlid Kandili, Peygamberimiz Hz. Muhammed'in (s.a.s.) "
                        + "doğumunun yıldönümü olarak Rebîülevvel ayının 12. gecesi idrak edilir. "
                        + "Peygamberimizin 571 yılında Mekke'de dünyaya geldiği kabul edilir."
                        + "\n\nKur'an'da: “Biz seni ancak alemlere rahmet olarak gönderdik.”"
                        + "\n\n" + GECE_IBADET,
                        "Enbiyâ Sûresi 21:107 (meal: QuranEnc Türkçe çevirisi)");
            default:
                return new Bilgi("", "");
        }
    }
}
