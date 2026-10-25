#!/usr/bin/env bash
# Otomatik testleri çalıştırır (telefon gerekmez). Önce ./build.sh çalıştırılmalı.
# Gerekenler: ANDROID_JAR (API 34 android.jar), AXT_SHIM_JAR (tools/axt-shim ile üretilen jar).
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?ANDROID_JAR gerekli}"
: "${AXT_SHIM_JAR:?AXT_SHIM_JAR gerekli}"
[ -f build/res.apk ] || ./build.sh
cd tools/robotest
gradle test --console=plain "$@"
