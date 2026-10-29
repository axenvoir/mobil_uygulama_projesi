# Expira — Ödüllü reklamları kurma ve test etme

## Uygulamadaki davranış

- Reklam yalnızca kullanıcı **Reklamı hazırla** ve ardından **Reklamı izle** eylemlerini seçerse istenir/gösterilir; uygulama açılışında reklam yüklenmez.
- Reklam izlemek isteğe bağlıdır. Reklamı kapatmak veya izlememek ürün, not, bildirim ya da başka temel özellikleri engellemez.
- Reklamın tamamlandığı Google Mobile Ads ödül callback’iyle doğrulanır; callback gelmeden puan verilmez. Puanlar cihazda saklanan uygulama içi sayaçtır; nakit değeri, devredilebilirliği veya uygulama dışı karşılığı yoktur.
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

Release workflow’u bu secret’ları Gradle’a aktarır, biçimlerini doğrular ve Google test kimlikleriyle imzalı release AAB üretimini durdurur. Bu değerler eklenmemişse **verify** işi (unit test, lint, debug build ve instrumentation) çalışabilir; ancak release artifact işi bilerek hata verir. Böylece yanlışlıkla reklamların kapalı olduğu veya test kimliği kullanan bir AAB’nin yayınlanması önlenir.

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
- Google Play geçmişi kontrol edilemezse reklam gösterilmez; bağlantı düzeldikten sonra **Satın alımları yeniden kontrol et** ile yeniden denenebilir.

Ödüllü reklamların üretim sunumu AdMob hesabı, gerçek kimlikler, consent mesajları, ülke/cihaz uygunluğu ve reklam envanterine bağlıdır. Debug’de test reklamının görünmesi, kapalı testte gerçek reklam envanterinin hazır olduğu anlamına gelmez.

## Henüz kullanıcıdan gerekenler

- Yukarıdaki iki AdMob ID’yi oluşturup GitHub Actions repository secrets’a eklemek.
- Google Play mağaza kaydında kullanılacak gizlilik politikası URL’si ve destek e-posta adresini sağlamak; bunlar olmadan sahte değer eklenmemelidir.
- Play Console Data Safety yanıtlarını ve mağaza beyanlarını, üretimde kullanılan AdMob/UMP SDK’larının güncel veri uygulamalarına göre tamamlamak.


> **Yerel script notu:** `./scripts/run_automated_tests.sh --release`, kimlikleri doğrulamak için özellikle `EXPIRA_ADMOB_APP_ID` ve `EXPIRA_REWARDED_AD_UNIT_ID` ortam değişkenlerinin tanımlı olmasını ister. `~/.gradle/gradle.properties` kullanıyorsanız scripti çalıştırmadan önce bu iki değişkeni de dışa aktarın.


## Resmî kaynaklar

- [Google Mobile Ads Android — rewarded ads](https://developers.google.com/admob/android/next-gen/rewarded)
- [Google Mobile Ads Android — privacy and UMP](https://developers.google.com/admob/android/next-gen/privacy)
- [Google Mobile Ads Android — test ads](https://developers.google.com/admob/android/next-gen/test-ads)
- [AdMob — rewarded ad policy](https://support.google.com/admob/answer/7313578?hl=en)
