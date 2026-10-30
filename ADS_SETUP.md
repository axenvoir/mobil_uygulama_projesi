# Expira — Ödüllü reklamları kurma ve test etme

## Uygulamadaki davranış

- Reklam yalnızca kullanıcı **Reklamı hazırla** ve ardından **Reklamı izle** eylemlerini seçerse istenir/gösterilir; uygulama açılışında reklam yüklenmez.
- Reklam izlemek isteğe bağlıdır. Reklamı kapatmak veya izlememek ürün, not, bildirim ya da başka temel özellikleri engellemez.
- Reklamın tamamlandığı Google Mobile Ads ödül callback’iyle doğrulanır; callback gelmeden puan verilmez. Her tamamlanan reklam 1 puan verir. Harcanabilir puanlarla Sakin Alacakaranlık (5), Narenciye Defteri (10) ve Böğürtlen Arşivi (15) kozmetik paletleri kalıcı olarak açılır; ömür boyu kazanılan 5 puan da harcama yapılsa bile kalıcı Destekçi rozetini açar.
- Puanlar, işlem tekilleştirmesi ve bakiye/kozmetik sahipliği `skt_plus.db` içindeki Room cüzdanında saklanır; uygulamanın mevcut Android yedekleme ve cihaz aktarımı allowlist'i bu ana veritabanını içerir. Böylece puan bakiyesi ve açılmış paletler desteklenen/etkin yedekleme veya aktarımda veritabanıyla birlikte taşınabilir. Puanların nakit değeri veya uygulama dışı karşılığı yoktur ve devredilemez.
- AdMob’da ödül miktarı **1**, ödül adı **Expira point** olarak ayarlanmalıdır; uygulama metinleri bu yapılandırmayı varsayar.
- Önceden Google Play üzerinden alınmış reklamsız kullanım hakları kontrol edilir. Hakkı olan kullanıcıya reklam gösterilmez. Uygulama içindeki yeni abonelik/ürün satış ekranı kaldırılmıştır; eski satın alımların kontrolü ve geri yüklenmesi korunur.
- Google UMP izin akışı reklam talebinden önce çalışır. İzin alınamayan, reklam yüklenemeyen veya kimlikleri bulunmayan durumda uygulama reklamsız çalışmaya devam eder.

## 1. AdMob’da oluşturulacak kimlikler

1. AdMob hesabında Android uygulaması oluşturun. Uygulama Play Store’da listelenmemişse listelenmediğini seçerek devam edin.
2. Android paket adı **`com.expira.app.tr`** olmalıdır. Bu değer `app/build.gradle.kts` içindeki `applicationId` ile eşleşir.
3. AdMob’un verdiği **App ID**’yi kaydedin. Biçimi `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY` şeklindedir; ayırıcı `~` işaretidir.
4. Uygulama için bir **Rewarded** reklam birimi oluşturun. Ödül adı `Expira point`, miktarı `1` yapın.
5. Oluşan **Rewarded Ad Unit ID**’yi kaydedin. Biçimi `ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY` şeklindedir; ayırıcı `/` işaretidir.
6. Gerekli ülkeler/region’lar için AdMob **Privacy & messaging** bölümünde UMP mesajı ve gizlilik seçeneklerini yapılandırın. Google Play’deki gizlilik politikası ve Data Safety beyanı da kullandığınız SDK’ların güncel veri uygulamalarını yansıtmalıdır.

AdMob kimlikleri uygulama parolası değildir; yine de bunları kaynak koda veya Git commit’lerine yazmayın. Uygulama, Google’ın test reklam kimliklerini yalnızca `debug` yapısında kullanır. Gerçek reklam kimliklerini debug testinde kullanmayın ve kendi canlı reklamlarınıza tıklamayın.

## 2. GitHub Actions için gerekli secrets

Depo ayarlarında **Settings → Secrets and variables → Actions → New repository secret** yolunu açıp aşağıdaki iki değeri ekleyin:

| Secret adı | Değer |
|---|---|
| `EXPIRA_ADMOB_APP_ID` | AdMob Android App ID (`~` içeren) |
| `EXPIRA_REWARDED_AD_UNIT_ID` | Rewarded reklam birimi ID’si (`/` içeren) |
| `KEYSTORE_BASE64` | Play App Signing’e yükleme için kullanılan release JKS keystore dosyasının Base64 içeriği |
| `KEYSTORE_PASSWORD` | JKS keystore parolası |
| `KEY_ALIAS` | Keystore içindeki imza anahtarının alias adı |
| `KEY_PASSWORD` | İmza anahtarının parolası |

`KEYSTORE_BASE64` değerini JKS dosyasının Base64 kodlamasından oluşturun (Linux: `base64 -w0 expira-release.jks`; macOS: `base64 < expira-release.jks | tr -d '\n'`). Ham keystore’u veya parolaları repoya eklemeyin; GitHub secret alanlarına girin.

Release workflow’u AdMob ID biçimlerini doğrular, Google test kimlikleriyle release üretimini durdurur ve listelenen imzalama secret’ları olmadan imzalı APK/AAB üretmez. Bu değerler eklenmemişse **verify** işi (unit test, lint, debug build ve instrumentation) çalışabilir; release artifact işi bilerek hata verir.

## 3. Yerel release derlemesi

İsteğe bağlı olarak kimlikleri, Git’e eklenmeyen kullanıcı Gradle ayarına (`~/.gradle/gradle.properties`) şu adlarla ekleyebilirsiniz:

```properties
expiraAdMobAppId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
expiraRewardedAdUnitId=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

Alternatif olarak ortam değişkenlerini kullanın:

```bash
export EXPIRA_ADMOB_APP_ID='ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY'
export EXPIRA_REWARDED_AD_UNIT_ID='ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY'
```

Kimlikler yokken debug build Google test kimlikleriyle çalışır. Release `ADS_ENABLED` durumunu yalnızca biçimi doğru, boş olmayan ve Google test kimliklerinden farklı kimliklerle etkinleştirir. Release paketini üretmeden önce `versionCode` değerinin Play Console’da daha önce kullanılmadığını da doğrulayın.

## 4. Kapalı testte kabul senaryoları

Gerçek cihazda, kapalı testten yüklenmiş uygulama ve test hesabıyla aşağıdakileri kontrol edin:

- Yeni/ücretsiz hesapta geçmiş satın alım kontrolü biter; AdMob kimlikleri ve izin akışı uygunsa reklam yüklenir.
- Önceden reklamsız hak kazanmış hesapta reklam kartı gizlenir ve hak görünür kalır.
- İlk kullanımda gizlilik formu gerekiyorsa form açılır; daha sonra Ayarlar’daki **Gizlilik tercihleri** üzerinden tekrar açılabilir.
- Consent güncellemesi ağ hatası alırsa kullanıcı **Gizlilik kontrolünü yeniden dene** ile tekrar deneyebilir.
- Reklam hazırlanmadan önce, reklam yüklenemediğinde veya cihaz çevrimdışıyken kullanıcıya anlaşılır mesaj gösterilir ve uygulama kullanılabilir kalır.
- Reklam tamamlanmadan kapatılırsa puan eklenmez; tamamlanan reklamda AdMob yapılandırmasındaki miktar (1) bir kez eklenir.
- Puan sayacı uygulama yeniden açıldığında korunur.
- Reklamı izlememek veya gizlilik izni vermemek ürün/not/bildirim işlevlerini kısıtlamaz.
- Eski `support_points` bakiyesi ilk kullanımda yalnızca bir kez Room cüzdanına aktarılır; cüzdanı sıfırdan başlatmak veya uygulama verisini temizlemek puan/kozmetik kayıtlarını etkileyebilir.
- Google Play geçmişi kontrol edilemezse reklam gösterilmez; bağlantı düzeldikten sonra **Satın alımları yeniden kontrol et** ile yeniden denenebilir.

Ödüllü reklamların üretim sunumu AdMob hesabı, gerçek kimlikler, consent mesajları, ülke/cihaz uygunluğu ve reklam envanterine bağlıdır. Debug’de test reklamının görünmesi, kapalı testte gerçek reklam envanterinin hazır olduğu anlamına gelmez.

## Güncel durum ve yayın için kullanıcıdan gerekenler

- En son başarılı imzalı release workflow’u production AdMob ID secret’larının mevcut ve Google test ID’lerinden farklı olduğunu doğruladı. Bu doğrulama yalnızca secret/biçim kontrolüdür; AdMob uygulama eşleşmesini, UMP ülke ayarlarını, canlı reklam envanterini veya cihazda reklam dolumunu kanıtlamaz.
- `EXPIRA_ADMOB_APP_ID` ve `EXPIRA_REWARDED_AD_UNIT_ID` değerleri GitHub Actions secrets’ta zaten yapılandırılmış görünüyor. Bunları sohbet veya commit içinde paylaşmayın; yalnızca döndürülürlerse secret’ları güncelleyin.
- Kullanıcı tarafından sağlanan gizlilik politikası URL’si `https://axenvoir.github.io/`, destek adresi `axenvoir@yahoo.com` şeklindedir. URL kamuya açık olarak açılıyor; uygulamanın Ayarlar > Hakkında bölümünde gösterilir.
- Gizlilik politikasının önceki Türkçe/İngilizce sürümü 30 Ekim 2026’da yayımlandı. 2.2.0 adayı için yerel taslak; Room cüzdanı ve palet yedeğini, bir defalık eski puan içe aktarımını, hatırlatıcı sağlık işaretini, geçici reklam-ödülü retry ID'sini ve özel kilit ekranı bildirim önizlemesini açıklar. Bu yeni taslak henüz kamuya yayımlanmadı; son içerik uygulama sahibince onaylanıp yayınlanmadan Play sürümü hazır sayılmaz. SDK veya uygulama davranışı değiştiğinde politika ve Data Safety beyanını yeniden gözden geçirin.
- Play Console Data Safety ve reklam beyanları, kullanılan AdMob/UMP SDK’larının güncel veri uygulamalarına göre tamamlanmalıdır.


> **Yerel script notu:** `./scripts/run_automated_tests.sh --release`, kimlikleri doğrulamak için özellikle `EXPIRA_ADMOB_APP_ID` ve `EXPIRA_REWARDED_AD_UNIT_ID` ortam değişkenlerinin tanımlı olmasını ister. `~/.gradle/gradle.properties` kullanıyorsanız scripti çalıştırmadan önce bu iki değişkeni de dışa aktarın.


## Resmî kaynaklar

- [Google Mobile Ads Android — rewarded ads](https://developers.google.com/admob/android/next-gen/rewarded)
- [Google Mobile Ads Android — privacy and UMP](https://developers.google.com/admob/android/next-gen/privacy)
- [Google Mobile Ads Android — test ads](https://developers.google.com/admob/android/next-gen/test-ads)
- [AdMob — rewarded ad policy](https://support.google.com/admob/answer/7313578?hl=en)
