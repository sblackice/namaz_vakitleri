# İki Namaz Vakti Uygulamasının Teknik ve Fonksiyonel Analizi — İlk Rapor

- **İnceleme tarihi:** 25 Ekim 2026
- **Uygulama A:** Ezan Vakti Pro: Namaz, Kuran — Deen Studios LLC-FZ
- **Uygulama B:** Namaz Vakitleri: Alarm & Kıble — NvApps Ltd
- **Raporun türü:** İlk (ön) rapor. APK analizi ve çalışma zamanı testi **yapılamadı**; nedenleri Bölüm 2 ve 11'de.

---

## Kanıt türleri ve güven düzeyleri (bu raporda kullanılan etiketler)

| Etiket | Anlamı |
|---|---|
| **[AÖ]** Arama özeti | Bir web arama aracının döndürdüğü sayfa başlığı + otomatik özet. **Sayfanın kendisini açamadım** (aşağıya bakın). Bu yüzden en fazla "Kısmen doğrulandı" sayılır. |
| **[Mağaza]** | Google Play / App Store kaydı. Geliştiricinin kendi beyanıdır; teknik doğrulama değildir. |
| **[3. taraf]** | AppBrain, APKPure, Aptoide gibi siteler. Resmî değil; eski sürüme ait olabilir. |
| **[Resmî Android]** | developer.android.com belgeleri. Platformun genel kuralı; uygulamanın bunu kullandığını göstermez. |
| **[Çıkarım]** | Eldeki verilerden yaptığım, doğrulanmamış değerlendirme. |

Güven düzeyleri: **Doğrulandı / Kısmen doğrulandı / Doğrulanamadı / Test edilmedi / Bilinmiyor.**

> **Önemli not:** Bu rapordaki hiçbir bulgu "Doğrulandı" seviyesinde değildir. Çünkü hiçbir web sayfasını doğrudan açıp okuyamadım; yalnızca arama motoru özetlerini görebildim. Arama özetleri bazen sayfayı yanlış veya eksik aktarabilir.

---

## 1. Yönetici özeti

1. **Dosya yok:** Bu çalışma ortamındaki proje klasörü (`namaz_vakitleri`) tamamen boştu. APK, AAB, kaynak kod, AndroidManifest.xml veya ekran görüntüsü **bulunmadı**. Mesajda bahsedilen ekran görüntüleri de bana ulaşmadı.
2. **Ağ kısıtlı:** Bu bulut ortamından Google Play, App Store, APK siteleri, Diyanet ve diğer sunuculara erişim **engelli** (proxy 403 hatası). Sayfa okuma aracı (WebFetch) de DNS hatası verdi. Yalnızca web arama aracı çalıştı.
3. **Bu yüzden:** APK analizi, ağ trafiği analizi ve testlerin **hiçbiri yapılmadı**. Rapordaki bilgiler yalnızca arama sonuçlarındaki mağaza kayıtları ve üçüncü taraf sitelerden gelmektedir.
4. **Kimlik (Kısmen doğrulandı):**
   - A → paket adı **`com.mobilexsoft.ezanvakti`** (İngilizce adı "Azan Time Pro"). [AÖ][Mağaza]
   - B → paket adı **`com.simsekburak.android.namazvakitleri`** (İngilizce adı "Namaz: Prayer Times & Qibla"). [AÖ][Mağaza][3. taraf]
5. **Öne çıkan farklar (beyan düzeyinde):**
   - A çok daha büyük ve kapsamlı bir uygulama: ~42–43 izin, 65 kütüphane, kapsamlı Kur'an modülü, 15+ hesaplama yöntemi. [3. taraf]
   - B daha küçük: ~23 izin, 53 kütüphane; Kur'an ve dua "oynatıcısı" var. [3. taraf]
   - İkisi de **reklam** içeriyor ve **uygulama içi satın alma** sunuyor. [AÖ]
   - A'nın App Store gizlilik etiketinde **kaba konum ve kimlik bilgilerinin reklam/takip için** kullanılabileceği beyan ediliyor. [AÖ][Mağaza]
6. **En önemli açık soru:** "Vakitler sunucudan mı geliyor, cihazda mı hesaplanıyor?" ve "Diyanet verisi nereden alınıyor?" soruları **APK veya trafik incelemesi olmadan cevaplanamaz.** Bu soruların cevabını bilmiyorum.

---

## 2. İncelenen dosyalar ve kaynaklar

### 2.1 Gerçekten yaptığım işlemler

| # | İşlem | Sonuç |
|---|---|---|
| 1 | Proje klasöründeki tüm dosyaları listeledim (`find`) | Klasör **boş**; git deposunda hiç commit yok. |
| 2 | Ev dizinlerini kontrol ettim (`/home/user`, `/root`) | Analiz dosyası **yok**. |
| 3 | Araçları kontrol ettim | `java`, `python3`, `unzip` **var**. `jadx`, `apktool`, `aapt/aapt2`, `apkanalyzer`, `adb` **yok**. Emülatör **yok**. |
| 4 | Google Play'e `curl` ile bağlandım | **403 – engellendi.** |
| 5 | apkpure.com, apkmirror.com, awqatsalah.diyanet.gov.tr, ezanvakti.emushaf.net, api.aladhan.com | Hepsi **403 – engellendi.** |
| 6 | WebFetch ile play.google.com, apps.apple.com, bluestacks.com | **DNS hatası** – açılamadı. |
| 7 | WebSearch ile 17 arama yaptım | Çalıştı. Rapordaki bilgilerin tamamı bu aramalardan. |

### 2.2 Erişemediğim / bulunmayan dosyalar

- Uygulama A ve B'nin **APK** dosyaları — yok.
- **AAB** dosyaları — yok (zaten geliştirici dışında elde edilemez).
- **Kaynak kod / proje klasörü** — yok (iki uygulama da açık kaynak değil gibi görünüyor; bunu da doğrulayamadım).
- **AndroidManifest.xml** — yok.
- **Ekran görüntüleri** — mesajda bahsedildi ama bana ulaşmadı.
- **Not:** Görevde "bu üç uygulama" ifadesi geçiyor ama yalnızca iki uygulama tanımlanmış. Üçüncü bir uygulama varsa adını bilmiyorum.

---

## 3. Uygulama A: Ezan Vakti Pro: Namaz, Kuran (Deen Studios LLC-FZ)

### 3.1 Kimlik

| Bilgi | Değer | Kaynak | Güven |
|---|---|---|---|
| Paket adı | `com.mobilexsoft.ezanvakti` | Google Play URL'si (arama sonucu) | Kısmen doğrulandı |
| Geliştirici | Deen Studios LLC-FZ (BAE), destek: support@deen-studios.com | [AÖ] Google Play geliştirici bilgisi | Kısmen doğrulandı |
| Eski geliştirici izi | Paket adındaki "mobilexsoft" eski bir geliştirici/marka adını işaret ediyor olabilir | [Çıkarım] | Doğrulanamadı |
| Güncel sürüm | 9.5.5-ps, 10 Eylül 2026 (bir kaynak); başka kaynakta 9.4.10-ps | [3. taraf] | Kısmen doğrulandı (kaynaklar çelişiyor) |
| Minimum Android | 9.5.2-ps için Android 9+ (eski sürümlerde 5.0+ ve 4.1) | [3. taraf] | Kısmen doğrulandı |
| Hedef SDK | — | — | Bilinmiyor |
| İndirme | 10M+ (Play), AppBrain'e göre toplam 40M | [AÖ] | Kısmen doğrulandı (kaynaklar farklı) |
| İzin sayısı | 42 veya 43 | [3. taraf] AppBrain | Kısmen doğrulandı |
| Kütüphane sayısı | 65 | [3. taraf] AppBrain | Kısmen doğrulandı |
| Aile uygulamaları | Ezan Vakti Pro Plus (`com.mobilexsoft.ezanvaktiproplus`, ücretli, reklamsız), Azan Time Lite (`com.mobilexsoft.ezanvaktilite`) | [AÖ][3. taraf] | Kısmen doğrulandı |

### 3.2 Özellik tablosu

"Nasıl ulaşılır" ve "nasıl çalışır" bilgileri için uygulamayı çalıştıramadığımdan çoğu hücre **Bilinmiyor**dur.

| # | Özellik | Var mı? | Nasıl ulaşılır / çalışır | İzin / donanım | İnternet | Kaynak / güven |
|---|---|---|---|---|---|---|
| 1-2 | Vakit tablosu, 6 vakit | Var (beyan) | Bilinmiyor | — | Aylık indirme sonrası çevrimdışı (beyan) | [AÖ][Mağaza] Kısmen |
| 3 | Ezan sesi / bildirim | Var (beyan) | Bilinmiyor | Bildirim izni (listede var) | Bilinmiyor | [AÖ] Kısmen |
| 4 | Vakit öncesi hatırlatma | Var (iOS benzer sürümde 45/30/15/5 dk) | Bilinmiyor | — | — | [AÖ] Kısmen — Android için doğrulanamadı |
| 5 | Alarm ayarlama | "Salah alarms" beyanı; izin listesinde kesin zamanlı alarm izni var | Bilinmiyor | Kesin alarm izni | — | [AÖ][3. taraf] Kısmen |
| 6 | Otomatik konum | Var (GPS) | Bilinmiyor | Yaklaşık + hassas + **arka plan** konum | GPS için gerekmez; ters coğrafi kodlama için gerekebilir [Çıkarım] | [AÖ] Kısmen |
| 7 | Şehir/ilçe seçimi | Muhtemel; "tüm il ve ilçeler" ifadesi başka bir uygulamaya ait olabilir | — | — | — | Doğrulanamadı |
| 8 | Hesaplama yöntemi | ISNA, MWL, Umm al-Qura ve 15+ yöntem; "GPS Diyanet" yöntemi (sürüm notu) | Bilinmiyor | — | — | [AÖ][3. taraf] Kısmen. Hanefi ikindi: **Doğrulanamadı** |
| 9-10 | Kıble pusulası, sensörler | Kıble pusulası var (beyan) | Bilinmiyor | Manyetometre/ivmeölçer [Çıkarım] | — | [AÖ] Kısmen |
| 11 | Hicri takvim / dini günler | İmsakiye, Ramazan özellikleri (beyan) | — | — | — | [AÖ] Kısmen |
| 12 | Kur'an, meal | Tam Kur'an, 20+ dilde meal, tecvid renklendirme | — | — | — | [AÖ][Mağaza] Kısmen |
| 13 | Sesli Kur'an | Hatim/okuma sesleri, otomatik kaydırma; iOS'ta 24 saat Kur'an radyosu | — | — | Muhtemelen gerekir [Çıkarım] | [AÖ] Kısmen; çevrimdışı ses: Doğrulanamadı |
| 14 | Mushaf seçenekleri | iOS benzer sürümde sayfa sayfa "Mushaf modu" | — | — | — | Android için Doğrulanamadı |
| 15 | Widget | iOS'ta var; Android için | — | — | — | Android: Doğrulanamadı |
| 16 | Kilit ekranı / sürekli gösterim | "Diğer uygulamaların üzerinde gösterme" izni listede var | — | SYSTEM_ALERT_WINDOW | — | İşlevi Doğrulanamadı |
| 17 | Tema / kişiselleştirme | — | — | — | — | Bilinmiyor |
| 18 | Reklam / satın alma | Reklam var (tam ekran reklam şikâyeti mevcut); Google Play faturalandırma var | — | Reklam kimliği izni | Gerekir | [AÖ][3. taraf] Kısmen |
| 19 | Hesap / bulut senk. | "Hatim topluluğu" özelliği hesap gerektirebilir | — | — | — | [Çıkarım] Doğrulanamadı |
| 20 | Çevrimdışı | Vakitler için beyan ediliyor (aylık indirme) | — | — | — | [AÖ] Kısmen |
| Ek | Kaza takibi, Wear OS, camide otomatik sessize alma (Lite'ta) | Beyan | — | — | — | [AÖ] Kısmen |

### 3.3 İzinler (üçüncü taraf listesine göre, resmî değil)

Yaklaşık ve hassas konum, **arka planda konum**, telefon durumu, bildirim, **kesin zamanlı alarm**, açılışta çalışma (RECEIVE_BOOT_COMPLETED), **diğer uygulamaların üzerinde gösterme**, reklam kimliği, Google Play faturalandırma. [AÖ — AppBrain] — **Kısmen doğrulandı.**

---

## 4. Uygulama B: Namaz Vakitleri: Alarm & Kıble (NvApps Ltd)

### 4.1 Kimlik

| Bilgi | Değer | Kaynak | Güven |
|---|---|---|---|
| Paket adı | `com.simsekburak.android.namazvakitleri` | Google Play URL'si, AppBrain | Kısmen doğrulandı |
| Geliştirici | NvApps Ltd, Londra; bazı yansı sitelerde "Burak Şimşek" | [AÖ][3. taraf] | Kısmen doğrulandı |
| iOS karşılığı | App Store ID 1190002151 | [AÖ] | Kısmen doğrulandı |
| Güncel sürüm | 6.22.915, son güncelleme 20 Şubat 2026 (AppBrain) | [3. taraf] | Kısmen doğrulandı; daha yeni sürüm olabilir |
| Minimum Android | Android 7.0+ (yeni sürümler); AppBrain 5.0+ diyor | [3. taraf] | Kısmen doğrulandı (çelişkili) |
| Hedef SDK | — | — | Bilinmiyor |
| Puan / indirme | 4,5 yıldız, 131 bin yorum, 5M+ (bir anlık görüntü) | [AÖ] | Kısmen; güncel değil olabilir |
| İzin sayısı | 23 (sürüm 6.18.900) | [3. taraf] Aptoide | Kısmen doğrulandı |
| Kütüphane sayısı | 53 | [3. taraf] AppBrain | Kısmen doğrulandı |

### 4.2 Özellik tablosu

| # | Özellik | Var mı? | Nasıl ulaşılır / çalışır | İzin / donanım | İnternet | Kaynak / güven |
|---|---|---|---|---|---|---|
| 1-2 | Vakit tablosu | Var; aylık vakit tablosu, 8000+ şehir (iOS) | Bilinmiyor | — | Bilinmiyor | [AÖ] Kısmen |
| 3 | Ezan sesi / bildirim | Var; farklı müezzin sesleri | Bilinmiyor | Bildirim izni | — | [AÖ] Kısmen |
| 4 | Vakit öncesi/sonrası hatırlatma | Var (önce, vaktinde, sonra) | Bilinmiyor | — | — | [AÖ] iOS için Kısmen; Android: Doğrulanamadı |
| 5 | Alarm | "Vakit başına alarm" (iOS) | — | — | — | Android: Doğrulanamadı |
| 6 | Otomatik konum | Gizlilik politikası hassas/yaklaşık konum toplandığını söylüyor | — | Konum | — | [AÖ] Kısmen |
| 7 | Şehir/ilçe seçimi | — | — | — | — | Bilinmiyor |
| 8 | Hesaplama yöntemi | "Dünya çapında hesaplama yöntemleri" | — | — | — | [AÖ] Kısmen. Hanefi ikindi: **Doğrulanamadı** |
| 9-10 | Kıble pusulası | Var | — | Sensörler [Çıkarım] | — | [AÖ] Kısmen |
| 11 | Hicri takvim / dini günler | Dini günler takvimi, Ramazan imsakiyesi | — | — | — | [AÖ] Kısmen |
| 12-14 | Kur'an | "Kur'an ve Dua oynatıcısı" | — | — | Bilinmiyor | [AÖ] Kısmen; meal ve mushaf: **Doğrulanamadı** |
| 15 | Widget | Var; vakit listesi + geri sayım; Pro'ya özel widget'lar | — | — | — | [AÖ] Kısmen |
| 16 | Kilit ekranı | iOS'ta Live Activity geri sayımı; Android karşılığı | — | — | — | Android: Doğrulanamadı |
| 17 | Tema | — | — | — | — | Bilinmiyor |
| 18 | Reklam / satın alma | Reklam var; Pro üyelik reklamları kaldırır | — | — | — | [AÖ] Kısmen; Pro fiyatı: **Bulamadım** |
| 19 | Hesap | iOS açıklaması: üyelik/kişisel bilgi gerekmez | — | — | — | [AÖ] Kısmen |
| 20 | Çevrimdışı | — | — | — | — | Bilinmiyor |
| Ek | Diyanet uyumu | Mağaza metninde "Diyanet ezan vakti" ifadesi geçiyor | — | — | — | [AÖ][3. taraf] Kısmen; **veri kaynağı Bulunamadı** |

---

## 5. Namaz vakti hesaplama sistemi

| Soru | Uygulama A | Uygulama B |
|---|---|---|
| Sunucudan mı, cihazda mı? | **Bilmiyorum.** "GPS ile otomatik hesaplama" + "aylık indirme ile çevrimdışı" ifadeleri, **ikisinin birlikte kullanıldığını** düşündürüyor (ör. Diyanet modu için sunucu, diğer yöntemler için cihazda hesap). [Çıkarım] | **Bilmiyorum.** |
| Kullanılan kütüphane/algoritma | Bulamadım (APK gerekli). | Bulamadım (APK gerekli). |
| Diyanet verisi | "GPS Diyanet" yöntemi sürüm notunda geçiyor [3. taraf]. Verinin Diyanet'ten mi çekildiği yoksa Diyanet parametreleriyle mi hesaplandığı **Doğrulanamadı.** | Mağaza metninde "Diyanet" ifadesi var; kaynak servis **Bulunamadı.** |
| Diğer ülke yöntemleri | ISNA, MWL, Umm al-Qura + 15'ten fazla [AÖ] | "Hesaplama yöntemleri" var; isimleri Bulunamadı |
| Saat dilimi / yaz saati | Bilinmiyor | Bilinmiyor |
| Yüksek enlem | Bir sürüm notunda "yüksek enlemler için GPS Diyanet yöntemi düzeltildi" [3. taraf]; yöntemin kendisi Bilinmiyor | Bilinmiyor |
| Hanefi ikindi | Doğrulanamadı | Doğrulanamadı |
| Manuel dakika düzeltmesi | Doğrulanamadı (bulduğum inceleme başka bir uygulamaya ait olabilir) | Bilinmiyor |
| Konum değişince güncelleme | Arka plan konum izni bunu yapıyor olabilir [Çıkarım] | Bilinmiyor |
| Çevrimdışı önceki veri | Aylık vakit indirme beyanı var [AÖ] | Bilinmiyor |
| Önceden hesaplama | Aylık (beyan) | Aylık tablo görünümü var; önbellekleme Bilinmiyor |

**Bilgi notu (genel, iki uygulamaya özel değil):** Diyanet, resmî namaz vakitlerini kendi web sitesi ve "Awqat Salah" adlı bir API servisi üzerinden yayımlıyor (bu bilgi genel bilgimdir; bu çalışmada siteye erişemediğim için **bu oturumda doğrulamadım**). Bir uygulamanın bu servisi kullanıp kullanmadığı ancak APK içindeki adreslerden veya ağ trafiğinden anlaşılabilir.

---

## 6. Ezan, alarm ve bildirim sistemi

### 6.1 Uygulamalarda gerçekten görülen işaretler

| Mekanizma | A | B |
|---|---|---|
| Kesin zamanlı alarm izni | Listede var [3. taraf] → **Kısmen doğrulandı** | Bilinmiyor |
| Açılışta yeniden kurma (BOOT_COMPLETED) | "Açılışta çalışma" izni listede var [3. taraf] → alarmların yeniden başlatmada kurulduğunu **düşündürür** [Çıkarım] | Bilinmiyor |
| Bildirim izni (Android 13+) | Listede var [3. taraf] | Bilinmiyor |
| AlarmManager / WorkManager kullanımı | **Doğrulanamadı** (kod gerekli) | **Doğrulanamadı** |
| Ezan sesinin çalınma şekli (ön plan servisi, süre, ses kanalı) | Doğrulanamadı | Doğrulanamadı |
| Sessiz mod / Rahatsız Etmeyin davranışı | Doğrulanamadı. Lite sürümde "camide otomatik sessize alma" beyanı var | Doğrulanamadı |
| Pil optimizasyonu | Doğrulanamadı | Doğrulanamadı |

### 6.2 Android platformunun genel kuralları (uygulamaların bunları nasıl uyguladığı bilinmiyor)

- **Android 12+:** Kesin zamanlı alarm kurmak için `SCHEDULE_EXACT_ALARM` izni gerekir; kullanıcı bunu ayarlardan kapatabilir. [Resmî Android]
- **Android 14+:** Android 13 ve üzerini hedefleyen **yeni kurulan** uygulamalara `SCHEDULE_EXACT_ALARM` artık otomatik verilmiyor. Alarm saati / takvim uygulamaları bunun yerine `USE_EXACT_ALARM` kullanabilir. İzin yoksa `setExact()`, `setExactAndAllowWhileIdle()`, `setAlarmClock()` çağrıları `SecurityException` hatası verir. [Resmî Android — developer.android.com/about/versions/14/changes/schedule-exact-alarms] — **Kısmen doğrulandı** (arama özeti üzerinden)
- **Android 13+:** Bildirim göstermek için `POST_NOTIFICATIONS` çalışma zamanı izni gerekir. (Genel bilgim; bu oturumda ayrıca doğrulamadım.)
- **Zorla durdurma:** Kullanıcı uygulamayı "Zorla durdur" yaparsa kurulmuş alarmlar iptal olur ve uygulama tekrar açılana kadar çalışmaz. (Genel bilgim; bu oturumda doğrulamadım.)
- **Üretici kısıtlamaları:** Xiaomi, Huawei, Samsung gibi bazı üreticilerin ek pil kısıtlamaları alarmları geciktirebilir. (Genel bilgim; bu oturumda kaynakla doğrulamadım.)

---

## 7. APK mimarisi ve kullanılan kütüphaneler

**APK'lara erişemediğim için bu bölüm yapılamadı.** Paket adı, sürüm ve SDK bilgileri Bölüm 3.1 ve 4.1'deki üçüncü taraf verileridir.

| İncelenmesi istenen | Durum |
|---|---|
| Manifest, Activity/Service/Receiver/Provider | Yapılmadı |
| Yerel veritabanı, SharedPreferences | Yapılmadı |
| Üçüncü taraf kütüphanelerin isimleri | Yapılmadı. A için yalnızca **sayı** (65), B için **sayı** (53) biliniyor [3. taraf] |
| Reklam/analiz SDK'ları | A: Reklam var. **Eski ve farklı** bir paket (`ezanvaktiproplus` 5.1.97, 2021) için Google Mobile Ads, Firebase ve Facebook Audience Network izleri görülmüş [AÖ — 3. taraf]. **Güncel A sürümü için Doğrulanamadı.** B: Reklam var; SDK adları **Bulunamadı.** |
| API uç noktaları | Bulunamadı |
| Şifreleme / hassas veri saklama | Yapılmadı. B'nin Play veri güvenliği bölümü "aktarımda şifreleme" beyan ediyor [AÖ] |
| Obfuscation (kod karartma) | Bilinmiyor |

---

## 8. Ağ bağlantıları, gizlilik ve güvenlik

**Ağ trafiği analizi yapılmadı.** Aşağıdakiler yalnızca beyanlardır.

| Konu | Uygulama A | Uygulama B |
|---|---|---|
| Gizlilik politikası | ezanvaktipro.com/en/privacy-policy (uygulamanın "Reklam destekli" olduğunu söylüyor; konum vakit hesabı ve kişiselleştirme için işleniyor) [AÖ]. deen-studios.com'daki resmî politikayı **bulamadım.** | namazvakitleri.com.tr/android-privacy-policy-en (son güncelleme 25 Ekim 2022): IP, cihaz özellikleri, işletim sistemi, ülke, **hassas veya yaklaşık konum**, kullanım bilgisi toplanabilir; amaç güvenlik, işletim ve iç analiz [AÖ] |
| App Store gizlilik etiketi | **Kaba konum**: üçüncü taraf reklam, pazarlama, analiz; **kimlik bilgileri takip için** kullanılabilir [AÖ] | "Geliştirici veri toplamıyor" beyanı (bir arama özetine göre) [AÖ] |
| Google Play veri güvenliği | Çelişkili: bir özet "konum üçüncü taraflarla paylaşılabilir", diğeri "kişisel veri toplanmaz/paylaşılmaz" diyor [AÖ] | Konum, uygulama etkinliği toplanabilir; üçüncü tarafla paylaşım yok; silme talebi mümkün [AÖ] |
| Hesap zorunluluğu | Bilinmiyor | Gerekmez (iOS beyanı) [AÖ] |
| Dikkat çeken izinler | Arka planda konum, diğer uygulamaların üzerinde gösterme, telefon durumu, reklam kimliği [3. taraf] | Liste Bulunamadı |

**Çelişkiler (gizlenmedi):**
- A: Google Play'de "veri paylaşılmaz" ifadesi ile App Store'daki "konum reklam/takip için kullanılabilir" beyanı **birbirini tutmuyor.** Hangisinin güncel olduğunu doğrulayamadım. Platformlar arası fark da olabilir.
- B: App Store'da "veri toplanmaz", gizlilik politikasında ise "konum ve cihaz bilgisi toplanabilir" yazıyor. **Çelişkili.**

**SDK güvenlik açıkları:** SDK isimlerini ve sürümlerini bilmediğim için bilinen açıkları araştıramadım.

---

## 9. Karşılaştırma tablosu

| Ölçüt | Uygulama A | Uygulama B |
|---|---|---|
| Vakit doğruluğu / hesaplama seçenekleri | Kısmen doğrulandı (15+ yöntem beyanı); doğruluk **Test edilmedi** | Kısmen doğrulandı (yöntem var beyanı); doğruluk **Test edilmedi** |
| Diyanet uyumu | Kısmen doğrulandı ("GPS Diyanet" yöntemi izi) | Kısmen doğrulandı (mağaza metni) |
| Alarm güvenilirliği | Test edilmedi | Test edilmedi |
| Bildirim seçenekleri | Kısmen doğrulandı | Kısmen doğrulandı (önce/vaktinde/sonra) |
| Kıble doğruluğu | Test edilmedi | Test edilmedi |
| Kur'an özellikleri | Kısmen doğrulandı — kapsamlı (meal 20+ dil, tecvid, ses) | Kısmen doğrulandı — sınırlı ("Kur'an ve dua oynatıcı") |
| Çevrimdışı çalışma | Kısmen doğrulandı (aylık vakit) | Doğrulanamadı |
| Arayüz / kullanım kolaylığı | Test edilmedi | Test edilmedi |
| Özelleştirme | Doğrulanamadı | Kısmen (widget boyutları, ezan sesleri) |
| Reklam yoğunluğu | Kısmen — reklam var; tam ekran reklam şikâyeti var | Kısmen — reklam var; yoğunluk bilinmiyor |
| Ücretli özellikler | Kısmen — ayrı ücretli "Pro Plus" uygulaması + uygulama içi satın alma | Kısmen — Pro üyelik (reklamsız + özel widget); fiyat bilinmiyor |
| Pil / bellek | Test edilmedi | Test edilmedi |
| Gizlilik ve izinler | Kısmen — 42-43 izin, arka plan konum, çelişkili beyanlar | Kısmen — 23 izin, çelişkili beyanlar |
| Android uyumluluğu | Kısmen — Android 9+ (son sürümler) | Kısmen — Android 7.0+ (çelişkili) |
| Teknik mimari / sürdürülebilirlik | Doğrulanamadı (Eylül 2026'da güncelleme izi var) | Doğrulanamadı (Şubat 2026'da güncelleme izi var) |

Yeterli kanıt olmadığı için **sayısal puan verilmedi.**

---

## 10. Gerçekleştirilen testler ve sonuçları

**Hiçbir çalışma zamanı testi yapılmadı.** Emülatör, adb, fiziksel cihaz ve APK yok.

Aşağıdaki plan, dosyalar ve uygun ortam sağlanınca uygulanacaktır:

| # | Senaryo | Beklenen davranış | Yapılan işlem | Sonuç | Güvenilirlik |
|---|---|---|---|---|---|
| 1 | Konum izni ver / verme | İzinsiz: manuel şehir seçimi istenmeli | — | Test edilmedi | — |
| 2 | İnternet açık / kapalı | Önbellekteki vakitler görünmeli | — | Test edilmedi | — |
| 3 | Şehir değiştirme | Vakitler ve alarmlar yenilenmeli | — | Test edilmedi | — |
| 4 | Sonraki vakit bildirimi | `adb shell dumpsys alarm` çıktısında alarm görünmeli | — | Test edilmedi | — |
| 5 | Yeniden başlatma | Alarmlar yeniden kurulmalı | — | Test edilmedi | — |
| 6 | Arka plana alma | Bildirim zamanında gelmeli | — | Test edilmedi | — |
| 7 | Pil tasarrufu / Doze (`adb shell dumpsys deviceidle force-idle`) | Kesin alarm Doze'da da tetiklenmeli | — | Test edilmedi | — |
| 8 | Saat dilimi / tarih değişimi | Vakitler yeniden hesaplanmalı | — | Test edilmedi | — |
| 9 | Kıble pusulası | Bilinen bir konum için ~ doğru açı (ör. İstanbul için yaklaşık 151–152°; bu değer genel bilgimdir, bu oturumda doğrulamadım) | — | Test edilmedi | — |
| 10 | Depolama / bellek / pil (`dumpsys meminfo`, `batterystats`) | — | — | Test edilmedi | — |
| 11 | Reklam ve ağ bağlantıları (sertifika sabitlemesini aşmadan, ör. DNS kayıtları) | — | — | Test edilmedi | — |

---

## 11. Bilinen sorunlar ve teknik sınırlamalar

1. **Ağ politikası:** Bu bulut ortamının ağ ayarı Google Play, App Store, APK siteleri ve Diyanet sunucularını engelliyor. WebFetch aracı DNS çözümleyemedi.
2. **Arama özetleri sınırlı:** Kaynak sayfaları kendim açıp okuyamadım; arama motoru özetine güvenmek zorunda kaldım.
3. **Analiz araçları yok:** jadx, apktool, aapt2, adb kurulu değil (Java var; jadx/apktool kurulabilir ama indirme sitelerine erişim bilinmiyor).
4. **APK yasal edinimi:** Google Play'den APK indirmek normalde bir Google hesabı ve cihaz gerektirir. Üçüncü taraf APK sitelerinden indirilen dosyalar değiştirilmiş olabilir; kullanılacaksa imza (sertifika) karşılaştırması yapılmalıdır.
5. **Kullanıcı yorumu:** A için "tam ekran reklamların arttığı" şikâyeti bir yorumda görülmüş [AÖ]; genel bir yaygınlık iddiası değildir.
6. **Eski veriler:** Puan, indirme ve sürüm bilgileri aramaların yakaladığı anlık görüntülerdir; güncel olmayabilir.

---

## 12. Doğrulanamayan bilgiler

- Her iki uygulamanın **hedef SDK** sürümü.
- Vakitlerin **sunucudan mı, cihazda mı** üretildiği; kullanılan algoritma/kütüphane.
- **Diyanet verisinin hangi servis üzerinden** alındığı (varsa).
- **Hanefi ikindi** seçeneği (iki uygulamada da).
- **Manuel dakika düzeltmesi** (iki uygulamada da).
- **Yüksek enlem** yöntemi.
- AlarmManager / WorkManager / ön plan servisi kullanımı.
- Ezan sesinin ne kadar süre çaldığı, sessiz mod / Rahatsız Etmeyin davranışı.
- Reklam ve analiz SDK'larının **isimleri** (güncel sürümler için).
- Ağ uç noktaları, konumun sunucuya gönderilip gönderilmediği.
- B'nin **izin listesi**, B'nin Pro fiyatı, B'nin çevrimdışı çalışması.
- A için Android'de **widget** ve **mushaf modu** (bunlar iOS benzer sürümde görüldü).
- Deen Studios'un resmî (deen-studios.com) gizlilik politikası.
- "Mobilexsoft" adının Deen Studios ile ilişkisi.
- Görevde bahsedilen **üçüncü uygulama**.

---

## 13. Sonuç ve teknik değerlendirme

**Doğrulanmış ölçüde söyleyebileceklerim (Kısmen doğrulandı seviyesinde):**
- İki uygulama da kimliği belirlenmiş, aktif güncellenen, reklam destekli, ücretli yükseltmesi olan namaz vakti uygulamalarıdır.
- **A**, Kur'an odaklı, çok özellikli ve çok izinli bir uygulamadır (arka plan konum, diğer uygulamaların üzerinde gösterme, reklam kimliği dahil). Hem uluslararası hesaplama yöntemleri hem de Diyanet'e yönelik bir mod sunduğu izlenimi vardır.
- **B**, daha sade, vakit-alarm-widget odaklı ve daha az izinli görünmektedir.
- İki uygulamanın da mağaza gizlilik beyanları ile politika metinleri arasında **çelişkiler** var.

**Söyleyemeyeceklerim:** Teknik mimari, vakit üretim yöntemi, alarm sisteminin iç yapısı ve veri akışı hakkında **kesin bir şey bilmiyorum.** Bunlar APK analizi ve kontrollü test olmadan doğrulanamaz.

### Analizi tamamlamak için gerekenler

1. **İki uygulamanın APK dosyaları** (en iyisi: kendi telefonunuza Google Play'den kurup `adb` ile çekilmiş hâli; bölünmüş APK'lar varsa hepsi — `base.apk` ve `split_*.apk`).
2. Görevde bahsedilen **ekran görüntüleri** (bana ulaşmadı).
3. Ya **bu bulut ortamının ağ izinlerinin genişletilmesi** (izin verilen alanlar: `play.google.com`, `apps.apple.com`, `reports.exodus-privacy.eu.org`, `www.appbrain.com`, `github.com` sürüm indirmeleri için `objects.githubusercontent.com`) ya da APK'ların proje klasörüne yüklenmesi.
4. Çalışma zamanı testleri için bir **Android cihaz veya emülatör** (bu bulut ortamında yok; kendi bilgisayarınızda Android Studio ile yapılabilir).
5. **Üçüncü uygulamanın adı** (varsa).

---

## 14. Kaynakça ve kanıtlar

Tüm erişimler: **25 Ekim 2026**. Kanıt türü: **[AÖ] arama özeti** — sayfalar doğrudan açılamadı.

**Uygulama A**
- Google Play – Azan Time Pro / Ezan Vakti Pro: https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvakti
- AppBrain – Azan Time Pro: https://www.appbrain.com/app/azan-time-pro-quran-qiblah/com.mobilexsoft.ezanvakti
- AppBrain – (diğer kayıt): https://www.appbrain.com/app/adhan-time-holy-quran-pro/com.mobilexsoft.ezanvakti
- AppBrain – Ezan Vakti Pro Plus: https://www.appbrain.com/app/ezan-vakti-pro-plus/com.mobilexsoft.ezanvaktiproplus
- Google Play – Ezan Vakti Pro Plus: https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvaktiproplus&hl=en_US
- Google Play – Azan Time Lite: https://play.google.com/store/apps/details?id=com.mobilexsoft.ezanvaktilite&hl=en_US&gl=US
- APKPure – Azan Time Pro: https://apkpure.com/azan-time-pro-quran-qiblah/com.mobilexsoft.ezanvakti
- aapks – Ezan Vakti Pro (Reklamsız) 5.1.97 (eski, farklı paket): https://aapks.com/apk/ezan-vakti/version/54569118/
- Gizlilik politikası (ezanvaktipro.com): https://ezanvaktipro.com/en/privacy-policy/
- App Store – Azan Time Pro: Holy Quran: https://apps.apple.com/us/app/azan-time-pro-holy-quran/id437447439
- App Store – Azan Time Pro Plus (satıcı Deen Studios L.L.C-FZ): https://apps.apple.com/app/id442401423
- BlueStacks – Ezan Vakti Pro: Namaz, Kuran: https://www.bluestacks.com/tr/apps/lifestyle/azan-time-pro-quran-qiblah-on-pc.html
- YouTube – Azan Time Pro kanalı: https://www.youtube.com/ezanvaktipro

**Uygulama B**
- Google Play: https://play.google.com/store/apps/details?id=com.simsekburak.android.namazvakitleri
- Google Play – NvApps Ltd geliştirici sayfası: https://play.google.com/store/apps/developer?id=NvApps+Ltd&hl=en
- AppBrain: https://www.appbrain.com/app/namaz-prayer-times-qibla/com.simsekburak.android.namazvakitleri
- App Store (ID 1190002151, TR): https://apps.apple.com/tr/app/namaz-vakitleri-alarm-k%C4%B1ble/id1190002151?l=tr
- Spark by MWM: https://spark.mwm.ai/en/apps/namaz-prayer-times-qibla/1190002151
- Gizlilik politikası: https://namazvakitleri.com.tr/android-privacy-policy-en
- Kullanım şartları: https://namazvakitleri.com.tr/terms-and-conditions
- Resmî site: https://namazvakitleri.com.tr/en
- APKFab: https://apkfab.com/namaz-vakitleri/com.simsekburak.android.namazvakitleri
- apk.cafe: https://namaz-vakitleri.apk.cafe/
- Aptoide: https://namaz-vakitleri.tr.aptoide.com/app

**Android platformu**
- Android 14 – Kesin alarm değişikliği: https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
- Exodus Privacy (rapor bulunamadı; yöntem açıklaması için): https://exodus-privacy.eu.org/en/page/faq/
