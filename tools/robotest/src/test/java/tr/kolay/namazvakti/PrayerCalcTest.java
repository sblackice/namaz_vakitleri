package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Hesaplama motoru, bağımsız adhan kütüphanesiyle karşılaştırılır. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PrayerCalcTest {

    @Test
    public void tumIllerVeTarihlerAdhanIleEnFazla1DakikaFarkli() throws Exception {
        List<Ref> refs = Ref.load();
        assertEquals(81 * 26, refs.size());
        int exact = 0, total = 0;
        long maxDiff = 0;
        for (Ref r : refs) {
            long[] mine = PrayerCalc.compute(r.lat, r.lon, r.y, r.m, r.d, null);
            for (int i = 0; i < 6; i++) {
                long diff = Math.abs(mine[i] - r.t[i]) / 60000;
                maxDiff = Math.max(maxDiff, diff);
                if (diff == 0) exact++;
                total++;
            }
        }
        System.out.println("Karşılaştırılan vakit: " + total + ", birebir aynı: " + exact
                + " (%" + (100 * exact / total) + "), en büyük fark: " + maxDiff + " dk");
        assertTrue("en büyük fark " + maxDiff + " dk", maxDiff <= 1);
        assertTrue(exact * 100 / total >= 90);
    }

    @Test
    public void ilKoordinatlariDogruSehirle() throws Exception {
        for (Ref r : Ref.load()) {
            int i = Cities.indexOf(r.city);
            assertTrue(r.city, i >= 0);
            assertEquals(r.city, r.lat, Cities.LAT[i], 1e-9);
            assertEquals(r.city, r.lon, Cities.LON[i], 1e-9);
        }
    }

    @Test
    public void kibleAcisiAdhanIleAyni() throws Exception {
        for (Ref r : Ref.load()) {
            assertEquals(r.city, r.qibla, PrayerCalc.qibla(r.lat, r.lon), 0.01);
        }
    }

    @Test
    public void vakitlerSiraliVeGunIcinde() throws Exception {
        for (Ref r : Ref.load()) {
            long[] t = PrayerCalc.compute(r.lat, r.lon, r.y, r.m, r.d, null);
            for (int i = 1; i < 6; i++) assertTrue(r.city, t[i] > t[i - 1]);
            assertTrue(t[5] - t[0] < 24 * 3600 * 1000L);
        }
    }

    @Test
    public void yuksekEnlemdeHesaplamaBozulmaz() {
        // Oslo, 21 Haziran: güneş 18° altına inmez; "gecenin yarısı" kuralı devreye girer.
        long[] t = PrayerCalc.compute(59.91, 10.75, 2026, 6, 21, null);
        for (int i = 1; i < 6; i++) assertTrue(t[i] > t[i - 1]);
        for (long x : t) assertFalse(x == 0);
    }

    @Test
    public void kullaniciDuzeltmesiUygulanir() {
        int[] off = {0, 0, 3, 0, -2, 0};
        long[] a = PrayerCalc.compute(39.92, 32.85, 2026, 10, 25, null);
        long[] b = PrayerCalc.compute(39.92, 32.85, 2026, 10, 25, off);
        assertEquals(3 * 60000L, b[2] - a[2]);
        assertEquals(-2 * 60000L, b[4] - a[4]);
        assertEquals(a[0], b[0]);
    }

    @Test
    public void kalanSureYazisi() {
        assertEquals("1 dakikadan az kaldı", Times.remaining(30000));
        assertEquals("5 dakika kaldı", Times.remaining(4 * 60000 + 1000));
        assertEquals("2 saat kaldı", Times.remaining(120 * 60000));
        assertEquals("1 saat 5 dakika kaldı", Times.remaining(65 * 60000));
    }

    @Test
    public void aramaTurkceHarfDuyarsiz() {
        assertEquals(CityActivity.simplify("İzmir"), CityActivity.simplify("izmir"));
        assertEquals(CityActivity.simplify("Şanlıurfa"), CityActivity.simplify("sanliurfa"));
        assertEquals(CityActivity.simplify("Iğdır"), CityActivity.simplify("igdir"));
    }
}
