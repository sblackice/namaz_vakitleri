// Bağımsız karşılaştırma verisi üretir: açık kaynak "adhan" (MIT) kütüphanesinin
// Turkey yöntemiyle 81 il x 26 tarih için vakitler.
// Kullanım: TZ=UTC node referans_uret.js <adhan klasörü> <Cities.java> > referans.json
process.env.TZ = 'UTC';
const fs = require('fs');
const adhan = require(process.argv[2]);
const src = fs.readFileSync(process.argv[3], 'utf8');
const block = (name) => src.split('static final ' + name)[1].split('};')[0];
const names = [...block('String[] NAMES').matchAll(/"([^"]+)"/g)].map(m => m[1]);
const lats = [...block('double[] LAT').matchAll(/-?\d+\.\d+/g)].map(m => +m[0]);
const lons = [...block('double[] LON').matchAll(/-?\d+\.\d+/g)].map(m => +m[0]);
const dates = [];
for (let m = 1; m <= 12; m++) { dates.push([2026, m, 1]); dates.push([2026, m, 15]); }
dates.push([2027, 6, 21]); dates.push([2027, 12, 21]);
const out = [];
names.forEach((n, i) => {
  const coords = new adhan.Coordinates(lats[i], lons[i]);
  const params = adhan.CalculationMethod.Turkey();
  for (const [y, mo, d] of dates) {
    const pt = new adhan.PrayerTimes(coords, new Date(y, mo - 1, d), params);
    out.push({ city: n, lat: lats[i], lon: lons[i], y, m: mo, d,
      t: [pt.fajr, pt.sunrise, pt.dhuhr, pt.asr, pt.maghrib, pt.isha].map(x => x.getTime()),
      qibla: adhan.Qibla(coords) });
  }
});
console.log(JSON.stringify(out));
