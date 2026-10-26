package tr.kolay.namazvakti;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

/** Alt menüdeki sayfaların ortak temeli. Her sayfa kendi görünümünü oluşturur. */
abstract class Page {
    final MainActivity activity;
    final View view;

    Page(MainActivity activity, ViewGroup parent, int layout) {
        this.activity = activity;
        this.view = activity.getLayoutInflater().inflate(layout, parent, false);
    }

    /** Sayfa ekrana gelince (veya uygulama öne gelince). */
    void onShow() {}

    /** Sayfa gizlenince (veya uygulama arka plana gidince). */
    void onHide() {}

    /** Sayfa görünürken her saniye çağrılır. */
    void onTick(long now) {}

    void onActivityResult(int requestCode, int resultCode, Intent data) {}

    /** Bir izin penceresi kapandığında (sayfa ekrandayken). */
    void onPermissionResult(int requestCode) { onShow(); }

    /** Pencere kapanırken (ör. seslendirme motorunu kapatmak için). */
    void onDestroy() {}

    float scale() { return Prefs.textScale(activity); }
}
