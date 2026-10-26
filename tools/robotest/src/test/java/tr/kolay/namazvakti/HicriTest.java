package tr.kolay.namazvakti;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/** Hicri takvim (Diyanet) ve dini gün tarihleri birbiriyle tutarlı mı? */
public class HicriTest {

    @Test
    public void diyanetinYayimladigiAyBaslari() {
        // Diyanet dini günler listelerinden (2026-2030)
        assertArrayEquals(new int[] {1447, 9, 1}, Times.hijriDiyanet(2026, 2, 19));
        assertArrayEquals(new int[] {1447, 8, 30}, Times.hijriDiyanet(2026, 2, 18));
        assertArrayEquals(new int[] {1448, 7, 1}, Times.hijriDiyanet(2026, 12, 10));
        assertArrayEquals(new int[] {1448, 9, 1}, Times.hijriDiyanet(2027, 2, 8));
        assertArrayEquals(new int[] {1449, 1, 1}, Times.hijriDiyanet(2027, 6, 6));
        assertArrayEquals(new int[] {1450, 10, 1}, Times.hijriDiyanet(2029, 2, 14));
        assertArrayEquals(new int[] {1452, 9, 1}, Times.hijriDiyanet(2030, 12, 26));
        System.out.println("Bugün (26 Ekim 2026) Hicri: "
                + Arrays.toString(Times.hijriDiyanet(2026, 10, 26)));
    }

    @Test
    public void tabloDisindaNullDoner() {
        assertNull(Times.hijriDiyanet(2040, 1, 1));
        assertNull(Times.hijriDiyanet(2020, 1, 1));
    }

    @Test
    public void aylar29Veya30GunVeSirali() {
        int[][] t = HicriTablo.AY_BASLARI;
        for (int i = 1; i < t.length; i++) {
            long d = Times.epochDay(t[i][2], t[i][3], t[i][4]) - Times.epochDay(t[i - 1][2], t[i - 1][3], t[i - 1][4]);
            assertTrue("ay uzunluğu " + d, d == 29 || d == 30);
            int expect = t[i - 1][1] == 12 ? 1 : t[i - 1][1] + 1;
            assertEquals(expect, t[i][1]);
        }
        assertTrue("tablo 2030 sonunu kapsamalı",
                Times.epochDay(t[t.length - 1][2], t[t.length - 1][3], t[t.length - 1][4])
                        > Times.epochDay(2031, 1, 1));
    }

    /** Dini gün listesindeki "26 Recep 1448" gibi Hicri tarihler takvim tablosuyla aynı olmalı. */
    @Test
    public void diniGunlerinHicriTarihleriTabloylaAyni() {
        Pattern p = Pattern.compile("^(\\d+) (\\p{L}+) (\\d{4})");
        int checked = 0;
        for (DiniGunler.Gun g : DiniGunler.LISTE) {
            Matcher m = p.matcher(g.hicri);
            if (!m.find()) continue;
            int day = Integer.parseInt(m.group(1));
            int month = Arrays.asList(Times.HIJRI_MONTHS).indexOf(m.group(2)) + 1;
            int year = Integer.parseInt(m.group(3));
            assertTrue(g.hicri, month > 0);
            int[] h = Times.hijriDiyanet(g.yil, g.ay, g.gun);
            assertArrayEquals(g.ad + " " + g.yil + "-" + g.ay + "-" + g.gun + " (" + g.hicri + ")",
                    new int[] {year, month, day}, h);
            checked++;
        }
        System.out.println("Hicri tarihi kontrol edilen dini gün: " + checked);
        assertTrue(checked > 40);
    }

    @Test
    public void diniGunler2030SonunaKadar() {
        DiniGunler.Gun last = DiniGunler.LISTE[DiniGunler.LISTE.length - 1];
        assertEquals(2030, last.yil);
        assertEquals(12, last.ay);
    }
}
