// Ezan Vakti Pro'nun Google Play tanıtım görselinde görünen örnek vakitleri,
// açık kaynak "adhan" (MIT) kütüphanesinin farklı hesaplama yöntemleriyle karşılaştırır.
// Varsayım: görseldeki şehir İstanbul, tarih Mart 2023 (görselde "2? Mart 2023 - 2? Şaban 1444").
// Kullanım: node ekran_vakit_karsilastir.js <adhan paket klasörü>
//   (adhan klasörü: npm pack adhan@4.4.3 && tar -xzf adhan-4.4.3.tgz → package/)
const adhan = require(process.argv[2]);
const istanbul = new adhan.Coordinates(41.01384, 28.94966); // GeoNames il merkezi (Cities.java ile aynı)
const saat = d => d.toLocaleTimeString('tr-TR', {timeZone: 'Europe/Istanbul', hour: '2-digit', minute: '2-digit'});
// Görseldeki değerler: İmsak, Güneş, Öğle (okunamadı), İkindi, Akşam, Yatsı
const ekran = ['05:36', '07:02', null, '16:40', '19:22', '20:42'];
const dk = s => { const [h, m] = s.split(':').map(Number); return h * 60 + m; };
const yontemler = {
  Turkey: adhan.CalculationMethod.Turkey(),
  MWL: adhan.CalculationMethod.MuslimWorldLeague(),
  ISNA: adhan.CalculationMethod.NorthAmerica(),
  UmmAlQura: adhan.CalculationMethod.UmmAlQura(),
};
console.log('Görsel             :', ekran.map(x => x || '  ?  ').join(' '));
for (const gun of [18, 19, 20, 21, 22]) {
  for (const [ad, p] of Object.entries(yontemler)) {
    const t = new adhan.PrayerTimes(istanbul, new Date(Date.UTC(2023, 2, gun, 9)), p);
    const v = [t.fajr, t.sunrise, t.dhuhr, t.asr, t.maghrib, t.isha].map(saat);
    const fark = v.map((x, i) => (ekran[i] ? dk(x) - dk(ekran[i]) : '')).filter(x => x !== '');
    const enBuyuk = Math.max(...fark.map(Math.abs));
    console.log(`2023-03-${gun} ${ad.padEnd(9)}: ${v.join(' ')}  fark(dk): ${fark.join(',')}  en büyük: ${enBuyuk}`);
  }
}
