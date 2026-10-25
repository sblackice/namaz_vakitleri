package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Test;

/** İçerik listelerinin tutarlılığı (telefon gerekmez). */
public class ContentTest {
    private static final TimeZone IST = TimeZone.getTimeZone("Europe/Istanbul");

    @Test
    public void hadislerBuhariVeyaMuslimdenVeTekrarsiz() {
        assertEquals(54, Hadisler.LISTE.length);
        Set<String> seen = new HashSet<String>();
        for (Hadisler.Hadis h : Hadisler.LISTE) {
            assertTrue(h.kaynak, h.kaynak.startsWith("Buhârî, ") || h.kaynak.startsWith("Müslim, "));
            assertTrue(h.ravi, h.ravi.contains("(r."));
            assertTrue("tekrar: " + h.metin, seen.add(h.metin));
        }
    }

    @Test
    public void gununHadisiHerGunDegisirVeGeceYarisiDegisir() {
        long before = TestUtil.istanbul(2026, 10, 15, 23, 59);
        long after = TestUtil.istanbul(2026, 10, 16, 0, 1);
        int a = Hadisler.indexFor(before, IST), b = Hadisler.indexFor(after, IST);
        assertEquals((a + 1) % Hadisler.LISTE.length, b);
        assertEquals(a, Hadisler.indexFor(TestUtil.istanbul(2026, 10, 15, 0, 1), IST));
    }

    @Test
    public void diniGunlerTarihSirasindaVeBilgileriDolu() {
        long prev = Long.MIN_VALUE;
        for (DiniGunler.Gun g : DiniGunler.LISTE) {
            long t = g.baslangic(IST);
            assertTrue(g.ad + " sırası bozuk", t >= prev);
            prev = t;
            DiniGunler.Bilgi b = DiniGunler.bilgi(g.tur);
            assertTrue(g.ad + " bilgisi boş", b.metin.length() > 100);
        }
    }

    @Test
    public void regaibKandiliHerZamanPersembeAksami() {
        // Regaib Kandili her zaman Perşembe akşamıdır (Recep'in ilk Cuma gecesi).
        for (DiniGunler.Gun g : DiniGunler.LISTE) {
            if (g.tur != DiniGunler.REGAIB) continue;
            String d = Times.longDate(g.yil, g.ay, g.gun);
            assertTrue(d, d.endsWith("Perşembe"));
        }
    }

    @Test
    public void ekimSonundaIlkYaklasanUcAylar() {
        long now = TestUtil.istanbul(2026, 10, 25, 9, 0);
        DiniGunler.Gun first = DiniGunler.yaklasanlar(now, IST).get(0);
        assertEquals("Üç Ayların Başlangıcı", first.ad);
        assertEquals(46, DiniGunler.kalanGun(first, now, IST));
    }

    @Test
    public void dualarFatihaIleBaslar() {
        assertEquals("Fâtiha Sûresi", Dualar.LISTE[0].ad);
        assertTrue(Dualar.LISTE[0].arapca.startsWith("بِسۡمِ"));
        for (Dualar.Dua d : Dualar.LISTE) {
            assertTrue(d.ad, d.arapca.length() > 5 && d.okunus.length() > 5 && d.anlam.length() > 5);
            assertTrue(d.ad, d.kaynak.length() > 3);
        }
    }

    @Test
    public void geriSayimBicimi() {
        assertEquals("00:00:00", Times.hms(0));
        assertEquals("00:00:01", Times.hms(1));
        assertEquals("01:02:03", Times.hms((3600 + 120 + 3) * 1000L));
        assertEquals("12:00:00", Times.hms(12 * 3600 * 1000L));
    }
}
