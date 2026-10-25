# Namaz Vakti Uygulamalarının Teknik ve Fonksiyonel Analizi — Rapor (2. sürüm)

- **İnceleme tarihi:** 25 Ekim 2026
- **Ana inceleme konusu (bu sürüm):** Uygulama A — *Ezan Vakti Pro: Namaz, Kuran* (Deen Studios LLC-FZ)
- **Uygulama B:** *Namaz Vakitleri: Alarm & Kıble* (NvApps Ltd) — son talepte tanımlanmadı; ilk rapordaki bulgular korunmuştur (Bölüm 4).
- **Raporun türü:** Ara rapor. APK analizi ve çalışma zamanı testi **yine yapılamadı** (APK yok, uygulama mağazaları ve APK siteleri bu ortamdan engelli). Nedenleri Bölüm 2 ve 11'de.

### Bu sürümde ne değişti?

| Yeni kanıt | Nereden |
|---|---|
| Google Play ekran görüntüsünden okunan veriler (puan, yorum, boyut, reklam, tanıtım görselleri) | Sizin gönderdiğiniz ekran görüntüsü |
| Güncel sürüm **9.5.9-ps (21 Ekim 2026)** ve bu sürümün izin listesi | AppBrain, APKPure, Aptoide (arama özetleri) |
| 2021–2022 sürümlerinde bulunan **takip araçları (tracker)** listesi | Exodus Privacy statik APK analiz raporları (arama özetleri) |
| Uygulamanın tanıtım görselindeki vakitlerin **Diyanet tipi hesapla ±1 dakika uyuştuğu** | Bu oturumda çalıştırdığım karşılaştırma (Bölüm 5.2) |
| Diyanet'in resmî vakit servisinin (**Awqat Salah**) teknik yapısı | Açık kaynak `pydiyanet` istemcisinin kaynak kodunu okudum (Bölüm 5.3) |
| Türk şirketi **MaviAy Yazılım** ile bağlantı izleri | App Store ve üçüncü taraf kayıtları (arama özetleri) |

---

## Kanıt türleri ve güven düzeyleri

| Etiket | Anlamı |
|---|---|
| **[Ekran]** | Sizin gönderdiğiniz Google Play ekran görüntüsü. Çekim tarihi bilinmiyor (saat 10:45 görünüyor). Görüntüdeki tanıtım görselleri geliştiricinin reklam malzemesidir. |
| **[AÖ]** | Web arama aracının verdiği özet. **Sayfanın kendisini açamadım.** En fazla "Kısmen doğrulandı" sayılır. |
| **[3. taraf]** | AppBrain, APKPure, Aptoide gibi siteler. Resmî değil; eski sürüme ait olabilir. Bu raporda bunlara da yalnızca arama özetiyle ulaştım. |
| **[Exodus]** | Exodus Privacy: Google Play'den indirilen APK'nın **statik analizi**. Exodus'un kendi uyarısı: bulunan iz "takip aracının çalıştığının kanıtı değildir". Bu raporda yalnızca arama özetiyle ulaşıldı. |
| **[Kod]** | Bu oturumda **kendim indirip okuduğum veya çalıştırdığım** açık kaynak kod (uygulamanın kendi kodu değil). |
| **[Resmî Android]** | developer.android.com belgeleri (arama özetiyle). Platformun genel kuralıdır; uygulamanın bunu kullandığını göstermez. |
| **[Çıkarım]** | Eldeki verilerden yaptığım, doğrulanmamış değerlendirme. |

Güven düzeyleri: **Doğrulandı / Kısmen doğrulandı / Doğrulanamadı / Test edilmedi / Bilinmiyor.**

> **Önemli:** Uygulama A ile ilgili hiçbir bulgu "Doğrulandı" seviyesinde değildir; çünkü uygulamanın APK'sına, koduna veya çalışan hâline erişemedim. "Doğrulandı" etiketi yalnızca bu oturumda kendim yaptığım işlemler (ör. ekran görüntüsünü okumak, karşılaştırma hesabı) için kullanıldı.

---

## 1. Yönetici özeti

1. **APK hâlâ yok.** Depoda Uygulama A veya B'nin APK'sı bulunmuyor. Google Play, APK siteleri (APKMirror, APKPure, APKCombo, F-Droid), Exodus, AppBrain, geliştirici siteleri ve Diyanet sunucuları bu bulut ortamının ağ kuralı tarafından **engelleniyor (403)**. Sayfa okuma aracı (WebFetch) de çalışmıyor (DNS hatası). Bu yüzden **APK analizi, ağ trafiği analizi ve çalışma zamanı testleri yapılmadı.**
2. **Uygulamanın kimliği (Kısmen doğrulandı):** Paket adı `com.mobilexsoft.ezanvakti`; geliştirici Deen Studios LLC-FZ; güncel sürüm **9.5.9-ps, 21 Ekim 2026**; 43 izin, 65 kütüphane (AppBrain). [3. taraf][AÖ]
3. **Ekran görüntüsü:** 4,6 yıldız, "1 Mn" yorum, PEGI 3, 48 MB, **reklam içerir**, **uygulama içi satın alma** var. Tanıtım görselleri: birden çok vakit gösterim teması, "19 hafız" ile Kur'an, tecvid renklendirme, e-kitap "Kütüphane". Kur'an ve Kütüphane görselleri **iPhone** ekranından alınmış; Android ekranı farklı olabilir. [Ekran]
4. **Vakit hesabı:** Tanıtım görselindeki 5 vakit, İstanbul için 20 Mart 2023'te **Türkiye/Diyanet tipi** ayarlarla hesaplanan değerlerle **en fazla 1 dakika** fark gösteriyor; diğer yöntemlerde fark 6–17 dakika. Bu, uygulamanın Türkiye'de Diyanet'e uygun vakit gösterdiğini **destekliyor**, ama vakitlerin **sunucudan mı geldiğini yoksa telefonda mı hesaplandığını göstermez.** [Çıkarım + Kod]
5. **Alarm sistemi (izin listesinden ipuçları):** Kesin zamanlı alarm izinleri (`SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`), açılışta çalışma, ön plan medya servisi, pil optimizasyonundan muafiyet isteme, Rahatsız Etmeyin erişimi izinleri listede var. Bunlar ezanın kesin saatte çalınması için tipik yapı taşlarıdır; **kodda nasıl kullanıldıkları doğrulanamadı.** [3. taraf][Çıkarım]
6. **Gizlilik (dikkat):** 2021–2022 sürümlerinde Exodus; Google AdMob, Facebook Ads, Firebase, Crashlytics, Huawei HMS ve **konum odaklı** "Blesh" ile reklam kategorisinde "Coulus Coelib" izlerini bulmuş. Uygulama **arka planda konum** iznini istiyor. Güncel sürümde hangi takip araçlarının bulunduğu **doğrulanamadı.** [Exodus][AÖ]
7. **Sahiplik:** Google Play'de Deen Studios LLC-FZ (BAE) görünüyor. App Store'daki bazı kayıtlarda ve eski APK sitelerinde Türk şirketi **MaviAy Yazılım Ltd. Şti.** görünüyor. Paket adında ise "mobilexsoft" geçiyor. Bu üç ad arasındaki ilişkiyi (devir, ortaklık vb.) **doğrulayamadım.**
8. **En önemli açık sorular:** Vakitler sunucudan mı geliyor, cihazda mı hesaplanıyor? Diyanet verisi hangi servis üzerinden alınıyor? Alarm hangi Android mekanizmasıyla kuruluyor? Bunlar **APK veya trafik incelemesi olmadan cevaplanamaz. Bilmiyorum.**

---

## 2. İncelenen dosyalar ve kaynaklar

### 2.1 Bu oturumda gerçekten yaptığım işlemler (25 Ekim 2026)

| # | İşlem | Sonuç |
|---|---|---|
| 1 | Proje klasöründeki tüm dosyaları listeledim (`find`) | İlk rapor (`RAPOR.md`), önceki oturumda yazılmış kendi uygulamamız (*Kolay Namaz Vakti*, `app/`, `dist/KolayNamazVakti.apk`) ve ekran görüntüleri var. **Uygulama A veya B'ye ait APK, AAB, kaynak kod veya manifest yok.** |
| 2 | Gönderdiğiniz ekran görüntüsünü inceledim; küçük yazıları büyütüp okudum (Python/PIL ile kırpma) | Bölüm 3.1'deki veriler çıkarıldı. |
| 3 | 21 adrese `curl` ile bağlanmayı denedim | play.google.com, appbrain.com, reports.exodus-privacy.eu.org, apkmirror.com, apkpure.com, apkcombo.com, f-droid.org, ezanvaktipro.com, deen-studios.com, awqatsalah.diyanet.gov.tr, namazvakitleri.diyanet.gov.tr, api.aladhan.com, dl.google.com → **403 (ağ kuralı engeli)**. pypi.org, registry.npmjs.org, repo1.maven.org → erişilebilir. |
| 4 | WebFetch ile Google Play sayfasını açmayı denedim | **DNS hatası** (`getaddrinfo ENOTFOUND play.google.com`). |
| 5 | WebSearch ile 33 arama yaptım | Çalıştı. Mağaza, AppBrain, Exodus ve Android belgelerine ait bilgiler bu aramaların **özetlerinden** geliyor. |
| 6 | PyPI'den `pydiyanet 0.1.2` (MIT) kaynak paketini indirdim, SHA-256 özetini PyPI kaydıyla karşılaştırdım (eşleşti), **çalıştırmadan** okudum | Diyanet Awqat Salah servisinin adresleri ve giriş yöntemi (Bölüm 5.3). |
| 7 | npm'den `adhan 4.4.3` (MIT) paketini indirdim; `tools/ekran_vakit_karsilastir.js` betiğini yazıp çalıştırdım | Tanıtım görselindeki vakitler 4 yöntemle karşılaştırıldı (Bölüm 5.2). |

### 2.2 Bulunmayan / erişemediğim dosyalar

- Uygulama A ve B'nin **APK** dosyaları (bölünmüş APK'lar dahil: `base.apk`, `split_config.*.apk`) — **yok**.
- **AAB** — yok (geliştirici dışında elde edilemez).
- **Kaynak kod / AndroidManifest.xml** — yok. İki uygulamanın da açık kaynak olduğuna dair bir iz **bulamadım**.
- **Uygulama B'nin ekran görüntüsü** — bu talepte gönderilmedi.
- **Not:** Görev metninde hem "iki uygulama" hem "üç uygulama" geçiyor, ama yalnızca Uygulama A tanımlanmış. Başka bir uygulama kastediliyorsa adını bilmiyorum.

---

## 3. Uygulama A: Ezan Vakti Pro: Namaz, Kuran (Deen Studios LLC-FZ)

### 3.1 Ekran görüntüsünden okunan bilgiler [Ekran]

| Bilgi | Görüntüde okunan | Not / güven |
|---|---|---|
| Uygulama adı | Ezan Vakti Pro: Namaz, Kuran | Doğrulandı (görüntüde net) |
| Geliştirici | Deen Studios LLC-FZ | Doğrulandı (görüntüde net) |
| Reklam / satın alma | "Reklam içerir • Uygulama içi satın alma" | Doğrulandı (görüntüde net) |
| Puan / yorum | 4,6 ★, "1 Mn yorum" | Çekim anına ait. Arama özetinde İngilizce Play sayfası için 4,7 ★ ve 1,3 milyon+ yorum geçiyor [AÖ]. Fark, bölge veya tarih farkından olabilir. |
| Yaş derecesi | PEGI 3 | Doğrulandı (görüntüde net) |
| Boyut | 48 MB | Google Play bu sayıyı **o telefona göre** indirme boyutu olarak gösterir; kurulu boyut daha büyük olabilir. [Çıkarım] |
| Kurulu mu? | "Yükle" düğmesi görünüyor | Çekim anında o telefonda **kurulu değildi**. |
| Kategori etiketleri | "yaşam tarzı kategorisindeki ücretsiz en…" (kesik), "Kitaplar ve Başvuru Kaynakları" | Yazının devamı kesik; tam sırası okunamadı. |
| Kısa açıklama | "En Popüler İslami Yaşam yardımcısı. Namaz Vakitleri, Kuran-ı Kerim ve Dahası" | Doğrulandı (görüntüde net) |
| Üstteki "11.336" | Google Play Puanları simgesiyle gösteriliyor | Büyük olasılıkla **sizin hesabınızın Play Puanları bakiyesi**; uygulamaya ait bir veri değil. [Çıkarım] |
| Tanıtım görseli 1 | "İslami Yaşam Yardımcınız"; koyu tema, yay şeklinde vakit göstergesi ("İkindi"), geri sayım "01:01:26", üstte hava durumu simgeleri, tarih "2? Mart 2023 – 2? Şaban 1444" | Gün rakamları bulanık. Hava durumu simgeleri, Play açıklamasındaki "hava durumu" özelliğiyle uyumlu [AÖ]. |
| Tanıtım görseli 2 | "Birden çok vakit gösterim şekli"; lacivert ve yeşil (kara tahta) temalar; İmsak 05:36, Güneş 07:02, İkindi 16:40, Akşam 19:22, Yatsı 20:42 | Öğle vakti okunamadı. Bu vakitler Bölüm 5.2'de karşılaştırıldı. |
| Tanıtım görseli 3 | "Kuran-ı Kerim — 19 Hafız, Oku Takip, Meal Seslendirme, Te(c)vid Renklendirme"; "İstanbul" yazan ekran; Fatiha suresi | Kur'an ekranı **iPhone çerçevesinde** (Dynamic Island görünüyor). "Tecvid" kelimesi görselde "Tevid" gibi okunuyor (çözünürlük düşük). |
| Tanıtım görseli 4 | "Kütüphane — Özenle hazırlanmış birbirinden değerli e-kitaplar"; kitap kapakları (okunabilenler: Namaz Hocası, Oruç, Tesbihat, Kuran-ı Kerim); "Sureler, Cüzler, Yer İmleri" | **iPhone çerçevesinde.** Diğer kapak adları okunamadı. |

### 3.2 Kimlik

| Bilgi | Değer | Kaynak | Güven |
|---|---|---|---|
| Paket adı | `com.mobilexsoft.ezanvakti` | Google Play adresi, AppBrain, Exodus [AÖ] | Kısmen doğrulandı |
| Geliştirici | Deen Studios LLC-FZ | [Ekran] + AppBrain [AÖ] | Ekranda doğrulandı; paket adıyla eşleşmesi Kısmen doğrulandı |
| İngilizce adı | "Azan Time Pro – Prayer Times" / "Azan Time Pro" | AppBrain, APKPure [AÖ] | Kısmen doğrulandı |
| Güncel sürüm | **9.5.9-ps — 21 Ekim 2026** | AppBrain, APKPure, Aptoide (üçü de aynı) [AÖ] | Kısmen doğrulandı. (İlk raporda 9.5.5-ps / 10 Eylül 2026 bulunmuştu.) |
| Minimum Android | Aptoide: Android 11+; daha önce 9.5.2-ps için Android 9+; APKPure "undefined" | [AÖ][3. taraf] | **Çelişkili** — Doğrulanamadı |
| Hedef SDK | — | — | Bilinmiyor |
| İndirme | 10 milyon+ (Play), yaklaşık 40 milyon (AppBrain toplam tahmini) | [AÖ] | Kısmen doğrulandı |
| İzin / kütüphane sayısı | 43 izin · 65 kütüphane | AppBrain [AÖ] | Kısmen doğrulandı. Başka bir APK sitesi 38 izin diyor. |
| Aile uygulamaları | Ezan Vakti Pro Plus (`com.mobilexsoft.ezanvaktiproplus`, ücretli, reklamsız), Azan Time Lite (`com.mobilexsoft.ezanvaktilite`) | İlk rapor [AÖ] | Kısmen doğrulandı |

### 3.3 Sahiplik ve geliştirici geçmişi

| Bulgu | Kaynak | Güven |
|---|---|---|
| Google Play ve AppBrain'de geliştirici: Deen Studios LLC-FZ | [Ekran][AÖ] | Kısmen doğrulandı |
| App Store'da "Azan Time Pro Plus" (id442401423) satıcısı bir mağazada **Deen Studios L.L.C-FZ**, ABD mağazasında **MAVIAY YAZILIM DONANIM DANISMANLIK HIZMETLERI** | [AÖ] | Kısmen doğrulandı (mağazaya göre farklı) |
| App Store'da "Azan Time Pro: Holy Quran" (id437447439) MaviAy adına; gizlilik bağlantısı ezanvaktipro.com | [AÖ] | Kısmen doğrulandı |
| Tamindir'deki "Ezan Vakti Pro" APK sayfasında üretici: MaviAy; BlueStacks sayfası "Deen Studios" diyor ama açıklamada MaviAy geçiyor | [AÖ][3. taraf] | Kısmen doğrulandı |
| "Mobilexsoft" adı hiçbir kaynakta şirket olarak geçmedi; yalnızca paket adında var | — | Bilinmiyor |
| Bir devir/satın alma haberi | Arandı, **bulunamadı** | Doğrulanamadı |

**Çıkarım:** Uygulama muhtemelen Türk kökenli (MaviAy) bir ürün olup yayıncılığı sonradan BAE merkezli Deen Studios'a geçmiş olabilir. **Bu bir tahmindir; doğrulayamadım.**

### 3.4 Özellik tablosu

"Nasıl ulaşılır" sütunu için uygulamayı çalıştıramadığımdan çoğu hücre **Bilinmiyor**dur.

| # | Özellik | Var mı? | Nasıl çalışır (bilinen kadarıyla) | İzin / donanım | İnternet | Kaynak / güven |
|---|---|---|---|---|---|---|
| 1-2 | Vakit tablosu, 6 vakit | Var | Görselde İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı; aylık imsakiye (beyan) | — | Bilinmiyor | [Ekran] + [AÖ] — Kısmen |
| 3 | Ezan sesi / vakit bildirimi | Var (beyan) | "Salah alarms", ezan sesleri (beyan) | `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` listede | Bilinmiyor | [AÖ][3. taraf] — Kısmen |
| 4 | Vakit öncesi hatırlatma | Muhtemel | iOS benzer sürümde 45/30/15/5 dk (ilk rapor) | — | — | Android için Doğrulanamadı |
| 5 | Alarm kurma | Var (beyan: "Salah alarms", "Sahur alarmı") | Kesin alarm izinleri listede; `SET_ALARM` izni de var (sistem saat uygulamasına alarm ekleme izni) | `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `SET_ALARM` | Gerekmez [Çıkarım] | [AÖ][3. taraf] — Kısmen |
| 6 | Konuma göre otomatik şehir | Var (beyan: "GPS ile otomatik") | — | Yaklaşık + hassas + **arka plan** konum | GPS için gerekmez; adres/şehir adı bulmak için gerekebilir [Çıkarım] | [AÖ][3. taraf] — Kısmen |
| 7 | Şehir/ilçe seçimi | Muhtemel | Görselde "İstanbul" yazıyor | — | — | [Ekran] — Kısmen; ilçe düzeyi Doğrulanamadı |
| 8 | Hesaplama yöntemi / mezhep | Var: ISNA, MWL, Umm al-Qura ve 15+ yöntem (beyan); Türkiye için Diyanet'e uygun değerler (Bölüm 5.2) | — | — | — | [AÖ] + [Kod] — Kısmen. **Hanefi ikindi seçeneği: Doğrulanamadı** |
| 9-10 | Kıble pusulası ve harita | Var (beyan: "Qibla compass" + "Qibla map") | — | Manyetometre/ivmeölçer [Çıkarım]; harita için internet [Çıkarım] | Harita için muhtemelen | [AÖ] — Kısmen |
| 11 | Hicri takvim / dini günler | Hicri takvim var (beyan + görselde Hicri tarih) | — | — | — | [Ekran][AÖ] — Kısmen; dini günler listesi Doğrulanamadı |
| 12 | Kur'an, meal | Var: tam Kur'an, 20+ dilde meal, tecvid renklendirme, oku-takip, otomatik kaydırma | Görselde Fatiha suresi, sure/cüz/yer imi | — | — | [Ekran][AÖ] — Kısmen |
| 13 | Sesli Kur'an | Var: "19 hafız", "meal seslendirme" | — | Depolama izni listede (indirme için olabilir [Çıkarım]) | Muhtemelen gerekir [Çıkarım]; çevrimdışı ses Doğrulanamadı | [Ekran] — Kısmen (geliştirici beyanı) |
| 14 | Kütüphane / mushaf | Var: e-kitap kütüphanesi (Namaz Hocası, Oruç, Tesbihat vb.) | — | — | Bilinmiyor | [Ekran][AÖ] — Kısmen; farklı mushaf seçeneği Doğrulanamadı |
| 15 | Widget | Bilinmiyor | Play açıklaması özetinde widget **geçmedi**; Wear OS "komplikasyon" desteği beyan ediliyor | — | — | Android widget: Doğrulanamadı |
| 16 | Kilit ekranı / üstte gösterim | Belirsiz | "Diğer uygulamaların üzerinde gösterme" izni var (tam ekran ezan ekranı için kullanılıyor olabilir [Çıkarım]) | `SYSTEM_ALERT_WINDOW` | — | İşlevi Doğrulanamadı |
| 17 | Tema / kişiselleştirme | Var: birden çok vakit gösterim teması; duvar kâğıdı | — | `SET_WALLPAPER` | — | [Ekran] + [3. taraf] — Kısmen |
| 18 | Reklam / satın alma | Var | Reklamsız ayrı "Pro Plus" uygulaması; Google Play faturalandırma | `com.android.vending.BILLING`, `AD_ID` | Gerekir | [Ekran] — Google Play etiketinde **doğrulandı**; reklam yoğunluğu ölçülmedi; fiyat **Bulamadım** |
| 19 | Hesap / bulut senk. | Bilinmiyor | "Hatim topluluğu" özelliği hesap gerektirebilir [Çıkarım] | — | — | Doğrulanamadı |
| 20 | Çevrimdışı | Vakitler için beyan var ("internet gerekmez") | — | — | — | [AÖ] — Kısmen |
| Ek | Hava durumu | Var (beyan + görselde simgeler) | — | Konum [Çıkarım] | **Gerekir** [Çıkarım] | [Ekran][AÖ] — Kısmen |
| Ek | Cami bulucu, zikirmatik, kaza takibi | Var (beyan) | — | Konum (cami bulucu) | Cami bulucu için muhtemelen | [AÖ] — Kısmen |
| Ek | "Namazdayım" / camide otomatik sessize alma | Eski açıklamalarda var | "Konumunuza bağlı olarak cami içinde olduğunuzu anlayıp telefonu sessize alır" (APK sitesi açıklaması) | `ACCESS_NOTIFICATION_POLICY`, arka plan konum | — | [AÖ][3. taraf] — Kısmen; güncel sürümde Doğrulanamadı |
| Ek | Wear OS (akıllı saat) | Var (beyan) | — | — | — | [AÖ] — Kısmen |

### 3.5 İzinler (sürüm 9.5.9-ps — üçüncü taraf listelerine göre, resmî değil)

Aşağıdaki liste APKPure ve AppBrain'in arama özetlerinden derlenmiştir. Özetlerde izinler bazen düz Türkçe/İngilizce açıklamayla geçti; bunları Android'deki izin adlarına **ben eşleştirdim.** "Olası amaç" sütunu tamamen **çıkarımdır**.

| İzin | Ne işe yarar (Android genel) | Bu uygulamada olası amaç [Çıkarım] | Dikkat |
|---|---|---|---|
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Hassas / yaklaşık konum | Vakit hesabı, kıble, cami bulucu, hava durumu | — |
| `ACCESS_BACKGROUND_LOCATION` | Uygulama kapalıyken konum | Seyahatte şehir değişince vakit güncelleme; camide sessize alma | **Yüksek** — kullanıcı "Her zaman izin ver" seçmedikçe çalışmaz |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Ağ erişimi | Reklam, Kur'an sesi, hava durumu, (varsa) vakit indirme | — |
| `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` | Saniyesi saniyesine alarm | Ezan vaktinde tam zamanında uyarı | — |
| `RECEIVE_BOOT_COMPLETED` | Telefon açılınca çalışma | Yeniden başlatmadan sonra alarmları yeniden kurma | — |
| `WAKE_LOCK`, `VIBRATE` | Ekran kapalıyken işlemciyi uyanık tutma; titreşim | Ezan çalarken telefonun uyumaması | — |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Android 14+ için "medya oynatma" türünde ön plan servisi | Ezan sesini kesilmeden çalmak | — |
| `POST_NOTIFICATIONS` | Android 13+ bildirim izni | Vakit bildirimleri | — |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Pil optimizasyonundan muaf tutulmayı isteme | Alarmların gecikmemesi | Pil tüketimini artırabilir |
| `ACCESS_NOTIFICATION_POLICY` | Rahatsız Etmeyin ayarını değiştirme | Namaz sırasında / camide sessize alma | — |
| `MODIFY_AUDIO_SETTINGS` | Ses ayarlarını değiştirme | Sessize alma veya ezan ses seviyesi | — |
| `READ_PHONE_STATE` | Telefon durumu (arama var mı vb.) | Arama gelince ezanı durdurma olabilir | **Orta** — eski Android'lerde cihaz kimliğine erişim sağlayabiliyordu |
| `SYSTEM_ALERT_WINDOW` | Diğer uygulamaların üzerinde gösterme | Tam ekran ezan/vakit ekranı | **Orta** |
| `PACKAGE_USAGE_STATS` | Uygulama kullanım istatistikleri (kullanıcının ayarlardan ayrıca vermesi gerekir) | **Bilinmiyor** | **Yüksek** — amacı belirsiz |
| `SET_WALLPAPER` | Duvar kâğıdı değiştirme | Duvar kâğıdı özelliği | — |
| `SET_ALARM` | Saat uygulamasına alarm ekleme | Sahur alarmı olabilir | — |
| Depolama (okuma/yazma) | Paylaşılan depolama | Kur'an ses dosyaları / e-kitaplar | — |
| `com.android.vending.BILLING` | Google Play ödeme | Uygulama içi satın alma | — |
| `com.google.android.gms.permission.AD_ID`, `ACCESS_ADSERVICES_*` | Reklam kimliği; Android Privacy Sandbox reklam API'leri | Reklam | **Orta** — reklam takibi |
| `com.amazon.privacypass.ATTEST` | Amazon'a ait bir izin | Amazon kaynaklı bir reklam SDK'sı olabilir | **Doğrulanamadı** |

**Exodus'un 2022 raporunda** ayrıca `QUERY_ALL_PACKAGES` (telefondaki tüm uygulamaları listeleme) izni görülmüş [Exodus][AÖ]; güncel sürümde olup olmadığı **doğrulanamadı.**

---

## 4. Uygulama B: Namaz Vakitleri: Alarm & Kıble (NvApps Ltd)

> **Not:** Son talepte Uygulama B tanımlanmadı ve ekran görüntüsü gönderilmedi. Bu bölüm, aynı gün yazılan **ilk rapordaki bulguların** kısa özetidir; bu oturumda **yeniden araştırılmadı.**

| Bilgi | Değer | Kaynak | Güven |
|---|---|---|---|
| Paket adı | `com.simsekburak.android.namazvakitleri` | Google Play adresi, AppBrain [AÖ] | Kısmen doğrulandı |
| Geliştirici | NvApps Ltd (Londra); bazı sitelerde "Burak Şimşek" | [AÖ][3. taraf] | Kısmen doğrulandı |
| Sürüm | 6.22.915 (20 Şubat 2026, AppBrain) | [3. taraf] | Kısmen; daha yeni sürüm olabilir |
| Minimum Android | 7.0+ (yeni sürümler) / 5.0+ (AppBrain) | [3. taraf] | Çelişkili |
| İzin / kütüphane | 23 izin (6.18.900), 53 kütüphane | [3. taraf] | Kısmen doğrulandı |
| Özellikler (beyan) | Aylık vakit tablosu, farklı müezzin sesleri, önce/vaktinde/sonra hatırlatma, kıble, dini günler, imsakiye, "Kur'an ve dua oynatıcısı", widget'lar, reklam + Pro üyelik | [AÖ] | Kısmen doğrulandı |
| Diyanet | Mağaza metninde "Diyanet ezan vakti" ifadesi; veri kaynağı **bulunamadı** | [AÖ] | Kısmen |
| Gizlilik | Gizlilik politikası (25 Ekim 2022): IP, cihaz bilgisi, hassas/yaklaşık konum toplanabilir; App Store'da "veri toplanmaz" beyanı → **çelişkili** | [AÖ] | Kısmen doğrulandı |

Uygulama B'nin hesaplama yöntemi, alarm yapısı, takip araçları ve izin listesi **Bilinmiyor**. Ayrıntılı tablolar için bu dosyanın git geçmişindeki ilk sürüme bakılabilir (`git show 071afdc:RAPOR.md`).

---

## 5. Namaz vakti hesaplama sistemi

### 5.1 Uygulama A — soru bazında durum

| Soru | Bulgu | Güven |
|---|---|---|
| Sunucudan mı, cihazda mı? | **Bilmiyorum.** "GPS ile otomatik hesaplama" ve "15+ yöntem" beyanları cihazda hesaplamayı düşündürüyor; "aylık vakit indirme" (ilk rapor) ise sunucu kullanımını düşündürüyor. İkisinin birlikte kullanılması mümkün. | [AÖ] + [Çıkarım] |
| Kullanılan algoritma / kütüphane | **Bulamadım.** APK'daki sınıf adlarına bakmak gerekir. | Bilinmiyor |
| Diyanet uyumu | Tanıtım görselindeki vakitler Diyanet tipi hesapla ±1 dk uyumlu (Bölüm 5.2). Önceki raporda bir sürüm notunda "GPS Diyanet" adlı bir yöntem izi bulunmuştu [3. taraf]. | Kısmen doğrulandı (uyum); **kaynak servis Doğrulanamadı** |
| Diğer ülke yöntemleri | ISNA, MWL, Umm al-Qura + 15'ten fazla | [AÖ] Kısmen |
| Saat dilimi / yaz saati | Bilinmiyor | — |
| Yüksek enlem | İlk raporda bir sürüm notunda "yüksek enlemler için GPS Diyanet düzeltmesi" izi [3. taraf]; yöntem Bilinmiyor | Doğrulanamadı |
| Hanefi ikindi | Aradım, **bulamadım** | Doğrulanamadı |
| Manuel dakika düzeltmesi | Aradım, **bulamadım** | Doğrulanamadı |
| Konum değişince güncelleme | Arka plan konum izni bunu yapıyor olabilir | [Çıkarım] |
| Çevrimdışı önceki veri | "İnternet bağlantısı gerekmez" beyanı | [AÖ] Kısmen |

### 5.2 Test: Tanıtım görselindeki vakitler hangi yönteme uyuyor?

**Ne yaptım:** Tanıtım görselindeki 5 vakti (İmsak 05:36, Güneş 07:02, İkindi 16:40, Akşam 19:22, Yatsı 20:42), açık kaynak **adhan** kütüphanesiyle (MIT, sürüm 4.4.3) İstanbul için 18–22 Mart 2023 tarihlerinde 4 yöntemle hesaplanan vakitlerle karşılaştırdım.

- **Betik:** `tools/ekran_vakit_karsilastir.js`
- **Varsayımlar:** Şehir **İstanbul** (görsel 3'te "İstanbul" yazıyor; görsel 2'de şehir adı okunamadı), koordinat 41.01384, 28.94966 (GeoNames), tarih **Mart 2023** (görselde "2? Mart 2023 – 2? Şaban 1444"; gün rakamları bulanık).
- "Turkey" yöntemi adhan kütüphanesindeki Türkiye ayarıdır (imsak 18°, yatsı 17°, güneş −7 dk, öğle +5 dk, ikindi +4 dk, akşam +7 dk "temkin" düzeltmeleri). **Diyanet'in resmî takvimi değildir**; Diyanet'e yakın sonuç verdiği bilinen bir yaklaşımdır.

**Çıktıdan seçmeler (fark = hesaplanan − görseldeki, dakika):**

| Tarih | Yöntem | İmsak | Güneş | İkindi | Akşam | Yatsı | En büyük fark |
|---|---|---|---|---|---|---|---|
| 20 Mart 2023 | **Turkey** | 05:36 | 07:01 | 16:41 | 19:23 | 20:43 | **1 dk** |
| 20 Mart 2023 | MWL | 05:36 | 07:08 | 16:37 | 19:16 | 20:43 | 6 dk |
| 20 Mart 2023 | Umm al-Qura | 05:34 | 07:08 | 16:37 | 19:16 | 20:46 | 6 dk |
| 20 Mart 2023 | ISNA | 05:53 | 07:08 | 16:37 | 19:16 | 20:32 | 17 dk |
| 19 Mart 2023 | Turkey | 05:38 | 07:03 | 16:41 | 19:22 | 20:41 | 2 dk |
| 21 Mart 2023 | Turkey | 05:35 | 07:00 | 16:42 | 19:24 | 20:44 | 2 dk |

**Yorum (Çıkarım):** En iyi uyum Türkiye ayarlarıyla ve 20 Mart 2023 tarihinde (en büyük fark 1 dk). Özellikle güneş ve akşam vakitlerindeki 6–7 dakikalık fark, Diyanet'in kullandığı "temkin" düzeltmelerinin görseldeki vakitlerde **bulunduğunu** gösteriyor. Bu, uygulamanın Türkiye'de Diyanet'e uygun vakit gösterdiğini destekler.

**Sınırlar:** (1) Görsel bir reklam malzemesidir, gerçek kullanımı yansıtmayabilir. (2) Şehir ve tarih varsayımdır. (3) Bu test vakitlerin **nereden geldiğini** (Diyanet sunucusu mu, telefonda hesap mı) **göstermez**. (4) Diyanet'in o güne ait resmî İstanbul vakitleriyle karşılaştırma yapılamadı (Diyanet sitesi engelli).

### 5.3 Diyanet'in resmî vakit servisi (genel bilgi, uygulamaya özel değil)

`pydiyanet` 0.1.2 (MIT, PyPI'ye 28 Şubat 2026'da yüklenmiş) kaynak kodunu okudum [Kod — `pydiyanet/client.py`, satır 15–27]:

- Servis adresi: `https://awqatsalah.diyanet.gov.tr`
- Giriş: `/Auth/Login` adresine **e-posta ve şifre** gönderiliyor, karşılığında erişim anahtarı (token) alınıyor. Kodda erişim anahtarı süresi 30 dk, yenileme anahtarı 45 dk olarak yazılmış ("belgeye göre" notuyla).
- Vakitler: `/api/PrayerTime/Daily/{konum_kimliği}` — **koordinatla değil, Diyanet'in kendi şehir/ilçe kimlik numarasıyla** isteniyor. Ülke/il/ilçe listeleri `/api/Place/...` adreslerinden alınıyor.

**Bu ne anlama geliyor? [Çıkarım]** Bir uygulama Diyanet'in resmî verisini kullanıyorsa büyük olasılıkla (a) kullanıcının konumunu Diyanet'in ilçe listesindeki bir kimliğe eşliyor, (b) kendi sunucusu üzerinden veya uygulamaya gömülü bir hesapla bu servise bağlanıyor. "GPS ile her koordinat için hesap" ise telefonda formülle hesaplamayı gerektirir. Uygulama A hangisini yapıyor, **bilmiyorum.**

**Ek not:** Bir Home Assistant rehberinde, resmî siteden veri çekmenin engellendiği ve `ezanvakti.emushaf.net` adlı **resmî olmayan** bir topluluk servisinin aylık vakit verdiği yazıyor [AÖ]. Uygulama A'nın bu servisi kullandığına dair hiçbir kanıt yok.

**Resmî belge:** Diyanet'in bu servise ait resmî belgesini **bulamadım**; yukarıdaki bilgiler üçüncü taraf bir istemcinin kodundan alınmıştır.

### 5.4 Bunu kesin öğrenmenin yolu

APK elde edilirse: APK içindeki metinlerde `awqatsalah`, `diyanet`, `emushaf` gibi adresler ve astronomik hesap sınıfları (ör. "sun declination", "equation of time" gibi terimler veya `adhan` kütüphanesinin sınıf adları) aranır. Ayrıca uygulama internet kapalıyken yeni bir şehir için vakit gösterebiliyorsa, telefonda hesaplama yapıyor demektir (Bölüm 10, test 2).

---

## 6. Ezan, alarm ve bildirim sistemi

### 6.1 Uygulama A — izin listesinden çıkan ipuçları

Hiçbir mekanizmanın **kodda kullanıldığı doğrulanmadı.** Aşağıdakiler, izin listesindeki varlığa dayanan **olasılıklardır.**

| Mekanizma | İpucu | Durum |
|---|---|---|
| Kesin zamanlı alarm (AlarmManager) | `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` listede | İzin var: Kısmen doğrulandı. `setExactAndAllowWhileIdle` / `setAlarmClock` hangisi kullanılıyor: **Doğrulanamadı** |
| WorkManager | Exodus/AppBrain özetlerinde geçmedi | Bilinmiyor |
| Yeniden başlatmada alarm kurma | `RECEIVE_BOOT_COMPLETED` listede | İzin var; kullanım Doğrulanamadı |
| Ezan sesinin çalınması | `FOREGROUND_SERVICE_MEDIA_PLAYBACK` + `WAKE_LOCK` | Ezanın ön plan medya servisiyle çalındığını **düşündürür** [Çıkarım] |
| Ezanın süresi / kendiliğinden durması | — | Bilinmiyor |
| Arama gelince durma | `READ_PHONE_STATE` | Olası [Çıkarım]; Doğrulanamadı |
| Sessiz mod / Rahatsız Etmeyin | `ACCESS_NOTIFICATION_POLICY` + `MODIFY_AUDIO_SETTINGS`; "namazda sessize alma" beyanı | Olası [Çıkarım]; ezanın sessiz modda çalıp çalmadığı Doğrulanamadı |
| Pil optimizasyonu | `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Uygulamanın muafiyet istediğini **düşündürür** [Çıkarım] |
| Tam ekran ezan ekranı | `SYSTEM_ALERT_WINDOW` | Olası [Çıkarım] |

### 6.2 Android'in genel kuralları (uygulamanın bunları nasıl uyguladığı bilinmiyor)

| Kural | Kaynak | Güven |
|---|---|---|
| Android 14'ten itibaren, Android 13+ hedefleyen **yeni kurulan** uygulamalara `SCHEDULE_EXACT_ALARM` **varsayılan olarak verilmiyor**; zaten izni olan uygulama güncellemede iznini korur. | [Resmî Android] schedule-exact-alarms | Kısmen doğrulandı (arama özeti) |
| `setExact()`, `setExactAndAllowWhileIdle()` ve `setAlarmClock()` bu izin olmadan `SecurityException` verir; `OnAlarmListener` ile kurulan alarmlar izin gerektirmez. | Aynı sayfa | Kısmen doğrulandı |
| Alarm saati ve takvim uygulamaları `USE_EXACT_ALARM` kullanabilir; kurulumda otomatik verilir. `canScheduleExactAlarms()` ile kontrol önerilir. | Aynı sayfa | Kısmen doğrulandı |
| Android 13+ bildirim göstermek için `POST_NOTIFICATIONS` çalışma zamanı izni gerekir; yeni kurulumda bildirimler kapalı başlar. | [Resmî Android] notification-permission | Kısmen doğrulandı |
| "Zorla durdur" yapılınca **alarmlar ve planlı işler iptal olur.** | [Resmî Android] Android 13 FGS Task Manager sayfasındaki karşılaştırma tablosu | Kısmen doğrulandı |
| Zorla durdurulan uygulamanın, açılana kadar `BOOT_COMPLETED` dahil yayınları alamaması | Yalnızca üçüncü taraf kaynakta bulundu | Doğrulanamadı |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` izni API 34 (Android 14) ile eklendi. | [Resmî Android] Manifest.permission | Kısmen doğrulandı |
| Üretici kısıtlamaları: Huawei'nin "PowerGenie" servisi, Samsung'un 3 gün kullanılmayan uygulamaları durdurması, Xiaomi'nin sıkı kısıtlamaları | dontkillmyapp.com hakkındaki 2019–2023 haberleri | Kısmen doğrulandı (ikincil kaynak, güncel olmayabilir) |

### 6.3 Uygulama B

Alarm mekanizması hakkında bilgi **yok** (Bölüm 4).

---

## 7. APK mimarisi ve kullanılan kütüphaneler

**APK'ya erişemediğim için manifest, bileşen (Activity/Service/Receiver/Provider), veritabanı, SharedPreferences, API adresleri ve kod karartma (obfuscation) incelemesi yapılamadı.** Aşağıdakiler üçüncü taraf verileridir.

### 7.1 Exodus Privacy statik analiz raporları (Uygulama A, eski sürümler) [Exodus][AÖ]

| Sürüm (yıl) | Bulunan takip aracı izi | Kaynak |
|---|---|---|
| 8.1.99 (2021) | 10 adet; aralarında **InMobi** ve **Moat** | Exodus raporu (özet) |
| 8.2.7 (2021) | 7 adet: **Blesh**, **Coulus Coelib**, Facebook Ads, Google AdMob, Google Crashlytics, Google Firebase Analytics, Huawei Mobile Services (HMS) Core | Exodus raporu (özet) |
| 8.3.26 (2022) | 6 adet: Facebook Ads, Google AdMob, Google Crashlytics, Google Firebase Analytics, HMS Core, IAB Open Measurement | Exodus raporu (özet) |
| 9.5.9-ps (2026) | **Rapor bulunamadı** | — |

Rapor adresleri: `reports.exodus-privacy.eu.org/.../reports/162056`, `181195`, `248038` (hangi numaranın hangi sürüme ait olduğunu arama özetlerinden **eşleştiremedim**). Exodus'a göre izin sayısı sürüme göre **31–40** arasında.

**Takip araçları hakkında Exodus'un kendi tanımları [AÖ]:**
- **Blesh:** "Konum" kategorisinde; konum ziyaretlerine dayalı kitle verisi kullanan bir "konum zekâsı" platformu. Türkiye'de beacon (Bluetooth işaretçi) ağı işletiyor.
- **Coulus Coelib:** "Reklam" kategorisinde; "Panama merkezli olduğu iddia edilen bir şirkete" ait. Exodus bu konuda AppCensus'un bir blog yazısına bağlantı veriyor (yazıyı okuyamadım).

### 7.2 Diğer üçüncü taraf bilgileri

| Bilgi | Kaynak | Güven |
|---|---|---|
| 65 kütüphane; listede Firebase görünüyor | AppBrain [AÖ] | Kısmen doğrulandı; diğer kütüphane adları özetlerde görünmedi |
| Huawei HMS izi → uygulama Google servisleri olmayan Huawei telefonlarını da destekliyor olabilir | [Çıkarım] | Doğrulanamadı |
| `ACCESS_ADSERVICES_*` izinleri → Android Privacy Sandbox reklam altyapısı | AppBrain [AÖ] | Kısmen |
| Amazon izni (`com.amazon.privacypass.ATTEST`) → Amazon kaynaklı bir SDK olabilir | [Çıkarım] | Doğrulanamadı |
| Boyut: 48 MB indirme (sizin telefonunuz için) | [Ekran] | Doğrulandı (görüntüde) |
| Kod karartma, şifreleme, API uç noktaları | — | Bilinmiyor |

---

## 8. Ağ bağlantıları, gizlilik ve güvenlik

**Ağ trafiği analizi yapılmadı.** Aşağıdakiler beyanlar ve statik analiz izleridir.

| Konu | Uygulama A | Güven |
|---|---|---|
| Konum dış sunuculara gidiyor mu? | **Bilmiyorum.** Ancak eski sürümlerde konum odaklı (Blesh) ve reklam odaklı (Coulus Coelib) SDK izleri vardı; uygulama arka plan konum iznini istiyor; App Store etiketi (ilk rapor) kaba konumun üçüncü taraf reklam/pazarlama için kullanılabileceğini söylüyor. | [Exodus][AÖ] — risk göstergesi, **kanıt değil** |
| Google Play veri güvenliği | Arama özetleri çelişkili: "konum üçüncü taraflarla paylaşılabilir" ve "veri toplanmaz" ifadeleri birlikte geçiyor | [AÖ] Çelişkili |
| Gizlilik politikası | ezanvaktipro.com/en/privacy-policy (ilk rapor: "reklam destekli", konum vakit hesabı ve kişiselleştirme için işleniyor). Deen Studios'a ait ayrı bir politika **bulamadım**. | [AÖ] Kısmen |
| Hesap zorunluluğu | Bilinmiyor | — |
| Hassas izinler | Arka plan konum, kullanım istatistikleri (`PACKAGE_USAGE_STATS`), telefon durumu, üstte gösterme, reklam kimliği; 2022'de tüm uygulamaları listeleme | [3. taraf][Exodus] Kısmen |
| Reklam içeriği | Şikayetvar'da "Ezan Vakti Pro'da müstehcen, faiz ve bahis reklamları" şikâyeti var; aynı kişi bildirimlerin geldiğini söylüyor. **Şikâyetin bu uygulamaya ait olduğu doğrulanamadı** (aynı adlı başka uygulamalar var). | [AÖ] Doğrulanamadı |
| SDK güvenlik açıkları | SDK sürümlerini bilmediğim için **araştırılamadı** | — |

**Kullanıcı için pratik not [Çıkarım; genel Android bilgisi]:** Konum iznini "Her zaman" yerine "Yalnızca uygulamayı kullanırken" vermek, arka planda konum toplanmasını engeller. Bunun bedeli: seyahatte vakitlerin otomatik güncellenmemesi ve "camide sessize alma" özelliğinin çalışmaması olabilir.

---

## 9. Karşılaştırma tablosu

| Ölçüt | Uygulama A (Ezan Vakti Pro) | Uygulama B (Namaz Vakitleri) |
|---|---|---|
| Vakit doğruluğu / hesaplama seçenekleri | **Kısmen doğrulandı** — 15+ yöntem beyanı; tanıtım görseli Diyanet tipi hesapla ±1 dk uyumlu | Kısmen doğrulandı (yöntem var beyanı); doğruluk Test edilmedi |
| Diyanet uyumu | **Kısmen doğrulandı** (görsel uyumu); veri kaynağı Doğrulanamadı | Kısmen doğrulandı (mağaza metni); kaynak Bulunamadı |
| Alarm güvenilirliği | Test edilmedi (gerekli izinler listede var) | Test edilmedi |
| Bildirim seçenekleri | Kısmen doğrulandı (ezan sesleri, sahur alarmı) | Kısmen doğrulandı (önce/vaktinde/sonra) |
| Kıble doğruluğu | Test edilmedi (pusula + harita beyanı) | Test edilmedi |
| Kur'an özellikleri | Kısmen doğrulandı — kapsamlı (19 hafız, meal 20+ dil, tecvid, oku-takip, e-kitap kütüphanesi) | Kısmen doğrulandı — sınırlı ("Kur'an ve dua oynatıcısı") |
| Çevrimdışı çalışma | Kısmen doğrulandı (vakitler için beyan) | Doğrulanamadı |
| Arayüz / kullanım kolaylığı | Test edilmedi (birden çok tema görselde var) | Test edilmedi |
| Özelleştirme | Kısmen doğrulandı (temalar, duvar kâğıdı) | Kısmen (widget, ezan sesleri) |
| Reklam yoğunluğu | Kısmen — reklam var (ekranda doğrulandı); yoğunluk ölçülmedi | Kısmen — reklam var; yoğunluk bilinmiyor |
| Ücretli özellikler | Kısmen — uygulama içi satın alma + ayrı "Pro Plus"; fiyat Bulunamadı | Kısmen — Pro üyelik; fiyat Bulunamadı |
| Pil / bellek | Test edilmedi | Test edilmedi |
| Gizlilik ve izinler | Kısmen — 43 izin, arka plan konum, kullanım istatistikleri; eski sürümlerde konum/reklam SDK'ları | Kısmen — 23 izin; beyanlar çelişkili |
| Android uyumluluğu | Doğrulanamadı — Android 9+ / 11+ (kaynaklar çelişkili) | Kısmen — Android 7.0+ (çelişkili) |
| Teknik mimari / sürdürülebilirlik | Doğrulanamadı — aktif güncelleniyor (21 Ekim 2026) | Doğrulanamadı — son bilinen güncelleme Şubat 2026 |

Yeterli kanıt olmadığı için **sayısal puan verilmedi.**

---

## 10. Gerçekleştirilen testler ve sonuçları

### 10.1 Yapılan testler

| # | Test | Yapılan işlem | Sonuç | Güvenilirlik |
|---|---|---|---|---|
| T1 | Tanıtım görselindeki vakitlerin hangi yönteme uyduğu | `tools/ekran_vakit_karsilastir.js` ile adhan 4.4.3 karşılaştırması | Türkiye yöntemi, 20 Mart 2023: en büyük fark 1 dk; diğer yöntemler 6–17 dk | Orta — şehir ve tarih varsayım, görsel reklam malzemesi |
| T2 | Bu ortamdan analiz kaynaklarına erişim | `curl` ile 21 adres, WebFetch | Mağazalar, APK siteleri, Exodus, Diyanet engelli | Yüksek |

### 10.2 Yapılamayan çalışma zamanı testleri

**Hiçbir çalışma zamanı testi yapılmadı.** Emülatör, adb, fiziksel cihaz ve APK yok.

| # | Senaryo | Beklenen davranış | Nasıl test edilir | Sonuç |
|---|---|---|---|---|
| 1 | Konum izni ver / verme | İzinsiz: şehir elle seçilebilmeli | İlk açılışta izni reddet | Test edilmedi |
| 2 | İnternet açık / kapalı | Kapalıyken de vakitler görünmeli | Uçak modunda **yeni bir şehir** seç: vakit geliyorsa telefonda hesaplanıyordur | Test edilmedi |
| 3 | Şehir değiştirme | Vakitler ve alarmlar yenilenmeli | Şehri değiştir, `adb shell dumpsys alarm` çıktısına bak | Test edilmedi |
| 4 | Sonraki vakit bildirimi | Alarm listede görünmeli | `adb shell dumpsys alarm \| grep mobilexsoft` | Test edilmedi |
| 5 | Yeniden başlatma | Alarmlar yeniden kurulmalı | Telefonu kapat-aç, 4. adımı tekrarla | Test edilmedi |
| 6 | Arka plana alma | Bildirim zamanında gelmeli | Uygulamayı kapat, vakti bekle | Test edilmedi |
| 7 | Pil tasarrufu / Doze | Kesin alarm Doze'da da çalmalı | `adb shell dumpsys deviceidle force-idle` | Test edilmedi |
| 8 | Saat dilimi / tarih değişimi | Vakitler yeniden hesaplanmalı | Ayarlardan saat dilimini değiştir | Test edilmedi |
| 9 | Kıble pusulası | İstanbul il merkezi için **151,6°** (kuzeyden saat yönünde; bu oturumda adhan 4.4.3 `Qibla()` ile hesaplandı) | Uygulamanın gösterdiği açıyla karşılaştır | Test edilmedi |
| 10 | Depolama / bellek / pil | — | `dumpsys meminfo`, `batterystats` | Test edilmedi |
| 11 | Reklam ve ağ bağlantıları | — | Sertifika sabitlemesini **aşmadan**, yalnızca DNS kayıtlarına bakarak | Test edilmedi |

---

## 11. Bilinen sorunlar ve teknik sınırlamalar

1. **Ağ kuralı:** Bu bulut ortamı şu adresleri engelliyor: `play.google.com`, `www.appbrain.com`, `reports.exodus-privacy.eu.org`, `www.apkmirror.com`, `apkpure.com`, `apkcombo.com`, `f-droid.org`, `ezanvaktipro.com`, `deen-studios.com`, `awqatsalah.diyanet.gov.tr`, `namazvakitleri.diyanet.gov.tr`, `api.aladhan.com`, `dl.google.com`. WebFetch aracı DNS hatası veriyor.
2. **Arama özetleri sınırlı:** Kaynak sayfaları kendim okuyamadım; özetler sayfayı yanlış veya eksik aktarabilir. Örneğin bir özet, iki kesin alarm iznini (`SCHEDULE_EXACT_ALARM` ve `USE_EXACT_ALARM`) birlikte bildirmenin Google önerisine aykırı olduğunu ileri sürdü; bunu kaynakta doğrulayamadığım için rapora almadım.
3. **İsim karışıklığı:** "Ezan Vakti Pro" adında başka geliştiricilere ait uygulamalar da var (ör. App Store'da IslamiQ'ya ait "Ezan Vakti Namaz Vakitleri Pro"; AndroidOut'ta "Namaz Vakti Pro: Ezan, Kuran"). Bu raporda yalnızca `com.mobilexsoft.ezanvakti` paketine ait olduğu anlaşılan bilgiler kullanıldı.
4. **Eski veriler:** Exodus raporları 2021–2022'ye ait. Güncel sürümde takip araçları değişmiş olabilir.
5. **Ekran görüntüsü çözünürlüğü:** Tanıtım görsellerindeki küçük yazılar bulanık; bazı kelimeler ve rakamlar tahmini okundu ve tabloda belirtildi.
6. **APK yasal edinimi:** Üçüncü taraf APK siteleri değiştirilmiş (modlu) dosya sunabilir; arama sonuçlarında bu uygulamanın "premium kilidi açılmış" modlu sürümleri de görüldü. Bu tür dosyalar **kullanılmamalı.**

---

## 12. Doğrulanamayan bilgiler

- Vakitlerin **sunucudan mı, cihazda mı** üretildiği; kullanılan algoritma/kütüphane.
- Diyanet verisinin **hangi servis üzerinden** alındığı (varsa).
- **Hanefi ikindi** seçeneği ve **manuel dakika düzeltmesi**.
- **Yüksek enlem** yöntemi; saat dilimi ve yaz saati yönetimi.
- AlarmManager / WorkManager / ön plan servisinin **gerçekten** nasıl kullanıldığı; ezanın ne kadar süre çaldığı; sessiz mod davranışı.
- Güncel sürümdeki **reklam ve analiz SDK'larının adları** ve sürümleri.
- **Hedef SDK** ve kesin **minimum Android** sürümü.
- Ağ uç noktaları; konumun sunucuya gönderilip gönderilmediği.
- `PACKAGE_USAGE_STATS` ve Amazon izninin **amacı**.
- Android'de **widget** ve kilit ekranı özelliği.
- Uygulama içi satın alma **fiyatları**.
- Deen Studios, MaviAy ve "Mobilexsoft" arasındaki **ilişki**.
- Şikayetvar'daki reklam şikâyetinin bu uygulamaya ait olup olmadığı.
- Uygulama B ile ilgili, ilk raporda doğrulanamayan maddelerin tamamı.

---

## 13. Sonuç ve teknik değerlendirme

**Kısmen doğrulanmış ölçüde söyleyebileceklerim:**
- Uygulama A; aktif güncellenen (21 Ekim 2026), reklam destekli, uygulama içi satın almalı, çok özellikli bir "İslami yaşam" uygulamasıdır. Kur'an (19 hafız, tecvid, meal), e-kitap kütüphanesi, hava durumu, cami bulucu, Wear OS gibi namaz vakti dışı özellikleri belirgin biçimde fazladır.
- Türkiye için gösterdiği vakitler, en azından tanıtım görselinde, **Diyanet tipi hesapla uyumludur.**
- İzin listesi, ezanın kesin saatte ve kesintisiz çalınması için gereken Android yapı taşlarının (kesin alarm, açılışta yeniden kurma, ön plan medya servisi, pil muafiyeti) **bulunduğunu** gösteriyor. Ancak bunların doğru kullanıldığını bilmiyorum.
- Gizlilik açısından **dikkat gerektiren** bir profili vardır: arka plan konum, kullanım istatistikleri izni, reklam kimliği ve geçmiş sürümlerde konum odaklı reklam/analiz SDK'ları.

**Söyleyemeyeceklerim:** Vakit üretim yöntemi, alarm sisteminin iç yapısı, veri akışı ve güncel takip araçları hakkında **kesin bir şey bilmiyorum.**

### Analizi tamamlamak için gerekenler (kolaydan zora)

**Seçenek 1 — Ağ izni (en kolay, telefon gerekmez):**
Bu bulut ortamının ağ ayarına şu adresleri eklerseniz, mağaza sayfasını, Exodus raporlarını ve AppBrain listesini doğrudan okuyabilirim:
`play.google.com`, `reports.exodus-privacy.eu.org`, `www.appbrain.com`, `ezanvaktipro.com`.
Nasıl: Oturum başlığındaki bulut ortamı menüsü → **Edit** → **Network access** → **Allowed domains** kısmına bu adresleri ekleyin ("Allow package managers" kutusu işaretli kalsın). Ayrıntı: https://code.claude.com/docs/en/cloud-environments#network-access

**Seçenek 2 — Exodus'tan güncel analiz istemek (telefondan yapılabilir):**
1. Tarayıcıda `https://reports.exodus-privacy.eu.org/en/analysis/submit/` adresini açın.
2. "URL of the application" kutusuna şunu yapıştırın: `https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvakti`
3. Gönderin; analiz bitince çıkan sayfanın ekran görüntülerini (takip araçları ve izinler bölümü) bana gönderin.
(Bu form adresi arama sonucundan alındı; sayfayı kendim açamadığım için düğme adları farklı olabilir.)

**Seçenek 3 — APK'nın kendisi (tam teknik analiz için gerekli):**
- En güvenilir yol: Uygulamayı **kendi telefonunuza Google Play'den** kurmak ve Google'ın resmî **Android SDK Platform Tools** (`adb`) aracıyla çekmek:
  `adb shell pm path com.mobilexsoft.ezanvakti` → çıkan her `.apk` yolu için `adb pull <yol>`.
  Bu işlem telefonda "USB hata ayıklama" açmayı gerektirir; isterseniz adım adım ayrıca anlatırım.
- Üçüncü taraf APK sitelerinden indirilen dosyalar değiştirilmiş olabilir; kullanılacaksa imza sertifikası Google Play'den kurulan sürümle karşılaştırılmalıdır.
- Uyarı: Depo **herkese açıksa** APK'yı depoya yüklemeyin; başkasının uygulamasını yeniden dağıtmak olur.

**Seçenek 4 — Çalışma zamanı testleri:** Bir Android telefon veya emülatör gerekir (bu bulut ortamında yok).

---

## 14. Kaynakça ve kanıtlar

Tüm erişimler: **25 Ekim 2026.** Aksi belirtilmedikçe kanıt türü **[AÖ] arama özeti** — sayfalar doğrudan açılamadı.

**Kullanıcı tarafından sağlanan**
- Google Play ekran görüntüsü — "Ezan Vakti Pro: Namaz, Kuran" (çekim tarihi bilinmiyor) [Ekran]

**Uygulama A — mağaza ve üçüncü taraf kayıtları**
- Google Play: https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvakti
- Google Play (en_IN): https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvakti&hl=en_IN
- AppBrain: https://www.appbrain.com/app/azan-time-pro-quran-qiblah/com.mobilexsoft.ezanvakti
- AppBrain: https://www.appbrain.com/app/adhan-time-holy-quran-pro/com.mobilexsoft.ezanvakti
- AppBrain: https://www.appbrain.com/app/ezan-vakti-namaz-saati-pro/com.mobilexsoft.ezanvakti
- APKPure: https://apkpure.com/azan-time-pro-quran-qiblah/com.mobilexsoft.ezanvakti
- Aptoide: https://ezan-vakti-pro.en.aptoide.com/app
- APK4Fun (9.2.8_ps): https://www.apk4fun.com/apk/143482/
- Tamindir: https://www.tamindir.com/android/ezan-vakti-pro-apk/
- BlueStacks: https://www.bluestacks.com/tr/apps/lifestyle/azan-time-pro-quran-qiblah-on-pc.html
- App Store — Azan Time Pro: Holy Quran: https://apps.apple.com/app/id437447439
- App Store — Azan Time Pro Plus: Holy Quran: https://apps.apple.com/app/id442401423 ve https://apps.apple.com/us/app/-/id442401423
- App Store — Ezan Vakti / Adhan Time (MaviAy): https://apps.apple.com/us/app/-/id587363285
- Gizlilik politikası (ilk rapordan): https://ezanvaktipro.com/en/privacy-policy/
- YouTube kanalı: https://www.youtube.com/ezanvaktipro
- Şikayetvar (uygulama eşleşmesi doğrulanamadı): https://www.sikayetvar.com/ezan-vakti-pro/ezan-vakti-proda-mustehcen-ve-din-disi-reklamlar-goruntuleniyor

**Exodus Privacy [Exodus]**
- https://reports.exodus-privacy.eu.org/de/reports/162056
- https://reports.exodus-privacy.eu.org/en/reports/181195
- https://reports.exodus-privacy.eu.org/en/reports/248038
- Azan Time Lite: https://reports.exodus-privacy.eu.org/en/reports/181197
- Blesh takip aracı: https://reports.exodus-privacy.eu.org/en/trackers/388
- Coulus Coelib takip aracı: https://reports.exodus-privacy.eu.org/en/trackers/443
- Yeni analiz formu: https://reports.exodus-privacy.eu.org/en/analysis/submit/

**Diyanet ve vakit hesabı**
- pydiyanet 0.1.2 (PyPI, MIT) — **indirildi ve okundu** [Kod]: https://pypi.org/project/pydiyanet/ — dosya `pydiyanet-0.1.2.tar.gz`, SHA-256 `0c173735c086fabdce66b57243ef2867e5e0fbd0f31a2bb6c1f47a9e5af09e3b`
- adhan 4.4.3 (npm, MIT) — **indirildi ve çalıştırıldı** [Kod]: https://www.npmjs.com/package/adhan , https://github.com/batoulapps/adhan-js
- Home Assistant Diyanet rehberi (ezanvakti.emushaf.net notu): https://dashing-kayak-192.notion.site/Diyanet-Ezan-in-Home-Assistant-7186fe119843435d802ca3c0b71f1c5f
- Diyanet ve AlAdhan vakit farkı tartışması: https://community.islamic.network/d/151-diffrent-prayer-times-in-diyanet-method

**Android platformu [Resmî Android]**
- Kesin alarmlar (Android 14): https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
- Alarm planlama: https://developer.android.com/develop/background-work/services/alarms
- Bildirim izni (Android 13): https://developer.android.com/develop/ui/views/notifications/notification-permission
- Ön plan servis görev yöneticisi (zorla durdurma tablosu): https://developer.android.google.cn/about/versions/13/changes/fgs-manager
- İzin listesi: https://developer.android.com/reference/android/Manifest.permission
- Üretici kısıtlamaları (ikincil): https://gigazine.net/gsc_news/en/20231210-dont-kill-my-app , https://www.androidpolice.com/2019/01/13/dontkillmyapp-com-shames-oems-that-needlessly-kill-useful-background-processes-to-save-battery-life/

**Uygulama B** — kaynaklar için ilk rapora bakınız: `git show 071afdc:RAPOR.md` (Bölüm 14).
