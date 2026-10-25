package tr.kolay.namazvakti;

/** Şu anki zaman. Testlerde sabit bir saat verilebilsin diye ayrı tutuldu. */
final class Clock {
    private Clock() {}

    static volatile long fixedNow = 0;

    static long now() {
        return fixedNow != 0 ? fixedNow : System.currentTimeMillis();
    }
}
