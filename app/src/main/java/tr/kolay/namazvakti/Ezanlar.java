package tr.kolay.namazvakti;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Uygulamanın içine gömülü (hazır) ezan sesleri.
 *
 * Bir kayıt eklemek için: ses dosyasını app/src/main/res/raw/ klasörüne küçük harfli, Türkçe
 * karaktersiz bir adla koyun (ör. ezan_saba.ogg) ve aşağıdaki LISTE'ye bir satır ekleyin.
 * Yalnızca telifsiz (kamu malı, CC0) veya özgür lisanslı (ör. CC BY, CC BY-SA) kayıtlar
 * eklenmelidir; lisans ve kaynak "kaynak" alanına yazılır ve ekranda gösterilir.
 *
 * Bu sürümdeki 3 kayıt (ezan_1/2/3.mp3, 30, 17 ve 17 saniye) depo sahibi tarafından verildi.
 * Dosyalarda kaynak, okuyan ve makam bilgisi yoktur; lisansları geliştirici tarafından
 * doğrulanmadı. Uygulama ilk kurulduğunda Ezan 1 seçili gelir.
 */
final class Ezanlar {
    private Ezanlar() {}

    static final class Ezan {
        final String dosya, ad, kaynak;

        Ezan(String dosya, String ad, String kaynak) {
            this.dosya = dosya;
            this.ad = ad;
            this.kaynak = kaynak;
        }

        String uri(Context c) {
            return "android.resource://" + c.getPackageName() + "/raw/" + dosya;
        }
    }

    /** dosya adı (uzantısız), ekranda görünen ad, kaynak ve lisans */
    static final Ezan[] LISTE = {
        new Ezan("ezan_1", "Ezan 1 (30 sn)", "Depo sahibinin verdiği kayıt"),
        new Ezan("ezan_2", "Ezan 2 (17 sn)", "Depo sahibinin verdiği kayıt"),
        new Ezan("ezan_3", "Ezan 3 (17 sn)", "Depo sahibinin verdiği kayıt"),
    };

    /** Telefonda gerçekten bulunan hazır ezanlar. */
    static List<Ezan> mevcut(Context c) {
        List<Ezan> out = new ArrayList<Ezan>();
        for (Ezan e : LISTE) {
            if (c.getResources().getIdentifier(e.dosya, "raw", c.getPackageName()) != 0) out.add(e);
        }
        return out;
    }

    static boolean hazirMi(String uri) {
        return uri != null && uri.startsWith("android.resource://");
    }
}
