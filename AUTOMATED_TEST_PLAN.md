# Expira Otomatik Test Planı

Bu proje için unit test, Room migration testi, Compose smoke testi, Billing geri yükleme testi ve cihaz smoke test scriptleri eklendi.

## Hızlı çalıştırma

Proje kök dizininde:

```bash
./scripts/run_automated_tests.sh
```

Bu komut şunları çalıştırır:

- `:app:testDebugUnitTest`
- `:app:lintDebug`
- `:app:assembleDebug`

Bağlı Android cihaz veya emülatör testleri için:

```bash
./scripts/run_automated_tests.sh --connected
```

Bu seçenek, unit/lint/build görevlerine ek olarak `:app:connectedDebugAndroidTest` çalıştırır. Room migration ve Compose smoke testleri bu aşamada çalışır.

Release doğrulaması için:

```bash
./scripts/run_automated_tests.sh --release
```

Bu seçenek release lint, release APK ve release AAB üretimini de kontrol eder. Release signing için proje kökünde geçerli `keystore.properties` bulunmalıdır.

Sadece test ve lint:

```bash
./scripts/run_automated_tests.sh --skip-build
```

## Cihaz smoke testi

Debug APK oluşturulduktan sonra uygulama açılışı, paket durumu, yeniden başlatma ve Android 13+ bildirim izin durumu için:

```bash
./scripts/run_device_smoke_tests.sh
```

Script, yetkili ilk cihazı seçer. Farklı paket veya activity kullanılması gerekiyorsa:

```bash
PACKAGE=com.expira.app.tr \
ACTIVITY=com.axenvoir.expira/.MainActivity \
./scripts/run_device_smoke_tests.sh
```

## Eklenen otomatik testler

### Unit testler

`BillingViewModelTest.kt` şu senaryoları doğrular:

- Billing bağlantısı varken geri yükleme sonucunun kullanıcı arayüzüne iletilmesi.
- Billing kullanılamadığında başarısız sonucun iletilmesi.
- Sonucun tüketildikten sonra temizlenmesi.

Mevcut domain testleriyle birlikte ürün tarihleri, filtreler ve model davranışları da çalışır.

### Room migration testi

`AppDatabaseMigrationTest.kt`:

- Sürüm 1 veritabanını oluşturur.
- Kategori ve ürün kaydı ekler.
- 1→2 migration çalıştırır.
- `storageLocation` sütununun eklendiğini doğrular.
- Mevcut ürün ve kategori ilişkisinin korunduğunu doğrular.

### Compose smoke testleri

`ExpiraSmokeTest.kt`:

- İlk açılış onboarding ekranının kapatılabildiğini doğrular.
- Ürünler ekranının görünür olduğunu doğrular.
- Ürünler ve Notlar sekmelerinin görünür olduğunu doğrular.

### Worker ve bildirim senaryoları

Worker testi cihaz/instrumentation katmanında `connectedDebugAndroidTest` ile çalıştırılacak şekilde script kapsamına alınmıştır. Android 13+ cihazlarda izin verilmiş ve reddedilmiş durumlar `run_device_smoke_tests.sh` tarafından raporlanır. Gerçek zamanlı günlük hatırlatma davranışı için emülatörün WorkManager işini çalıştırmasına izin verilmelidir.

## Ön koşullar

- JDK 17
- Android SDK ve platform/build-tools paketleri
- `ANDROID_HOME` veya `/usr/local/lib/android/sdk`
- Cihaz testleri için adb ve yetkili Android cihaz/emülatör
- Release testi için geçerli signing yapılandırması
