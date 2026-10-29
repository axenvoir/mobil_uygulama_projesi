#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

CONNECTED=false
RELEASE=false
SKIP_BUILD=false
for arg in "$@"; do
  case "$arg" in
    --connected) CONNECTED=true ;;
    --release) RELEASE=true ;;
    --skip-build) SKIP_BUILD=true ;;
    -h|--help)
      cat <<'USAGE'
Kullanım: scripts/run_automated_tests.sh [seçenekler]

  --connected   Bağlı Android cihaz/emülatörde androidTest görevini de çalıştırır.
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

if [[ "$SKIP_BUILD" == false ]]; then
  run_gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
else
  run_gradle :app:testDebugUnitTest :app:lintDebug
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
  run_gradle :app:connectedDebugAndroidTest
fi

if [[ "$RELEASE" == true ]]; then
  run_gradle :app:lintRelease :app:assembleRelease :app:bundleRelease
fi

echo "Tüm istenen otomatik test görevleri başarıyla tamamlandı."
