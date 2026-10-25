#!/usr/bin/env bash
# Android Studio veya Gradle eklentisi olmadan APK üretir.
# Gerekenler (Ubuntu): sudo apt install aapt dalvik-exchange zipalign apksigner default-jdk
# ANDROID_JAR: Android 14 (API 34) android.jar dosyasının yolu.
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?ANDROID_JAR değişkenine android.jar yolunu verin}"
OUT=build
APP=app/src/main
NAME=KolayNamazVakti
rm -rf "$OUT" && mkdir -p "$OUT/gen" "$OUT/classes"

echo "1/5 Kaynaklar (aapt)"
aapt package -f -m --auto-add-overlay \
  -M "$APP/AndroidManifest.xml" -S "$APP/res" -I "$ANDROID_JAR" \
  -J "$OUT/gen" -F "$OUT/res.apk" \
  --min-sdk-version 24 --target-sdk-version 34

echo "2/5 Java derleme (javac)"
find "$APP/java" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -nowarn -encoding UTF-8 -source 8 -target 8 -Xlint:-options \
  -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" @"$OUT/sources.txt"

echo "3/5 Dex (dx)"
dalvik-exchange --dex --min-sdk-version=24 --output="$OUT/classes.dex" "$OUT/classes"

echo "4/5 Paketleme ve hizalama"
cp "$OUT/res.apk" "$OUT/unsigned.apk"
(cd "$OUT" && aapt add -f unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

echo "5/5 İmzalama (apksigner)"
KS="${KEYSTORE:-keystore/imza.jks}"
if [ ! -f "$KS" ]; then
  mkdir -p "$(dirname "$KS")"
  keytool -genkeypair -keystore "$KS" -storepass "${KS_PASS:-kolaynamaz}" -keypass "${KS_PASS:-kolaynamaz}" \
    -alias kolay -keyalg RSA -keysize 3072 -validity 10000 \
    -dname "CN=Kolay Namaz Vakti" >/dev/null 2>&1
fi
apksigner sign --ks "$KS" --ks-pass "pass:${KS_PASS:-kolaynamaz}" --ks-key-alias kolay \
  --out "$OUT/$NAME.apk" "$OUT/aligned.apk"
apksigner verify "$OUT/$NAME.apk"
echo "Hazır: $OUT/$NAME.apk"
