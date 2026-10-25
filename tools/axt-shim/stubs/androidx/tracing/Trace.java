package androidx.tracing;
public final class Trace {
  private Trace() {}
  public static void beginSection(String label) {}
  public static void endSection() {}
  public static boolean isEnabled() { return false; }
  public static void forceEnableAppTracing() {}
}
