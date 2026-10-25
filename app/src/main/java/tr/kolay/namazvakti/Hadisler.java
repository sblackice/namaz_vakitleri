package tr.kolay.namazvakti;

import java.util.TimeZone;

/**
 * "Günün Hadisi" listesi. Yalnızca Buhârî ve Müslim'in Sahîh'lerinden alınmıştır.
 *
 * Her hadisin numarası ve konusu, sunnah.com verisini içeren "hadith" (npm, MIT) paketindeki
 * Arapça asıl ve İngilizce çeviriyle karşılaştırılarak doğrulandı (tools/hadis_dogrula.py).
 * Numaralar Fuad Abdülbâkî numaralandırmasıdır. Türkçe metinler Arapça asıldan çevrilmiştir.
 */
final class Hadisler {
    private Hadisler() {}

    static final class Hadis {
        final String metin, ravi, kaynak;

        Hadis(String metin, String ravi, String kaynak) {
            this.metin = metin;
            this.ravi = ravi;
            this.kaynak = kaynak;
        }
    }

    static final Hadis[] LISTE = {
        new Hadis("Ameller ancak niyetlere göredir. Herkese ancak niyet ettiği şey vardır.",
                "Hz. Ömer (r.a.)", "Buhârî, 1; Müslim, 1907"),
        new Hadis("Müslüman, elinden ve dilinden Müslümanların emin olduğu kimsedir. Muhâcir de "
                + "Allah'ın yasakladıklarını terk eden kimsedir.",
                "Abdullah b. Amr (r.a.)", "Buhârî, 10"),
        new Hadis("Sizden biriniz, kendisi için istediğini kardeşi için de istemedikçe (gerçek "
                + "anlamda) iman etmiş olmaz.",
                "Enes b. Mâlik (r.a.)", "Buhârî, 13; Müslim, 45"),
        new Hadis("İslam beş esas üzerine kurulmuştur: Allah'tan başka ilah olmadığına ve "
                + "Muhammed'in Allah'ın Resûlü olduğuna şahitlik etmek, namazı dosdoğru kılmak, "
                + "zekât vermek, hac yapmak ve Ramazan orucunu tutmak.",
                "Abdullah b. Ömer (r.a.)", "Buhârî, 8; Müslim, 16"),
        new Hadis("İman altmış küsur (Müslim'in rivayetinde yetmiş küsur) şubedir. Hayâ da "
                + "imanın bir şubesidir.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 9; Müslim, 35"),
        new Hadis("Kim inanarak ve karşılığını Allah'tan bekleyerek Ramazan orucunu tutarsa, "
                + "geçmiş günahları bağışlanır.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 38; Müslim, 760"),
        new Hadis("Kim inanarak ve karşılığını Allah'tan bekleyerek Kadir Gecesi'ni ibadetle "
                + "geçirirse, geçmiş günahları bağışlanır.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 35; Müslim, 760"),
        new Hadis("Kolaylaştırınız, zorlaştırmayınız; müjdeleyiniz, nefret ettirmeyiniz.",
                "Enes b. Mâlik (r.a.)", "Buhârî, 69; Müslim, 1734"),
        new Hadis("Allah kime hayır dilerse, onu dinde anlayış sahibi kılar.",
                "Muâviye (r.a.)", "Buhârî, 71; Müslim, 1037"),
        new Hadis("“Birinizin kapısının önünde bir nehir olsa ve o kişi her gün orada beş defa "
                + "yıkansa, üzerinde kirden bir şey kalır mı, ne dersiniz?” Sahâbîler: “Kirinden "
                + "hiçbir şey kalmaz” dediler. Bunun üzerine: “İşte beş vakit namaz da böyledir. "
                + "Allah onlarla günahları siler” buyurdu.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 528; Müslim, 667"),
        new Hadis("Cemaatle kılınan namaz, tek başına kılınan namazdan yirmi yedi derece daha "
                + "faziletlidir.",
                "Abdullah b. Ömer (r.a.)", "Buhârî, 645; Müslim, 650"),
        new Hadis("Allah katında amellerin en sevimlisi, az da olsa devamlı olanıdır.",
                "Hz. Âişe (r.anhâ)", "Buhârî, 6464; Müslim, 783"),
        new Hadis("İki nimet vardır ki insanların çoğu bu ikisinde aldanmıştır: Sağlık ve boş "
                + "vakit.",
                "Abdullah b. Abbas (r.a.)", "Buhârî, 6412"),
        new Hadis("Allah'a ve ahiret gününe iman eden, komşusuna eziyet etmesin. Allah'a ve "
                + "ahiret gününe iman eden, misafirine ikram etsin. Allah'a ve ahiret gününe iman "
                + "eden, ya hayır söylesin ya da sussun.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 6018; Müslim, 47"),
        new Hadis("Güçlü kimse, güreşte rakibini yenen değildir. Asıl güçlü kimse, öfkelendiği "
                + "anda kendine hâkim olandır.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 6114; Müslim, 2609"),
        new Hadis("Resûlullah (s.a.s.) torunu Hasan'ı öptü. Yanında bulunan Akra' b. Hâbis: "
                + "“Benim on çocuğum var, hiçbirini öpmedim” dedi. Resûlullah ona baktı ve: "
                + "“Merhamet etmeyene merhamet edilmez” buyurdu.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 5997; Müslim, 2318"),
        new Hadis("İnsanın her bir eklemi için, güneşin doğduğu her gün bir sadaka gerekir: İki "
                + "kişi arasında adaletle hükmetmek sadakadır. Bir kimseye bineğine binmesinde "
                + "yardım etmek ya da yükünü bineğine yüklemek sadakadır. Güzel söz sadakadır. "
                + "Namaza giderken atılan her adım sadakadır. Yoldan eziyet veren bir şeyi "
                + "kaldırmak da sadakadır.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 2989; Müslim, 1009"),
        new Hadis("Kim Allah rızası için bir mescit yaparsa, Allah da ona cennette benzerini "
                + "yapar.",
                "Hz. Osman (r.a.)", "Buhârî, 450; Müslim, 533"),
        new Hadis("Mümin, mümin için, parçaları birbirini sağlamlaştıran bir bina gibidir. "
                + "(Peygamberimiz bunu söylerken parmaklarını birbirine geçirdi.)",
                "Ebû Mûsâ el-Eş'arî (r.a.)", "Buhârî, 481; Müslim, 2585"),
        new Hadis("Müminleri birbirlerine merhamet etmede, birbirlerini sevmede ve birbirlerine "
                + "şefkat göstermede tek bir vücut gibi görürsün. Vücudun bir organı "
                + "rahatsızlanınca, vücudun geri kalanı da uykusuzluk ve ateşle ona ortak olur.",
                "Nu'mân b. Beşîr (r.a.)", "Buhârî, 6011; Müslim, 2586"),
        new Hadis("Zulüm, kıyamet gününde karanlıklardır.",
                "Abdullah b. Ömer (r.a.)", "Buhârî, 2447; Müslim, 2579"),
        new Hadis("Sizin en hayırlınız, Kur'an'ı öğrenen ve öğretendir.",
                "Hz. Osman (r.a.)", "Buhârî, 5027"),
        new Hadis("Kişi sevdiği ile beraberdir.",
                "Abdullah b. Mes'ûd (r.a.)", "Buhârî, 6168; Müslim, 2640"),
        new Hadis("Mümin, aynı delikten iki defa sokulmaz.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 6133; Müslim, 2998"),
        new Hadis("Birbirinize kin tutmayın, birbirinizi kıskanmayın, birbirinize sırt "
                + "çevirmeyin. Ey Allah'ın kulları, kardeş olun! Bir Müslümanın, din kardeşine üç "
                + "günden fazla küs durması helal değildir.",
                "Enes b. Mâlik (r.a.)", "Buhârî, 6065; Müslim, 2559"),
        new Hadis("Her iyilik sadakadır.",
                "Câbir b. Abdullah ve Huzeyfe (r.a.)", "Buhârî, 6021; Müslim, 1005"),
        new Hadis("Yarım hurma (sadaka vermek) ile de olsa kendinizi ateşten koruyun.",
                "Adiy b. Hâtim (r.a.)", "Buhârî, 1417; Müslim, 1016"),
        new Hadis("Ezanı işittiğiniz zaman, müezzinin söylediğinin aynısını siz de söyleyin.",
                "Ebû Saîd el-Hudrî (r.a.)", "Buhârî, 611; Müslim, 383"),
        new Hadis("İnsanlara merhamet etmeyene Allah merhamet etmez.",
                "Cerîr b. Abdullah (r.a.)", "Buhârî, 7376; Müslim, 2319"),
        new Hadis("Müslümanın Müslüman üzerindeki hakkı beştir: Selamını almak, hastasını "
                + "ziyaret etmek, cenazesine katılmak, davetine icabet etmek ve aksırıp "
                + "“Elhamdülillah” dediğinde ona “Yerhamükellah” demek.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 1240; Müslim, 2162"),
        new Hadis("Helal de bellidir, haram da bellidir. Bu ikisinin arasında, insanların "
                + "çoğunun bilmediği şüpheli şeyler vardır. Kim şüpheli şeylerden sakınırsa dinini "
                + "ve namusunu korumuş olur. (...) Dikkat edin! Bedende bir et parçası vardır; o "
                + "iyi olursa bütün beden iyi olur, o bozulursa bütün beden bozulur. Dikkat edin, "
                + "o kalptir.",
                "Nu'mân b. Beşîr (r.a.)", "Buhârî, 52; Müslim, 1599"),
        new Hadis("Allah yaratmayı tamamlayınca, Arş'ın üzerinde kendi katında bulunan Kitab'a: "
                + "“Rahmetim gazabımı geçmiştir” diye yazdı.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 3194; Müslim, 2751"),
        new Hadis("Sabır, (musibetin) ilk sarsıntısı anında gösterilendir.",
                "Enes b. Mâlik (r.a.)", "Buhârî, 1283; Müslim, 926"),
        new Hadis("İki söz vardır ki dile hafif, mizanda ağır, Rahmân'a sevimlidir: "
                + "“Sübhânallâhi ve bi-hamdihî, Sübhânallâhi'l-azîm.”",
                "Ebû Hüreyre (r.a.)", "Buhârî, 6406; Müslim, 2694"),
        new Hadis("Kim günde yüz defa “Sübhânallâhi ve bi-hamdihî” derse, deniz köpüğü kadar "
                + "olsa bile günahları bağışlanır.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 6405; Müslim, 2691"),
        new Hadis("Cebrâil bana komşuya iyilik etmeyi o kadar çok tavsiye etti ki, komşuyu "
                + "komşuya mirasçı kılacak sandım.",
                "Hz. Âişe (r.anhâ)", "Buhârî, 6014; Müslim, 2624"),
        new Hadis("Bir adam Resûlullah'a gelerek: “Ey Allah'ın Resûlü! İyi davranmama en layık "
                + "kimdir?” diye sordu. “Annen” buyurdu. “Sonra kim?” dedi. “Annen” buyurdu. "
                + "“Sonra kim?” dedi. “Annen” buyurdu. “Sonra kim?” dedi. “Baban” buyurdu.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 5971; Müslim, 2548"),
        new Hadis("Biriniz mescide girdiğinde, oturmadan önce iki rekât namaz kılsın.",
                "Ebû Katâde (r.a.)", "Buhârî, 444; Müslim, 714"),
        new Hadis("Oruçlunun iki sevinci vardır: Biri orucunu açtığı zamanki sevinci, diğeri "
                + "Rabbine kavuştuğu zaman orucundan dolayı duyacağı sevinçtir.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 1904; Müslim, 1151"),
        new Hadis("Sahur yapınız; çünkü sahurda bereket vardır.",
                "Enes b. Mâlik (r.a.)", "Buhârî, 1923; Müslim, 1095"),
        new Hadis("Ramazan ayı girdiğinde gök kapıları açılır, cehennem kapıları kapanır ve "
                + "şeytanlar zincire vurulur.",
                "Ebû Hüreyre (r.a.)", "Buhârî, 1899; benzeri: Müslim, 1079"),
        new Hadis("Kadir Gecesi'ni Ramazan'ın son on gecesinin tek gecelerinde arayın.",
                "Hz. Âişe (r.anhâ)", "Buhârî, 2017"),
        new Hadis("Allah sizin dış görünüşünüze ve mallarınıza bakmaz; fakat kalplerinize ve "
                + "amellerinize bakar.",
                "Ebû Hüreyre (r.a.)", "Müslim, 2564"),
        new Hadis("Temizlik imanın yarısıdır. “Elhamdülillah” mizanı doldurur. “Sübhânallah” ve "
                + "“Elhamdülillah” gökler ile yer arasını doldurur. Namaz nurdur, sadaka "
                + "(imana) delildir, sabır ışıktır. Kur'an ise ya lehine ya da aleyhine bir "
                + "delildir.",
                "Ebû Mâlik el-Eş'arî (r.a.)", "Müslim, 223"),
        new Hadis("“Din nasihattir (samimiyettir).” “Kimin için?” diye sorduk. “Allah için, "
                + "Kitabı için, Resûlü için, Müslümanların yöneticileri ve bütün Müslümanlar "
                + "için” buyurdu.",
                "Temîm ed-Dârî (r.a.)", "Müslim, 55"),
        new Hadis("Sadaka maldan bir şey eksiltmez. Allah, affeden kulun ancak şerefini artırır. "
                + "Kim Allah için alçak gönüllü olursa, Allah onu yükseltir.",
                "Ebû Hüreyre (r.a.)", "Müslim, 2588"),
        new Hadis("Bir hayra yol gösteren kimse, o hayrı yapanın sevabı kadar sevap alır.",
                "Ebû Mes'ûd el-Ensârî (r.a.)", "Müslim, 1893"),
        new Hadis("Kim bir müminin dünya sıkıntılarından birini giderirse, Allah da onun kıyamet "
                + "günü sıkıntılarından birini giderir. Kim darda kalana kolaylık gösterirse, "
                + "Allah da ona dünyada ve ahirette kolaylık gösterir. Kim bir Müslümanın ayıbını "
                + "örterse, Allah da dünyada ve ahirette onun ayıbını örter. Kul, kardeşinin "
                + "yardımında olduğu sürece Allah da onun yardımındadır. Kim ilim öğrenmek için "
                + "bir yola girerse, Allah da onun için cennete giden yolu kolaylaştırır.",
                "Ebû Hüreyre (r.a.)", "Müslim, 2699"),
        new Hadis("“Kalbinde zerre kadar kibir bulunan kimse cennete giremez.” Bir adam: "
                + "“İnsan elbisesinin güzel, ayakkabısının güzel olmasını ister” dedi. "
                + "Peygamberimiz: “Allah güzeldir, güzelliği sever. Kibir, hakkı kabul etmemek "
                + "ve insanları küçük görmektir” buyurdu.",
                "Abdullah b. Mes'ûd (r.a.)", "Müslim, 91"),
        new Hadis("Bize karşı silah taşıyan bizden değildir. Bizi aldatan da bizden değildir.",
                "Ebû Hüreyre (r.a.)", "Müslim, 101"),
        new Hadis("İman etmedikçe cennete giremezsiniz; birbirinizi sevmedikçe de iman etmiş "
                + "olmazsınız. Yaptığınız zaman birbirinizi seveceğiniz bir şeyi size bildireyim "
                + "mi? Aranızda selamı yayınız.",
                "Ebû Hüreyre (r.a.)", "Müslim, 54"),
        new Hadis("Kulun Rabbine en yakın olduğu an, secde hâlidir. Öyleyse (secdede) duayı "
                + "çoğaltın.",
                "Ebû Hüreyre (r.a.)", "Müslim, 482"),
        new Hadis("Sabah namazını kılan kimse, Allah'ın koruması altındadır.",
                "Cündeb b. Abdullah (r.a.)", "Müslim, 657"),
        new Hadis("Arefe günü tutulan orucun, önceki yılın ve sonraki yılın günahlarına kefaret "
                + "olmasını Allah'tan umarım. Âşûrâ günü tutulan orucun da önceki yılın "
                + "günahlarına kefaret olmasını Allah'tan umarım.",
                "Ebû Katâde (r.a.)", "Müslim, 1162"),
    };

    /** Bugünün sırası: her gece yarısı (seçili şehrin saatine göre) bir sonraki hadise geçer. */
    static int indexFor(long now, TimeZone tz) {
        long localMs = now + tz.getOffset(now);
        long day = Math.floorDiv(localMs, 86400000L);
        return (int) Math.floorMod(day, (long) LISTE.length);
    }

    static Hadis gunun(long now, TimeZone tz) {
        return LISTE[indexFor(now, tz)];
    }
}
