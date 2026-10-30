# Expira Otomatik Test Planı

Bu proje için unit test, Room migration testi, Compose smoke testi, Billing geri yükleme testi ve cihaz smoke test scriptleri eklendi. CI, unit/lint/debug doğrulamasından sonra gerçek bir Android emülatörü boot eder ve instrumentation testlerini bağlı cihaz göreviyle çalıştırır.

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

CI tarafından önceden boot edilmiş emülatörde yalnızca instrumentation testlerini çalıştırmak için:

```bash
./scripts/run_automated_tests.sh --connected-only
```

Bu seçenek `adb` ile bağlı ve yetkili cihazı doğrular, ardından yalnızca `:app:connectedDebugAndroidTest` görevini çalıştırır. CI workflow’undaki emulator action bu komutu API 35 `google_apis` x86_64 emülatöründe çalıştırır.

## CI emulator akışı

`.github/workflows/build-aab.yml` içindeki `verify` job’ı şu sırayı korur:

1. Android SDK içinde `sdkmanager` bulunursa lisansları kabul eder; araç PATH’te yoksa kurulu SDK konumlarında arar ve bulunamadığında runner’ın önceden kabul edilmiş lisanslarına dayanıp Gradle SDK doğrulamasını sürdürür.
2. `:app:testDebugUnitTest`, `:app:lintDebug` ve `:app:assembleDebug` görevlerini çalıştırır.
3. KVM’i etkinleştirir ve doğrulanmış `reactivecircus/android-emulator-runner@v2.38.0` action’ı ile API 35 `google_apis` x86_64 emülatörünü boot eder.
4. Emülatör hazır olduğunda `./gradlew --no-daemon :app:connectedDebugAndroidTest` komutunu çalıştırır; Room migration ve Compose smoke testleri bu instrumentation aşamasında koşar.
5. Testler başarısız olsa bile HTML/XML instrumentation raporlarını ve hata seviyesindeki emulator logcat’i `expira-connected-test-results` artifact’inde saklar.

Room 2.8.5 migration test serializer’ı ile AndroidX’in getirdiği eski serializer BOM’u arasındaki ABI çakışmasını önlemek için yalnızca debug ve instrumentation classpath’lerinde kotlinx.serialization ailesi 1.8.1’e hizalanır; release runtime bundan etkilenmez.

Emülatörün boot edilmesi, SDK/lisans kurulumu veya Gradle instrumentation görevi hata verirse workflow başarılı kabul edilmez. Release job’ı `verify` işinin tamamlanmasına bağlıdır; imzalama secret’ları ve production AdMob kimlikleri (`EXPIRA_ADMOB_APP_ID`, `EXPIRA_REWARDED_AD_UNIT_ID`) yoksa release artifact’leri bilerek üretilmez. Ayrıntı: `ADS_SETUP.md`.

Release doğrulaması için:

```bash
./scripts/run_automated_tests.sh --release
```

Bu seçenek release lint, release APK ve release AAB üretimini de kontrol eder; yerel `keystore.properties` olmadan başlamaz ve üretilen AAB/APK imzalarını sırasıyla `jarsigner` ve Android SDK `apksigner` ile doğrular. Production AdMob kimlikleri ve imzalama kurulumu için `ADS_SETUP.md` adımlarını tamamlayın; eksik veya Google test reklam kimlikleriyle release üretimi reddedilir.

Sadece test ve lint:

```bash
./scripts/run_automated_tests.sh --skip-build
```

## Cihaz smoke testi

Debug APK oluşturulduktan sonra uygulama açılışı, yeniden başlatılınca uygulamanın ön plana gelmesi ve Android 13+ bildirim AppOps durumunu görüntülemek için:

```bash
./scripts/run_device_smoke_tests.sh
```

Script yetkili ilk cihazı seçer ve bildirim izni/AppOps durumunu yalnızca ekrana yazar; bildirimin gerçekten ulaştığını veya tıklama akışını doğrulamaz. Bunlar gerçek cihaz manuel kontrol listesinden test edilmelidir. Farklı paket veya activity kullanılması gerekiyorsa:

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
- Ödüllü reklam uygunluğunun yalnızca doğrulanmış ücretsiz kullanıcı snapshot’ında açılması; kontrol edilmemiş veya ücretli hak sahibi durumunda kapalı kalması.

`SearchTextNormalizerTest.kt` Türkçe `İ/i/ı`, aksan ve boşluk normalizasyonunu; `NoteListViewModelTest.kt`, `ProductListViewModelTest.kt` ve `ProductEditViewModelTest.kt` veri gözlem hatasının boş listeye dönüşmemesini ve Retry sonrası başarılı yüklemeyi doğrular. `CsvSnapshotLoaderTest.kt` kaynak okuma hatalarını, `CsvExporterTest.kt` CSV alan kaçışını/formül nötrleştirmesini, `ExpiraPaletteTest.kt` statik palet kontrastı/preview/fallback davranışını, `SupportCosmeticCatalogTest.kt` de katalog kimliği/fiyat bütünlüğünü doğrular. Mevcut domain testleri ürün tarihleri, filtreler ve model davranışlarını da kapsar.

`PendingRewardStoreTest.kt`, ayrı bir store örneğinin aynı süreçte aynı SharedPreferences dosyasından bekleyen olay kimliğini okuyabildiğini ve eski bir olayın yeni olay anahtarını silemediğini doğrular. Bu test process-death ya da AdsManager/Room entegrasyonunu tek başına kanıtlamaz.

`ThemePreferencesTest.kt`, adlandırılmış palet seçiminin DataStore üzerinden sistem dinamik renklerini aynı işlemde kapatmasını doğrular.

### Room migration testi

`AppDatabaseMigrationTest.kt`:

- Sürüm 1 veritabanını oluşturur.
- Kategori, ürün ve not kayıtları ekler.
- 1→2→3 migration zincirini çalıştırır.
- `storageLocation` sütunu/default değerini, Room identity hash'ini, benzersiz kategori indeksini ve foreign key kısıtını doğrular.
- Reward wallet, idempotent işlem günlüğü ve kozmetik sahipliği tablolarının oluştuğunu; mevcut ürün, kategori ilişkisi ve not verisinin korunduğunu doğrular.
- İki repository üzerinden eşzamanlı kategori eklemesinin tek satır/ID oluşturduğunu doğrular.

`SupportRewardWalletTest.kt` gerçek Room işlemlerinde eski bakiye içe aktarımını, yinelenen reklam callback'inde çift puan verilmemesini, yetersiz bakiyede satın almanın geri alınmasını, kozmetik palet satın alımını ve harcama sonrasında kalıcı ömür-boyu rozet durumunu doğrular.

### Compose smoke testleri

`ExpiraSmokeTest.kt`:

- İlk açılış onboarding ekranının kapatılabildiğini doğrular.
- Ürünler ekranının görünür olduğunu doğrular.
- Ürünler ve Notlar sekmelerinin görünür olduğunu doğrular.
- Ayarlar içinden kozmetik palet mağazasının ve ücretsiz Grove paletinin erişilebilir olduğunu doğrular.

### Bildirim ve reklam senaryoları

Şu anda `ReminderWorker` için ayrı bir deterministik uçtan uca `WorkerTest` yoktur. `ReminderSchedulerGenerationTest.kt` generation değerinin eski değerden ilerlemesini ve yalnızca güncel tokenın geçerli sayılmasını; `ReminderHealthStoreTest.kt` ise ayrı store örneklerinin aynı süreçte paylaşılan yerel terminal-hata durumunu ve nesil koşullu temizlemeyi doğrular. `ReminderTimeChangeReceiverIntegrationTest.kt` zaman dilimi alıcısının ardından WorkManager'da etkin iş oluşmasını kontrol eder. Bu testler worker içindeki tüm eşzamanlı yarışları/işletim sistemi yeniden başlatmasını doğrulamaz. Cihaz smoke script’i paket/açılış ve izin durumunu kontrol eder; gerçek zamanlı zamanlama, Android 13+ izin reddi, cihaz yeniden başlatma ve bildirim tıklama davranışı gerçek cihazdaki manuel yayın kontrol listesinde doğrulanmalıdır. Ödüllü reklamların gösterim/ödül callback’i AdMob ve UMP test ortamına bağlı olduğundan burada otomatik ağ testi yapılmaz; debug build Google test reklam kimliklerini kullanır, release ise production ID’leri olmadan engellenir.

## Ön koşullar

- JDK 17
- Android SDK ve platform/build-tools paketleri
- `ANDROID_HOME` veya `/usr/local/lib/android/sdk`
- Cihaz testleri için adb ve yetkili Android cihaz/emülatör
- CI cihaz testleri için KVM destekli Linux runner ve `reactivecircus/android-emulator-runner@v2.38.0`
- Release testi için geçerli signing yapılandırması
