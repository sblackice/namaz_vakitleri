package tr.kolay.namazvakti;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** adhan (MIT) kütüphanesinden üretilmiş karşılaştırma verisini okur. */
final class Ref {
    String city;
    double lat, lon, qibla;
    int y, m, d;
    long[] t = new long[6];

    static List<Ref> load() throws Exception {
        InputStream in = Ref.class.getResourceAsStream("/referans.json");
        byte[] buf = in.readAllBytes();
        String s = new String(buf, StandardCharsets.UTF_8);
        Matcher mt = Pattern.compile("\\{\"city\":\"([^\"]+)\",\"lat\":([-0-9.]+),\"lon\":([-0-9.]+),"
                + "\"y\":(\\d+),\"m\":(\\d+),\"d\":(\\d+),\"t\":\\[([0-9,]+)\\],\"qibla\":([-0-9.eE]+)\\}")
                .matcher(s);
        List<Ref> out = new ArrayList<Ref>();
        while (mt.find()) {
            Ref r = new Ref();
            r.city = mt.group(1);
            r.lat = Double.parseDouble(mt.group(2));
            r.lon = Double.parseDouble(mt.group(3));
            r.y = Integer.parseInt(mt.group(4));
            r.m = Integer.parseInt(mt.group(5));
            r.d = Integer.parseInt(mt.group(6));
            String[] p = mt.group(7).split(",");
            for (int i = 0; i < 6; i++) r.t[i] = Long.parseLong(p[i]);
            r.qibla = Double.parseDouble(mt.group(8));
            out.add(r);
        }
        return out;
    }

    static Ref find(List<Ref> all, String city, int y, int m, int d) {
        for (Ref r : all) if (r.city.equals(city) && r.y == y && r.m == m && r.d == d) return r;
        throw new AssertionError("referans yok: " + city);
    }
}
