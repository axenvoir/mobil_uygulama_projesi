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

- AdMob App ID ve Rewarded Ad Unit ID, GitHub Actions repository secrets’a `EXPIRA_ADMOB_APP_ID` ve `EXPIRA_REWARDED_AD_UNIT_ID` adlarıyla eklenmiş.
- AdMob ödül türü `Expira point`, miktarı `1` olarak yapılandırılmış.
- Release workflow’u gerçek kimlikleri kullanıyor; Google test ID’leri release için kabul edilmiyor.
- UMP consent mesajı, gerekli ülkeler/bölgeler ve gizlilik seçenekleri AdMob hesabında yapılandırılmış.
- Ücretsiz kullanıcı reklamı yalnızca kendisi isterse hazırlar ve başlatır. Reklamı izlememek/kapamak uygulamanın temel işlevlerini kısıtlamaz.
- Önceden reklamsız hak kazanmış kullanıcıya reklam gösterilmiyor.
- Tamamlanmayan reklama puan verilmiyor; tamamlanan reklama bir kez puan ekleniyor ve uygulama yeniden açıldığında sayaç korunuyor.
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

- Bildirim izni reddedilirse uygulama çökmez; test bildirimi sonucu kullanıcıya açıklanır.
- Test bildirimi ve yaklaşan son kullanma tarihi bildirimi gelir.
- Bildirim içindeki ürün adı ve gün bilgisi seçilen dilde gösterilir.
- Bildirime dokununca uygulama açılır.
- Cihaz yeniden başlatıldıktan ve uygulama güncellendikten sonra hatırlatma işi devam eder.
- Bildirim kanalı cihaz ayarlarında doğru isimle görünür; bildirim izni sonradan kapatılırsa worker çökmez.

## 5. Veri, yedekleme ve cihaz uyumu

- Ürün, kategori ve not oluşturma/düzenleme/silme/sabitleme akışlarını gerçek cihazda deneyin.
- Room 1→2 migration testi eski şemadan ürün/not verisini korur; eski sürümden güncelleme senaryosunu kapalı test cihazında da doğrulayın.
- İki eşzamanlı kategori oluşturma, CSV dışa aktarma ve paylaşma senaryolarını test edin.
- CSV içinde formül gibi yorumlanabilecek kullanıcı girişlerinin güvenli biçimde dışa aktarıldığını kontrol edin.
- Bulut yedekleme ve cihaz aktarımı testinde Room verisi ve kullanıcı tercihleri beklenen şekilde taşınır; crash marker ve geçici dosyalar yedeklenmez.
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

- Gizlilik politikası URL’si ve destek e-posta adresi.
- Kullanım koşulları ve abonelik iptali açıklamaları, gerekiyorsa.
- Türkçe ve İngilizce mağaza kısa/uzun açıklamaları, ekran görüntüleri ve feature graphic.
- İçerik derecelendirmesi ve Play Console Data Safety formu.
- Data Safety ve gizlilik politikası; kullanılan AdMob/UMP sürümlerinin güncel veri beyanlarını doğru yansıtmalı.
- Kapalı test kullanıcılarına uygulama ve gerekli Play ürünleri/özellikleri erişilebilir.
- Play Console’un güncel hedef API şartı ve hedef ülke gereklilikleri, yayın günü yeniden kontrol edilmeli.

## 8. Release son kontrolü

- [ ] `versionCode` Play Console’da kullanılmamış, artan bir değer; mevcut kod `100`.
- [ ] `versionName` yayın notlarıyla uyumlu; mevcut değer `2.0.2`.
- [ ] Release secrets: imzalama anahtarı/değerleri ve iki production AdMob ID’si tanımlı.
- [ ] GitHub Actions’ta unit test, lint, debug build, migration/Compose instrumentation ve imzalı AAB işleri başarılı.
- [ ] İmzalı AAB kapalı test kanalına yüklendi; Play App Signing/sertifika ve cihaz uyumluluğu kontrol edildi.
- [ ] R8 mapping dosyası saklandı.
- [ ] Play Console pre-launch raporu, çökme/ANR ve reklam davranışı gözden geçirildi.
- [ ] Son gerçek cihazlarda dil, tema, bildirim, veri ve izin senaryoları tamamlandı.

## Yayın kararı

Aşağıdakiler tamamlanmadan global yayına geçmeyin: (1) gerçek AdMob ID’leri ve gizlilik/consent kurulumu, (2) mevcut satın alma haklarının korunması, (3) Data Safety ve mağaza bilgilerinin doğru beyanı, (4) CI ile imzalı kapalı test AAB’si, (5) gerçek cihaz ve erişilebilirlik testleri.
