package tr.kolay.namazvakti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** En Yakın Cami: OpenStreetMap (Overpass) cevabının işlenmesi. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CamiTest {
    /** Overpass API'nin döndürdüğü biçimde örnek cevap (nokta, alan ve adsız cami). */
    static final String SAMPLE = "{\"elements\":["
            + "{\"type\":\"way\",\"id\":1,\"center\":{\"lat\":39.9168,\"lon\":32.8605},"
            + "\"tags\":{\"amenity\":\"place_of_worship\",\"religion\":\"muslim\",\"name\":\"Kocatepe Camii\"}},"
            + "{\"type\":\"node\",\"id\":2,\"lat\":39.9420,\"lon\":32.8550,"
            + "\"tags\":{\"amenity\":\"place_of_worship\",\"religion\":\"muslim\"}},"
            + "{\"type\":\"node\",\"id\":3,\"lat\":39.9000,\"lon\":32.8600,"
            + "\"tags\":{\"name\":\"<b>Hacı</b> Bayram\",\"name:tr\":\"Hacı Bayram-ı Veli Camii\"}},"
            + "{\"type\":\"relation\",\"id\":4,\"tags\":{\"name\":\"merkezi yok\"}}"
            + "]}";

    @Test
    public void overpassCevabiMesafeyeGoreSiralanir() throws Exception {
        List<CamiPage.Cami> l = CamiPage.parse(SAMPLE, 39.9334, 32.8597);
        assertEquals(3, l.size()); // merkezi olmayan kayıt atlanır
        assertEquals("Cami (adı girilmemiş)", l.get(0).ad);
        assertEquals("Kocatepe Camii", l.get(1).ad);
        assertEquals("Türkçe ad tercih edilir", "Hacı Bayram-ı Veli Camii", l.get(2).ad);
        assertTrue(l.get(0).metre < l.get(1).metre && l.get(1).metre < l.get(2).metre);
        // Örnek noktalar arası kuş uçuşu: yaklaşık 1,0 km ve 1,85 km
        System.out.println("Mesafeler: " + l.get(0).metre + ", " + l.get(1).metre + ", " + l.get(2).metre);
        assertTrue(String.valueOf(l.get(0).metre), Math.abs(l.get(0).metre - 1000) < 120);
        assertTrue(String.valueOf(l.get(1).metre), Math.abs(l.get(1).metre - 1850) < 80);
    }

    @Test
    public void mesafeYazisi() {
        assertEquals("350 m", CamiPage.distanceText(347));
        assertEquals("10 m", CamiPage.distanceText(2));
        assertEquals("1,2 km", CamiPage.distanceText(1234));
    }

    @Test
    public void sorguDogruEtiketleriArar() {
        String q = CamiPage.query(41.0, 29.0, 3000);
        assertTrue(q, q.contains("[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"]"));
        assertTrue(q, q.contains("around:3000,41.000000,29.000000"));
        assertTrue(q, q.contains("out center"));
    }
}
