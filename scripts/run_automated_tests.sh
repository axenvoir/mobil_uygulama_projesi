#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

CONNECTED=false
CONNECTED_ONLY=false
RELEASE=false
SKIP_BUILD=false
for arg in "$@"; do
  case "$arg" in
    --connected) CONNECTED=true ;;
    --connected-only) CONNECTED=true; CONNECTED_ONLY=true ;;
    --release) RELEASE=true ;;
    --skip-build) SKIP_BUILD=true ;;
    -h|--help)
      cat <<'USAGE'
Kullanım: scripts/run_automated_tests.sh [seçenekler]

  --connected   Bağlı Android cihaz/emülatörde androidTest görevini de çalıştırır.
  --connected-only
                 Önceden hazırlanmış bağlı cihaz/emülatörde yalnızca instrumentation testlerini çalıştırır.
  --release     Release lint ve release bundle doğrulamasını ekler.
  --skip-build  Sadece test/lint görevlerini çalıştırır; derleme görevlerini atlar.
USAGE
      exit 0
      ;;
    *) echo "Bilinmeyen seçenek: $arg" >&2; exit 2 ;;
  esac
done

if [[ ! -x "$ROOT/gradlew" ]]; then
  echo "HATA: gradlew bulunamadı veya çalıştırılabilir değil: $ROOT/gradlew" >&2
  exit 1
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  if [[ -d /usr/lib/jvm/java-17-openjdk-amd64 ]]; then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  elif [[ -d /opt/java/openjdk ]]; then
    export JAVA_HOME=/opt/java/openjdk
  fi
fi

if [[ -z "${ANDROID_HOME:-}" && -d /usr/local/lib/android/sdk ]]; then
  export ANDROID_HOME=/usr/local/lib/android/sdk
  export ANDROID_SDK_ROOT="$ANDROID_HOME"
fi

run_gradle() {
  echo "+ ./gradlew --no-daemon $*"
  ./gradlew --no-daemon "$@"
}

if [[ "$CONNECTED_ONLY" == false ]]; then
  if [[ "$SKIP_BUILD" == false ]]; then
    run_gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
  else
    run_gradle :app:testDebugUnitTest :app:lintDebug
  fi
fi

if [[ "$CONNECTED" == true ]]; then
  if ! command -v adb >/dev/null 2>&1; then
    echo "HATA: --connected için adb gerekli." >&2
    exit 1
  fi
  adb wait-for-device
  if [[ -z "$(adb devices | awk 'NR > 1 && $2 == "device" {print $1; exit}')" ]]; then
    echo "HATA: bağlı ve yetkili Android cihaz/emülatör bulunamadı." >&2
    exit 1
  fi
  echo "Room migration ve Compose smoke instrumentation testleri çalıştırılıyor: :app:connectedDebugAndroidTest"
  run_gradle :app:connectedDebugAndroidTest
fi

if [[ "$RELEASE" == true ]]; then
  if [[ ! -s "$ROOT/keystore.properties" ]]; then
    echo "HATA: --release için keystore.properties ve geçerli release imza yapılandırması gerekli. Ayrıntı: ADS_SETUP.md" >&2
    exit 1
  fi
  if [[ -z "${EXPIRA_ADMOB_APP_ID:-}" || -z "${EXPIRA_REWARDED_AD_UNIT_ID:-}" ]]; then
    echo "HATA: Release için EXPIRA_ADMOB_APP_ID ve EXPIRA_REWARDED_AD_UNIT_ID ortam değişkenleri gerekli. Ayrıntı: ADS_SETUP.md" >&2
    exit 1
  fi
  if [[ ! "$EXPIRA_ADMOB_APP_ID" =~ ^ca-app-pub-[0-9]{16}~[0-9]{10}$ ||
        ! "$EXPIRA_REWARDED_AD_UNIT_ID" =~ ^ca-app-pub-[0-9]{16}/[0-9]{10}$ ]]; then
    echo "HATA: Production AdMob kimliklerinin biçimi geçersiz." >&2
    exit 1
  fi
  if [[ "$EXPIRA_ADMOB_APP_ID" == "ca-app-pub-3940256099942544~3347511713" ||
        "$EXPIRA_REWARDED_AD_UNIT_ID" == "ca-app-pub-3940256099942544/5224354917" ]]; then
    echo "HATA: Google test reklam kimlikleri release için kullanılamaz." >&2
    exit 1
  fi
  run_gradle :app:lintRelease :app:assembleRelease :app:bundleRelease

  AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
  APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
  if [[ ! -s "$AAB" || ! -s "$APK" ]]; then
    echo "HATA: Release APK veya AAB çıktısı bulunamadı." >&2
    exit 1
  fi
  command -v jarsigner >/dev/null 2>&1 || {
    echo "HATA: AAB imzasını doğrulamak için JDK jarsigner gerekli." >&2
    exit 1
  }
  AAB_SIGNATURE="$(jarsigner -verify "$AAB" 2>&1)" || {
    echo "HATA: Release AAB imzası doğrulanamadı." >&2
    exit 1
  }
  if ! grep -qi '^jar verified\.' <<<"$AAB_SIGNATURE"; then
    echo "HATA: Release AAB imzalı görünmüyor." >&2
    exit 1
  fi

  APKSIGNER="$(command -v apksigner || true)"
  if [[ -z "$APKSIGNER" && -n "${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}" ]]; then
    APKSIGNER="$(find "${ANDROID_HOME:-$ANDROID_SDK_ROOT}/build-tools" -type f -name apksigner -perm -u+x 2>/dev/null | sort -V | tail -n1 || true)"
  fi
  if [[ -z "$APKSIGNER" ]] || ! "$APKSIGNER" verify "$APK" >/dev/null; then
    echo "HATA: Release APK imzası doğrulanamadı; Android SDK apksigner aracını kontrol edin." >&2
    exit 1
  fi
fi

echo "Tüm istenen otomatik test görevleri başarıyla tamamlandı."
