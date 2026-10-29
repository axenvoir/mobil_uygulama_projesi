# Expira — Google Play Yayın Öncesi Kontrol Listesi

Bu belge, kod tabanında otomatik olarak doğrulanamayan Google Play ve gerçek cihaz kontrolleri içindir. CI başarılı olsa bile aşağıdaki manuel kontroller tamamlanmadan uygulama global yayına hazır kabul edilmemelidir.

## 1. Google Play Billing ürünleri

Play Console ürün kimlikleri kodla birebir eşleşmelidir:

- `supporter_monthly` — abonelik, aylık
- `supporter_yearly` — abonelik, yıllık
- `premium_monthly` — abonelik, aylık
- `premium_yearly` — abonelik, yıllık
- `premium_lifetime` — tek seferlik ürün

Her ürün için kontrol edin:

- Ürün aktif durumda.
- Fiyatı ve desteklenen ülkeleri tanımlı.
- Abonelik ürünlerinde aktif bir base plan bulunuyor.
- Kapalı test sürümündeki uygulamayla aynı application ID kullanılıyor.
- Test hesabı lisans test kullanıcıları arasında yer alıyor.

## 2. Gerçek cihaz satın alma testi

Her senaryo, kapalı testten yüklenmiş uygulamada ve lisans test hesabıyla denenmelidir:

- Her beş ürünün fiyatı uygulamada görünüyor.
- Aylık ve yıllık abonelik satın alma ekranı açılıyor.
- Ömür boyu ürün satın alma ekranı açılıyor.
- Satın alma iptal edildiğinde uygulama kilitlenmiyor.
- Bekleyen satın alma kullanıcıya açık şekilde bildiriliyor.
- Başarısız satın alma işleminde anlaşılır hata gösteriliyor.
- Satın alma sonrasında uygulama yeniden başlatıldığında yetki korunuyor.
- Uygulama kaldırılıp tekrar yüklendiğinde geri yükleme çalışıyor.
- Google Play'den abonelik iptal edildiğinde uygulama durumu beklenen sürede güncelleniyor.
- Premium, ömür boyu ve Destekçi yetkilerinin önceliği beklenen şekilde çalışıyor.

## 3. Bildirim testi

Android 13 veya üzeri gerçek cihazda:

- Bildirim izni ilk kullanımda isteniyor.
- İzin reddedilirse kullanıcı uygulama ayarlarına yönlendirilebiliyor.
- Test bildirimi geliyor.
- Ürün son kullanma tarihi yaklaşınca bildirim geliyor.
- Bildirime dokununca uygulama açılıyor.
- Cihaz yeniden başlatıldıktan sonra planlanan bildirimler çalışıyor.
- Bildirim kanalı cihaz ayarlarında anlaşılır isimle görünüyor.

## 4. Veri ve yedekleme

- Ürün, kategori ve not oluşturup CSV dışa aktarma deneyin.
- CSV paylaşılmadan önce dosyanın hassas veri içerebileceğini değerlendirin.
- Room migration testini eski sürümden güncel sürüme gerçek cihazda doğrulayın.
- Uygulama kaldırma ve yeniden yükleme sonrası beklenen yerel veri davranışını kontrol edin.
- Android yedekleme/cihaz aktarımı senaryosunda verilerin beklenen şekilde taşındığını kontrol edin.

## 5. Erişilebilirlik ve cihaz matrisi

Aşağıdaki kombinasyonlarda Ürünler, Notlar, Ürün Formu, Not Formu, Ayarlar ve Destek ekranlarını kontrol edin:

- Açık tema ve koyu tema
- Türkçe ve English
- Küçük telefon ve büyük telefon
- Sistem yazı boyutu %100, %130, %150 ve %200
- Klavye açık ve kapalı
- TalkBack açık

Özellikle kontrol edin:

- Seçili filtre, kategori ve depolama yeri ekran okuyucuyla anlaşılır.
- Hata mesajı doğru alanla ilişkilidir.
- Silme, sabitleme, geri dönme ve satın alma eylemleri anlaşılır okunur.
- Metin veya buton taşması yoktur.
- Dokunma alanları rahat kullanılabilir.

## 6. Play Console yayın bilgileri

Kullanıcıya ait bilgilerle tamamlanması gereken alanlar:

- Gizlilik politikası URL'si
- Destek e-posta adresi
- Kullanım koşulları URL'si, varsa
- İade ve abonelik iptali açıklaması
- Mağaza kısa açıklaması
- Mağaza uzun açıklaması
- Türkçe ve İngilizce ekran görüntüleri
- Feature graphic
- İçerik derecelendirmesi
- Data Safety formu

Bu bilgiler sağlanmadan uygulama içine veya mağaza sayfasına sahte URL ya da iletişim bilgisi eklenmemelidir.

## 7. Release kontrolü

- [ ] `versionCode`, önceki Play yüklemesinden büyük.
- [ ] `versionName` yayın notlarıyla uyumlu.
- [ ] İmzalı AAB Play Console'a yüklenebiliyor.
- [ ] Release AAB, kapalı test kanalına gönderildi.
- [ ] Çökme ve ANR raporları kontrol edildi.
- [ ] R8 mapping dosyası saklandı.
- [ ] Play Console ürünleri kapalı test kullanıcılarına açık.
- [ ] Son test cihazlarında uygulama açılıyor.

## Son karar

Aşağıdaki üç koşul birlikte sağlanmadan global yayına geçmeyin:

1. Billing ürünleri gerçek test hesabıyla başarılı.
2. Gizlilik/Data Safety ve mağaza bilgileri doğru kullanıcı verileriyle tamamlanmış.
3. Farklı cihaz, dil, tema, yazı boyutu ve TalkBack kontrolleri tamamlanmış.
