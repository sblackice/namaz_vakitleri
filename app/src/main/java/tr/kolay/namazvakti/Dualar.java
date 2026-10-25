package tr.kolay.namazvakti;

/**
 * "Dualar" sekmesi: namazda okunan dualar, sıralı. En başta Fâtiha Sûresi.
 *
 * Arapça metinler:
 *  - Kur'an'dan olanlar KuranMetni.java'dan gelir (QuranEnc Medine Mushafı metni).
 *  - Hadislerden olanlar, kaynakta gösterilen hadisin Arapça metninden birebir alınmıştır
 *    (sunnah.com verisi, npm "hadith" paketi).
 *  - Kunut duaları Hanefî mezhebinde okunan şekliyle yazılmıştır.
 * Türkçe okunuşlar Türkiye'de yaygın kullanılan yazımla verilmiştir.
 */
final class Dualar {
    private Dualar() {}

    static final class Dua {
        final String bolum, ad, arapca, okunus, anlam, not, kaynak;

        Dua(String bolum, String ad, String arapca, String okunus, String anlam, String not,
                String kaynak) {
            this.bolum = bolum;
            this.ad = ad;
            this.arapca = arapca;
            this.okunus = okunus;
            this.anlam = anlam;
            this.not = not;
            this.kaynak = kaynak;
        }
    }

    static final String NAMAZDA = "Namazda okunanlar";
    static final String VITIR = "Vitir namazında okunanlar";
    static final String SURELER = "Namazda okunan kısa sûreler";
    static final String SONRA = "Namazdan sonra";
    static final String MEAL_KAYNAGI = "Meal: QuranEnc Türkçe çevirisi.";

    static final Dua[] LISTE = {
        new Dua(NAMAZDA, "Fâtiha Sûresi", KuranMetni.FATIHA_AR,
                "Bismillâhirrahmânirrahîm. Elhamdü lillâhi rabbi'l-âlemîn. Errahmâni'r-rahîm. "
                        + "Mâliki yevmi'd-dîn. İyyâke na'büdü ve iyyâke neste'în. "
                        + "İhdine's-sırâta'l-müstakîm. Sırâta'llezîne en'amte aleyhim "
                        + "ğayri'l-mağdûbi aleyhim ve le'd-dâllîn.",
                KuranMetni.FATIHA_MEAL,
                "Her rekâtta okunur. Bitince içinden “Âmin” denir.",
                "Kur'an, Fâtiha Sûresi (1). " + MEAL_KAYNAGI),
        new Dua(NAMAZDA, "Sübhâneke",
                "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ "
                        + "وَلاَ إِلَهَ غَيْرُكَ",
                "Sübhânekellâhümme ve bi-hamdik. Ve tebârakesmük. Ve teâlâ ceddük. "
                        + "Ve lâ ilâhe ğayrük.",
                "Allah'ım! Sen her türlü eksiklikten uzaksın. Seni hamd ile överim. Senin adın "
                        + "mübarektir. Şanın çok yücedir. Senden başka ilah yoktur.",
                "Namaza başlarken, iftitah tekbirinden sonra okunur. Cenaze namazında "
                        + "“ve celle senâük” eklenir.",
                "Tirmizî, 243; Müslim, 399"),
        new Dua(NAMAZDA, "Rükû tesbihi", "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                "Sübhâne rabbiye'l-azîm.",
                "Yüce Rabbimi her türlü eksiklikten tenzih ederim.",
                "Rükûda en az üç defa söylenir.",
                "Müslim, 772; Tirmizî, 261"),
        new Dua(NAMAZDA, "Rükûdan doğrulurken",
                "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ\nرَبَّنَا لَكَ الْحَمْدُ",
                "Semi'allâhü limen hamideh. Rabbenâ leke'l-hamd.",
                "Allah, kendisine hamd edeni işitir. Rabbimiz! Hamd yalnız sana mahsustur.",
                "Rükûdan kalkarken “Semi'allâhü limen hamideh”, doğrulunca “Rabbenâ leke'l-hamd” "
                        + "denir.",
                "Buhârî, 3228"),
        new Dua(NAMAZDA, "Secde tesbihi", "سُبْحَانَ رَبِّيَ الأَعْلَى",
                "Sübhâne rabbiye'l-a'lâ.",
                "En yüce olan Rabbimi her türlü eksiklikten tenzih ederim.",
                "Her secdede en az üç defa söylenir.",
                "Müslim, 772; Tirmizî, 261"),
        new Dua(NAMAZDA, "Ettehiyyâtü",
                "التَّحِيَّاتُ لِلَّهِ، وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلاَمُ عَلَيْكَ أَيُّهَا "
                        + "النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلاَمُ عَلَيْنَا وَعَلَى "
                        + "عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ، "
                        + "وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                "Ettehiyyâtü lillâhi ve's-salevâtü ve't-tayyibât. Esselâmü aleyke eyyühe'n-"
                        + "nebiyyü ve rahmetullâhi ve berekâtüh. Esselâmü aleynâ ve alâ "
                        + "ibâdillâhi's-sâlihîn. Eşhedü en lâ ilâhe illallâh. Ve eşhedü enne "
                        + "Muhammeden abdühû ve rasûlüh.",
                "Bütün saygılar, dualar ve güzel sözler Allah'a mahsustur. Ey Peygamber! "
                        + "Allah'ın selamı, rahmeti ve bereketi senin üzerine olsun. Selam bizim "
                        + "üzerimize ve Allah'ın salih kulları üzerine olsun. Şahitlik ederim ki "
                        + "Allah'tan başka ilah yoktur. Yine şahitlik ederim ki Muhammed O'nun "
                        + "kulu ve elçisidir.",
                "Namazda her oturuşta okunur.",
                "Buhârî, 831"),
        new Dua(NAMAZDA, "Allâhümme Salli",
                "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ، وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى "
                        + "إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
                "Allâhümme salli alâ Muhammedin ve alâ âli Muhammed. Kemâ salleyte alâ "
                        + "İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
                "Allah'ım! İbrahim'e ve İbrahim'in ailesine rahmet ettiğin gibi, Muhammed'e ve "
                        + "Muhammed'in ailesine de rahmet eyle. Şüphesiz sen övülmeye layıksın, "
                        + "şanı yücesin.",
                "Son oturuşta Ettehiyyâtü'den sonra okunur.",
                "Buhârî, 3370"),
        new Dua(NAMAZDA, "Allâhümme Bârik",
                "اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ، وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى "
                        + "إِبْرَاهِيمَ، وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
                "Allâhümme bârik alâ Muhammedin ve alâ âli Muhammed. Kemâ bârekte alâ "
                        + "İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
                "Allah'ım! İbrahim'e ve İbrahim'in ailesine bereket verdiğin gibi, Muhammed'e ve "
                        + "Muhammed'in ailesine de bereket ver. Şüphesiz sen övülmeye layıksın, "
                        + "şanı yücesin.",
                "Son oturuşta Allâhümme Salli'den sonra okunur.",
                "Buhârî, 3370"),
        new Dua(NAMAZDA, "Rabbenâ Âtinâ", KuranMetni.RABBENA_ATINA_AR,
                "Rabbenâ âtinâ fi'd-dünyâ haseneten ve fi'l-âhireti haseneten ve kınâ "
                        + "azâbe'n-nâr.",
                "Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi cehennem "
                        + "azabından koru.",
                "Son oturuşta Allâhümme Bârik'ten sonra okunur.",
                "Kur'an, Bakara Sûresi 2:201 (ayetin dua kısmı)."),
        new Dua(NAMAZDA, "Rabbenâğfirlî", KuranMetni.RABBENAGFIRLI_AR,
                "Rabbenâğfirlî ve li-vâlideyye ve li'l-mü'minîne yevme yekûmü'l-hisâb.",
                KuranMetni.RABBENAGFIRLI_MEAL,
                "Rabbenâ Âtinâ'dan sonra okunur; ardından selam verilir.",
                "Kur'an, İbrâhîm Sûresi 14:41. " + MEAL_KAYNAGI),
        new Dua(NAMAZDA, "Selam", "السَّلاَمُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                "Esselâmü aleyküm ve rahmetullâh.",
                "Allah'ın selamı ve rahmeti üzerinize olsun.",
                "Namazın sonunda önce sağa, sonra sola dönülerek söylenir.",
                "Ebû Dâvûd, 996; Müslim, 582"),

        new Dua(VITIR, "Kunut Duası 1",
                "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنَسْتَهْدِيكَ، وَنُؤْمِنُ بِكَ "
                        + "وَنَتُوبُ إِلَيْكَ، وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ "
                        + "كُلَّهُ، نَشْكُرُكَ وَلَا نَكْفُرُكَ، وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ",
                "Allâhümme innâ neste'înüke ve nestağfirüke ve nestehdîk. Ve nü'minü bike ve "
                        + "netûbü ileyk. Ve netevekkelü aleyke ve nüsnî aleyke'l-hayra küllehû. "
                        + "Neşkürüke ve lâ nekfürük. Ve nahleu ve netrükü men yefcürük.",
                "Allah'ım! Senden yardım isteriz, bizi bağışlamanı isteriz, bizi doğru yola "
                        + "iletmeni isteriz. Sana inanırız, sana tövbe ederiz, sana güveniriz. "
                        + "Bütün hayırları sana yakıştırarak seni överiz. Sana şükrederiz, "
                        + "nimetlerini inkâr etmeyiz. Sana karşı geleni bırakır, ondan "
                        + "uzaklaşırız.",
                "Vitir namazının üçüncü rekâtında, kıraatten sonra tekbir alınarak, rükûdan önce "
                        + "okunur (Hanefî mezhebi).",
                "Hanefî mezhebinde okunan metin. Benzer bir metin Hısnü'l-Müslim'de (118) "
                        + "geçer; Buhârî ve Müslim'de yoktur."),
        new Dua(VITIR, "Kunut Duası 2",
                "اللَّهُمَّ إِيَّاكَ نَعْبُدُ، وَلَكَ نُصَلِّي وَنَسْجُدُ، وَإِلَيْكَ نَسْعَى "
                        + "وَنَحْفِدُ، نَرْجُو رَحْمَتَكَ وَنَخْشَى عَذَابَكَ، إِنَّ عَذَابَكَ "
                        + "بِالْكُفَّارِ مُلْحِقٌ",
                "Allâhümme iyyâke na'büdü ve leke nusallî ve nescüdü ve ileyke nes'â ve "
                        + "nahfid. Nercû rahmeteke ve nahşâ azâbek. İnne azâbeke bi'l-küffâri "
                        + "mülhık.",
                "Allah'ım! Yalnız sana ibadet ederiz. Namazı yalnız senin için kılar, yalnız sana "
                        + "secde ederiz. Yalnız sana koşar, sana yaklaştıran işlere çalışırız. "
                        + "Rahmetini umar, azabından korkarız. Şüphesiz senin azabın kâfirlere "
                        + "ulaşacaktır.",
                "Kunut Duası 1'den hemen sonra okunur.",
                "Hanefî mezhebinde okunan metin. Benzer bir metin Hısnü'l-Müslim'de (118) "
                        + "geçer; Buhârî ve Müslim'de yoktur."),

        new Dua(SURELER, "Fîl Sûresi", KuranMetni.FIL_AR,
                "Elem tera keyfe fe'ale rabbüke bi-ashâbi'l-fîl. Elem yec'al keydehüm fî "
                        + "tadlîl. Ve ersele aleyhim tayran ebâbîl. Termîhim bi-hicâratin min "
                        + "siccîl. Fe-cealehüm ke-asfin me'kûl.",
                KuranMetni.FIL_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Fîl Sûresi (105). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Kureyş Sûresi", KuranMetni.KUREYS_AR,
                "Li-îlâfi Kureyş. Îlâfihim rihlete'ş-şitâi ve's-sayf. Fe'l-ya'büdû rabbe "
                        + "hâze'l-beyt. Ellezî et'amehüm min cû'in ve âmenehüm min havf.",
                KuranMetni.KUREYS_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Kureyş Sûresi (106). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Mâûn Sûresi", KuranMetni.MAUN_AR,
                "Era'eyte'llezî yükezzibü bi'd-dîn. Fe-zâlike'llezî yedu'u'l-yetîm. Ve lâ "
                        + "yehuddu alâ ta'âmi'l-miskîn. Fe-veylün li'l-musallîn. Ellezîne hüm an "
                        + "salâtihim sâhûn. Ellezîne hüm yürâûn. Ve yemneûne'l-mâûn.",
                KuranMetni.MAUN_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Mâûn Sûresi (107). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Kevser Sûresi", KuranMetni.KEVSER_AR,
                "İnnâ a'taynâke'l-kevser. Fe-salli li-rabbike ve'nhar. İnne şânieke "
                        + "hüve'l-ebter.",
                KuranMetni.KEVSER_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Kevser Sûresi (108). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Kâfirûn Sûresi", KuranMetni.KAFIRUN_AR,
                "Kul yâ eyyühe'l-kâfirûn. Lâ a'büdü mâ ta'büdûn. Ve lâ entüm âbidûne mâ a'büd. "
                        + "Ve lâ ene âbidün mâ abedtüm. Ve lâ entüm âbidûne mâ a'büd. Leküm "
                        + "dînüküm ve liye dîn.",
                KuranMetni.KAFIRUN_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Kâfirûn Sûresi (109). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Nasr Sûresi", KuranMetni.NASR_AR,
                "İzâ câe nasrullâhi ve'l-feth. Ve raeyte'n-nâse yedhulûne fî dînillâhi efvâcâ. "
                        + "Fe-sebbih bi-hamdi rabbike ve'stağfirh, innehû kâne tevvâbâ.",
                KuranMetni.NASR_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Nasr Sûresi (110). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Tebbet Sûresi", KuranMetni.TEBBET_AR,
                "Tebbet yedâ ebî lehebin ve tebb. Mâ ağnâ anhü mâlühû ve mâ keseb. Se-yaslâ "
                        + "nâran zâte leheb. Ve'mraetühû hammâlete'l-hatab. Fî cîdihâ hablün "
                        + "min mesed.",
                KuranMetni.TEBBET_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Tebbet Sûresi (111). " + MEAL_KAYNAGI),
        new Dua(SURELER, "İhlâs Sûresi", KuranMetni.IHLAS_AR,
                "Kul hüvallâhü ehad. Allâhü's-samed. Lem yelid ve lem yûled. Ve lem yekün lehû "
                        + "küfüven ehad.",
                KuranMetni.IHLAS_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, İhlâs Sûresi (112). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Felak Sûresi", KuranMetni.FELAK_AR,
                "Kul eûzü bi-rabbi'l-felak. Min şerri mâ halak. Ve min şerri ğâsikın izâ "
                        + "vekab. Ve min şerri'n-neffâsâti fi'l-ukad. Ve min şerri hâsidin izâ "
                        + "hased.",
                KuranMetni.FELAK_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Felak Sûresi (113). " + MEAL_KAYNAGI),
        new Dua(SURELER, "Nâs Sûresi", KuranMetni.NAS_AR,
                "Kul eûzü bi-rabbi'n-nâs. Meliki'n-nâs. İlâhi'n-nâs. Min şerri'l-vesvâsi'l-"
                        + "hannâs. Ellezî yüvesvisü fî sudûri'n-nâs. Mine'l-cinneti ve'n-nâs.",
                KuranMetni.NAS_MEAL, "Fâtiha'dan sonra okunabilir (zamm-ı sûre).",
                "Kur'an, Nâs Sûresi (114). " + MEAL_KAYNAGI),

        new Dua(SONRA, "Allâhümme Ente's-Selâm",
                "اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ تَبَارَكْتَ ذَا الْجَلاَلِ "
                        + "وَالإِكْرَامِ",
                "Allâhümme ente's-selâmü ve minke's-selâm. Tebârekte ze'l-celâli ve'l-ikrâm.",
                "Allah'ım! Sen her türlü eksiklikten uzaksın (Selâm'sın); esenlik ancak "
                        + "sendendir. Ey celal ve ikram sahibi! Sen ne yücesin.",
                "Farz namazdan sonra selam verince okunur.",
                "Müslim, 591"),
        new Dua(SONRA, "Âyete'l-Kürsî", KuranMetni.AYETEL_KURSI_AR,
                "Allâhü lâ ilâhe illâ hüve'l-hayyü'l-kayyûm. Lâ te'huzühû sinetün ve lâ nevm. "
                        + "Lehû mâ fi's-semâvâti ve mâ fi'l-ard. Men ze'llezî yeşfeu indehû illâ "
                        + "bi-iznih. Ya'lemü mâ beyne eydîhim ve mâ halfehüm. Ve lâ yuhîtûne "
                        + "bi-şey'in min ilmihî illâ bimâ şâ'. Vesia kürsiyyühü's-semâvâti "
                        + "ve'l-ard. Ve lâ yeûdühû hifzuhümâ. Ve hüve'l-aliyyü'l-azîm.",
                KuranMetni.AYETEL_KURSI_MEAL,
                "Namazdan sonra yapılan tesbihatta okunur.",
                "Kur'an, Bakara Sûresi 2:255. " + MEAL_KAYNAGI),
    };
}
