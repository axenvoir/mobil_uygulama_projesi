#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE="${PACKAGE:-com.expira.app.tr}"
ACTIVITY="${ACTIVITY:-com.axenvoir.expira/.MainActivity}"
cd "$ROOT"

command -v adb >/dev/null 2>&1 || { echo "HATA: adb bulunamadı." >&2; exit 1; }
DEVICE="$(adb devices | awk 'NR > 1 && $2 == "device" {print $1; exit}')"
[[ -n "$DEVICE" ]] || { echo "HATA: yetkili Android cihaz/emülatör bulunamadı." >&2; exit 1; }
ADB=(adb -s "$DEVICE")

if [[ ! -f app/build/outputs/apk/debug/app-debug.apk ]]; then
  echo "Debug APK bulunamadı; oluşturuluyor."
  ./gradlew --no-daemon :app:assembleDebug
fi

"${ADB[@]}" install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null
"${ADB[@]}" shell pm clear "$PACKAGE" >/dev/null
"${ADB[@]}" shell am start -W -n "$ACTIVITY"

assert_running() {
  local top
  top="$("${ADB[@]}" shell dumpsys activity activities | grep -m1 'mResumedActivity' || true)"
  [[ "$top" == *"$PACKAGE"* ]] || {
    echo "HATA: uygulama ön planda değil: $top" >&2
    exit 1
  }
}

assert_running

SDK="$("${ADB[@]}" shell getprop ro.build.version.sdk | tr -d '\r')"
if [[ "$SDK" -ge 33 ]]; then
  PERMISSION="$("${ADB[@]}" shell cmd appops get "$PACKAGE" POST_NOTIFICATION 2>/dev/null || true)"
  echo "Bildirim izin durumu: $PERMISSION"
fi

"${ADB[@]}" shell am force-stop "$PACKAGE"
"${ADB[@]}" shell monkey -p "$PACKAGE" 1 >/dev/null
assert_running

echo "Smoke test başarılı: cihaz=$DEVICE paket=$PACKAGE sdk=$SDK"
