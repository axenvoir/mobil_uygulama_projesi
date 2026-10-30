# Expira — Google Play Yayın Öncesi Kontrol Listesi

Bu liste koddan tek başına doğrulanamayacak Play Console, AdMob ve gerçek cihaz kontrolleri içindir. CI başarılı olsa bile bu maddeler tamamlanmadan global yayına hazır olduğu varsayılmamalıdır.

## 1. Mevcut Google Play satın alımları

Uygulama içindeki yeni abonelik/tek seferlik satış ekranı kaldırılmıştır. Google Play üzerinden daha önce alınmış reklamsız kullanım hakları ve geri yükleme akışı korunur. Önceden oluşturduğunuz ürünleri, aktif abonelikleri veya mevcut kullanıcı haklarını, etkisini değerlendirmeden silmeyin ya da devre dışı bırakmayın.

- `supporter_monthly`, `supporter_yearly`, `premium_monthly`, `premium_yearly` ve `premium_lifetime` ürün kimliklerinin kodda beklenen kimliklerle eşleştiğini doğrulayın.
- Daha önce satın alım yapmış lisans test hesabında uygulama hakkı algılıyor ve reklam kartını gizliyor.
- **Satın alımları geri yükle** geçmiş satın alımı tekrar kontrol ediyor.
- Çevrimdışı veya Google Play bağlantı hatasında reklam varsayılan olarak engelleniyor; tekrar kontrol eylemi kullanılabiliyor.
- Eski ürünlerden yeni satış başlatma UI’ı olmadığını, aktif kullanıcıların yenileme ve iptal davranışlarının Play Console’da bozulmadığını doğrulayın.

## 2. AdMob ve UMP ödüllü reklamları

Kurulum adımları ve ID biçimleri [`ADS_SETUP.md`](ADS_SETUP.md) içinde yer alır.

- [ ] AdMob ve signing değerleri yalnızca `play-production` GitHub Actions Environment secrets'ında `PLAY_PRODUCTION_ADMOB_APP_ID`, `PLAY_PRODUCTION_REWARDED_AD_UNIT_ID`, `PLAY_PRODUCTION_KEYSTORE_BASE64`, `PLAY_PRODUCTION_KEYSTORE_PASSWORD`, `PLAY_PRODUCTION_KEY_ALIAS` ve `PLAY_PRODUCTION_KEY_PASSWORD` adlarıyla yapılandırıldı; eski repository-level kopyalar kaldırıldı.
- AdMob ödül türü `Expira point`, miktarı `1` olarak yapılandırılmış.
- Tamamlanan her reklamda bir puan, beş puanda uygulama içi kozmetik Destekçi rozeti görünür; ödül metni, uygulama içi davranış ve AdMob yapılandırması birbiriyle eşleşiyor.
- Release workflow’u gerçek kimlikleri kullanıyor; Google test ID’leri release için kabul edilmiyor.
- UMP consent mesajı, gerekli ülkeler/bölgeler ve gizlilik seçenekleri AdMob hesabında yapılandırılmış.
- Ücretsiz kullanıcı reklamı yalnızca kendisi isterse hazırlar ve başlatır. Reklam daveti yalnızca eylemi ve 1 puan ödülünü açıklar; işletmeyi destekleme çağrısı yoktur. Reklamı izlememek/kapamak uygulamanın temel işlevlerini kısıtlamaz.
- Önceden reklamsız hak kazanmış kullanıcıya reklam gösterilmiyor.
- Tamamlanmayan reklama puan verilmiyor; tamamlanan reklama bir kez puan ekleniyor ve uygulama yeniden açıldığında sayaç korunuyor.
- Beşinci puan verildiğinde Destekçi rozeti görünür; reklama puan vermek zorunlu olmadığı gibi temel özellikleri de etkilemiyor.
- Consent, internet veya reklam envanteri sorunlarında kullanıcıya uygun mesaj gösteriliyor; yeniden deneme işlevi çalışıyor.
- Debug testinde Google test reklamları dışında gerçek üretim reklamı kullanılmıyor; geliştirici kendi canlı reklamına tıklamıyor.

## 3. Gerçek cihaz satın alma/reklam testi

Her senaryoyu kapalı testten yüklenen uygulamada ve lisans test hesabıyla deneyin:

- Ücretsiz kullanıcı için önce Google Play hak kontrolü tamamlanıyor, sonra reklam izni ve reklam birimi uygunsa reklam gösterilebiliyor.
- Önceden reklamsız hakkı olan hesapta reklam gösterilmiyor; hak uygulama yeniden açıldığında da korunuyor.
- UMP gizlilik formu açılıyor; gerekiyorsa gizlilik seçenekleri tekrar açılabiliyor.
- Reklam yükleme, gösterme, kapatma, tamamlayıp ödül alma, ağ kesintisi, uygulamayı arka plana alma ve uygulamaya geri dönme senaryoları çökme olmadan çalışıyor.
- Reklam tamamlanmadan kapatılırsa puan verilmediği; tamamlanınca 1 puanın yalnızca bir kez verildiği doğrulanıyor.
- Google Play satın alma geçmişi yüklenemezse reklam başlatılmıyor ve tekrar kontrol edilebiliyor.
- Uygulamayı reklamsız kullanma senaryosu da baştan sona deneniyor.

## 4. Bildirim testi

Android 13 veya üzeri gerçek cihazda:

- Hatırlatıcılar bölümündeki **Hatırlatıcıları etkinleştir** eylemi bildirimin kullanım nedenini açıklar ve Android 13+ runtime iznini yalnız bu açık kullanıcı eyleminde ister.
- İzin reddedilirse uygulama çökmez; kullanıcıya sistem bildirim ayarlarına gitme eylemi gösterilir. Test bildirimi kendi ayrı eyleminde kalır ve izni örtük biçimde istemez.
- İzin ilk redden sonra Settings'ten açıldığında hatırlatıcı planı yeniden kurulur; izin tekrar kapatılıp açıldığında schedule recover davranışı doğrulanır.
- Test bildirimi ve yaklaşan son kullanma tarihi bildirimi gelir.
- Bildirim içindeki ürün adı ve gün bilgisi seçilen dilde gösterilir.
- Bildirime dokununca uygulama açılır.
- Cihaz yeniden başlatıldıktan ve uygulama güncellendikten sonra hatırlatma işi devam eder.
- Bildirim kanalı cihaz ayarlarında doğru isimle görünür; bildirim izni sonradan kapatılırsa worker çökmez.

## 5. Veri, yedekleme ve cihaz uyumu

- Ürün, kategori ve not oluşturma/düzenleme/silme/sabitleme akışlarını gerçek cihazda deneyin.
- Room 1→3 migration testi eski şemadan ürün/not verisini korur ve yeni cüzdan tablolarını doğrular; eski sürümden güncelleme senaryosunu kapalı test cihazında da doğrulayın.
- İki eşzamanlı kategori oluşturma, CSV dışa aktarma ve paylaşma senaryolarını test edin.
- CSV içinde formül gibi yorumlanabilecek kullanıcı girişlerinin güvenli biçimde dışa aktarıldığını kontrol edin.
- Bulut yedekleme ve cihaz aktarımı testinde ürün/not/kategori verisi, Room içindeki destek puanı bakiyesi ve açılmış kozmetik paletler ile izin verilen kullanıcı tercihleri beklenen şekilde taşınır. Eski puan içe aktarımı için `support_reward_preferences.xml` geçici olarak allowlist'tedir; `reward_retry_preferences.xml`, crash marker, hatırlatıcı sağlık işareti ve geçici dosyalar yedeklenmez.
- Android 12+ cloud-backup kuralı, OS istemci tarafı şifreleme yeteneği olmadığını bildirirse yedeklemeyi atlar. Android 11 ve altındaki legacy full-backup yolu aynı şifreleme yeteneği güvencesini sağlamaz; bu cihazlarda bulut yedekleme davranışını ayrıca değerlendirin. Device-to-device transfer aynı allowlist ile ayrıca test edilmelidir.
- Beklenmedik kapanma sonrasında kullanıcıya ham stack trace gösterilmez ve uygulamaya devam edilebilir.

## 6. Erişilebilirlik ve cihaz matrisi

Ürünler, Notlar, ürün formu, not formu, Ayarlar ve destek ekranlarını aşağıdakilerde kontrol edin:

- Açık/koyu tema ve Material You dinamik renk açık/kapalı.
- Türkçe ve İngilizce; ayrıca İngilizce olmayan sistem dilinde İngilizce fallback.
- Küçük ve büyük ekranlı telefon; yatay/dikey yönlendirme.
- Sistem yazı boyutu %100, %130, %150 ve %200; klavye açık/kapalı.
- TalkBack açık.

Özellikle metin taşması, dokunma alanı, kontrast, odak sırası, seçili filtre/kategori/saklama yerinin ekran okuyucuda anlaşılması, hata mesajının doğru alanla ilişkisi, ödeme/reklam butonu durumunun açıkça okunması ve tüm ekranlardan geri dönüş kontrol edilmelidir.

## 7. Global Play Console ve gizlilik bilgileri

Aşağıdaki değerler geliştirici/uygulama sahibinden alınmadan uydurulmamalıdır:

- Gizlilik politikası URL’si `https://axenvoir.github.io/` ve destek e-posta adresi `axenvoir@yahoo.com` olarak uygulama sahibinden alınmıştır; uygulamadaki Ayarlar > Hakkında bağlantılarının açıldığını doğrulayın. Bilingual policy commit `a066ff9` ile yayımlandı ve canlı sayfa doğrulandı.
- Kullanım koşulları ve abonelik iptali açıklamaları, gerekiyorsa.
- Türkçe ve İngilizce mağaza kısa/uzun açıklamaları, ekran görüntüleri ve feature graphic.
- İçerik derecelendirmesi ve Play Console Data Safety formu.
- Play Console Data Safety ve gizlilik politikası, kullanılan AdMob/UMP sürümlerinin ve Play Billing’in güncel veri uygulamalarını doğru yansıtmalı; SDK veya uygulama davranışı değişirse ikisini de güncelleyin.
- Kapalı test kullanıcılarına uygulama ve gerekli Play ürünleri/özellikleri erişilebilir.
- Play Console’un güncel hedef API şartı ve hedef ülke gereklilikleri, yayın günü yeniden kontrol edilmeli.

## 8. Release son kontrolü

- [ ] `versionCode` Play Console’da kullanılmamış, artan bir değer; kullanıcı `102`’nin yüklendiğini doğruladığı için kaynak ağacındaki yeni aday kod `103`’tür.
- [ ] `versionName` yayın notlarıyla uyumlu; bu kaynak ağacındaki yeni aday sürüm `2.2.1`.
- [ ] Release secrets: imzalama anahtarı/değerleri ve iki production AdMob ID’si tanımlı.
- [ ] `play-production` environment oluşturuldu; required reviewer, yalnız `v*` deployment kuralı ve release environment secrets doğrulandı. `v*` tag'leri için yetkisiz create/update/delete işlemlerini önleyen GitHub ruleset etkin.
- [ ] `main` branch protection ve doğrudan push/bypass kuralları ekip yapısına göre belirlendi ve GitHub'da uygulandı.
- [ ] GitHub Actions main push'unda yalnız verify çalışıyor; release job'ı elle seçilen `versionName` tag'i ve birebir yazılan onay girdisi olmadan çalışmıyor. Tag çalıştırma anındaki tam `main` HEAD commit'ini gösteriyor. Protected environment onayı başarıyla test edildi.
- [ ] GitHub Actions’ta unit test, lint, debug build, 1→3 Room migration, reward-wallet ve Compose instrumentation işleri başarılı; signed release artifact'i yalnız korumalı elle dispatch sonrasında üretildi.
- [ ] İmzalı AAB kapalı test kanalına yüklendi; Play App Signing/sertifika ve cihaz uyumluluğu kontrol edildi.
- [ ] R8 mapping dosyası saklandı.
- [ ] Play Console pre-launch raporu, çökme/ANR ve reklam davranışı gözden geçirildi.
- [ ] Son gerçek cihazlarda dil, tema, bildirim, veri ve izin senaryoları tamamlandı.

## Yayın kararı

Aşağıdakiler tamamlanmadan global yayına geçmeyin: (1) gerçek AdMob ID’leri ve gizlilik/consent kurulumu, (2) mevcut satın alma haklarının korunması, (3) Data Safety ve mağaza bilgilerinin doğru beyanı, (4) CI ile imzalı kapalı test AAB’si, (5) gerçek cihaz ve erişilebilirlik testleri.
