# Expira — Ürün Ekleme ve Notlarım UI İncelemesi

**İnceleme tarihi:** 2026-10-29  
**Kapsam:** Ürün ekleme/düzenleme, ürün listesi aksiyonları, Notlarım listesi ve not düzenleme ekranları; bu akışların ViewModel, navigasyon ve Türkçe/İngilizce kaynak metinleri.

## Yönetici özeti

Ürün ve not akışları Compose + MVVM + Room yapısında; kayıtlar yerel akışlarla gözlemleniyor. Formlarda kaydedilmemiş değişiklik kontrolü, silme onayı, silme sonrası geri alma, arama, not sabitleme ve iki dil için kaynak metinler mevcut. Türkçe ve İngilizce kaynaklarda **126'şar anahtar var ve anahtar kümeleri eşleşiyor**.

Bu incelemede kullanıcıya doğrudan yansıyan en önemli kusurlar şunlar:

1. Kayıp bir kaydı düzenlerken “yenisini ekle” akışı, eski düzenleme ekranını geri yığında bırakıyor; yeni kayıt kaydedilince kullanıcı silinmiş kaydın düzenleme ekranına geri dönebiliyor.
2. Not düzenleme ekranındaki “kayıt bulunamadı” metni ürünlerden söz ediyor. Ürün düzenleme akışında da kayıt yüklenirken genel hata gösteriliyor; notta ise boş form sessizce açılıyor.
3. Notlar ve ürünler sayfalarındaki aşağı çekerek yenileme, veriyi yenilemeden yalnızca 800 ms'lik bir animasyon gösteriyor.
4. Not başlığı boşken Kaydet devre dışı; kullanıcı neden kaydedemediğini göremiyor. Not aramasında temizleme kısayolu da yok ve sonuç bulunmadığında aynı metin başlık ile alt metinde tekrarlanıyor.
5. Ürün formu doğrulama hatalarını alanın yanında değil snackbar ile veriyor; adet alanında sayısal klavye tanımlı değil.

Bu belge bir **inceleme ve uygulama planıdır**; bu commit'te uygulama kaynak kodunda davranış değişikliği yapılmamıştır.

## Ekran ve aksiyon envanteri

### Ürün ekleme/düzenleme

`ProductEditScreen.kt`:

- Üst çubukta geri/vazgeç; düzenleme varsa değişiklikleri atma onayı.
- Ürün adı ve adet alanları.
- Hazır kategori chip'leri ve yeni kategori alanı.
- Depolama yeri chip'leri.
- Tarih seçici; geçmiş tarih seçilince ayrıca onay.
- Kaydet düğmesi ve işlem sırasında devre dışı bırakma/gösterge.
- Kaydetme hataları snackbar ile gösteriliyor.

`ProductEditViewModel.kt`:

- Ürün adı, tarih, adet ve kategori kontrol ediliyor.
- Düzenleme kaydı bulunamazsa kaydetme sırasında kurtarma diyaloğu tetikleniyor.
- Mevcut ürün yüklenirken kayıt bulunamazsa genel hata state'i kuruluyor.

### Ürünler listesi ve butonları

`ProductListScreen.kt`:

- Ürün arama ve aramayı temizleme, durum sayaçları/filtreleri, ürün ekleme FAB'ı.
- Ürün kartına dokunma düzenleme ekranını açıyor; uzun dokunma ayrıntı alt sayfasını açıyor.
- Kartta kaydırma veya silme eylemi silme onayı, ardından Geri Al snackbar'ı gösteriyor.
- Boş liste durumunda ürün ekle CTA'sı bulunuyor.
- Ürün listesinde aşağı çekerek yenileme de gerçekte yerel veriyi yeniden sorgulamıyor; yalnızca gecikmeli gösterge çalıştırıyor.

### Notlar listesi

`NoteListScreen.kt` ve `NoteListViewModel.kt`:

- Not arama; FAB ve boş liste CTA'sı yeni not açıyor.
- Nota dokunma düzenlemeyi; yıldız/kalp simgesi sabitlemeyi; silme simgesi ve sola kaydırma silme onayını açıyor.
- Silinen not için Geri Al sağlanıyor.
- Sabitlenmiş notlar önce, sonra güncellenme tarihine göre sıralanıyor; arama başlık ve içerikte yapılıyor.
- Arama sonucu yokken başlık ve alt açıklama aynı kaynağı kullanıyor.
- Arama kutusunda ürün listesindeki gibi bir “aramayı temizle” düğmesi yok.
- Pull-to-refresh yalnızca 800 ms bekleyip kapanıyor; not kaynağında yenileme işlemi yapmıyor.

### Not ekleme/düzenleme

`NoteEditScreen.kt` ve `NoteEditViewModel.kt`:

- Başlık, içerik, sabitleme, kaydetme ve geri/çıkış aksiyonları var.
- Başlık boşsa Kaydet düğmesi devre dışı; mevcut inline hata yalnızca ViewModel'e hata ulaştıysa gösterilebiliyor.
- İçeriğin ilk dolu satırından önerilen başlık oluşturuluyor.
- İçerik kaydedilirken baştaki ve sondaki boşluklar `trim()` ile atılıyor.
- Eksik kayıt için kaydetme sırasında kurtarma diyaloğu var; ilk yüklemede eksik kayıt açıkça gösterilmiyor.

## Bulgular ve öncelikler

### P1 — Düzenleme kaydı bulunamadığında kurtarma navigasyonu bozuk

**Kanıt:** `AppNavHost.kt:31-42, 53-64` içindeki `onNavigateToAdd` callback'leri doğrudan yeni ekleme rotasına `navigate` ediyor. Düzenleme rotası back stack'te kalıyor.  
**Etki:** Kullanıcı silinmiş ürün/not yerine yeni kayıt oluşturmaya yönlendirilse bile yeni kaydı kaydettikten sonra eski düzenleme ekranına dönebilir.  
**Plan:** Düzenleme kaydı eksikken onaylanan kurtarma akışında eski edit rotasını back stack'ten çıkar; yeni ekleme ekranı tamamlandığında ilgili listeye dönülsün. Ürün ve not için davranış aynı ve test edilebilir olmalı.  
**Kabul ölçütü:** Var olmayan bir ürün/notu düzenle; kurtarmayı onayla; yeni kaydı kaydet; hedef ürünler/notlar listesi görünmeli ve geri tuşuyla boş/bozuk eski edit ekranı açılmamalı.

### P1 — Kayıp kayıt mesajı notlar için yanlış, ilk yükleme davranışı tutarsız

**Kanıt:** `NoteEditScreen.kt:99-100` notlar için `edit_record_missing_message` kullanıyor; kaynak metin “Bu ürün artık mevcut değil…” / “This product no longer exists…”. `ProductEditViewModel.kt:143` ilk ürün yüklemesi başarısızsa `Generic` veriyor. `NoteEditViewModel.kt` kayıt yüklenemediğinde boş state'te kalıyor; `editRecordMissing` yalnızca kaydetme sırasında ayarlanıyor (`:116-130`).  
**Etki:** Yanlış bağlamda mesaj, genel hata veya boş form; kullanıcı silinmiş kayıtla mı yoksa yeni notla mı çalıştığını anlayamayabilir.  
**Plan:** Ürün ve not için ayrı yerelleştirilmiş bulunamayan kayıt metinleri/eylemleri tanımla. Kayıt ilk yüklemede yoksa açık bir state ve kullanıcı kararı göster; kaydı sessizce yeni kayıt gibi yazma.  
**Kabul ölçütü:** Türkçe ve İngilizce'de ürün metni ürün, not metni not demeli; kayıt yüklemede yoksa boş formun yeni kayıt gibi kaydedilmesi mümkün olmamalı.

### P1 — Yenileme hareketi sahte bir işlem sunuyor

**Kanıt:** `NoteListScreen.kt:112-120` ve `ProductListScreen.kt` pull-to-refresh callback'leri `delay(800)` sonrasında yalnızca `isRefreshing = false` yapıyor. Yerel Room Flow'ları dışında yenileme çağrısı yok.  
**Etki:** Kullanıcı yenilemenin veriyi eşitlediğini veya güncellediğini sanabilir.  
**Plan:** Veri tamamen yerel ve Flow ile canlı gözlemlendiği sürece gereksiz pull-to-refresh affordance'ını kaldır. İleride gerçek bir yenileme kaynağı eklenirse ancak o zaman gerçek işleme bağla; yapay gecikmeyi bırakma.  
**Kabul ölçütü:** Aşağı çekme kullanıcıya sonuçsuz bir yenileme animasyonu göstermemeli; mevcut kayıt değişiklikleri Room akışından görünmeye devam etmeli.

### P1 — Not başlığı boşken Kaydet neden çalışmıyor anlaşılmıyor

**Kanıt:** `NoteEditScreen.kt:150-153` Kaydet `title.isNotBlank()` koşuluyla devre dışı bırakılıyor; `NoteEditViewModel.save()` içindeki `EmptyTitle` hatası bu UI yolunda tetiklenemiyor.  
**Etki:** Boş formdaki kullanıcıya zorunlu alan nedeni açıkça anlatılmıyor; erişilebilirlikte devre dışı düğme bağlamı yetersiz.  
**Plan:** Boş başlık için alanla ilişkili, yerelleştirilmiş inline hata/yardım metni sun. Kaydet düğmesini ya açıklayıcı state ile devre dışı bırak ya da tıklamada doğrulama göster; aynı anda iki çelişkili doğrulama yolu bırakma.  
**Kabul ölçütü:** Başlık boşken zorunluluk görünür ve ekran okuyucu tarafından okunabilir; geçerli başlık girilince hata kalkar ve kayıt yalnızca bir kez yapılır.

### P1 — Ürün formunda doğrulama hataları alanla ilişkilendirilmiyor

**Kanıt:** `ProductEditScreen.kt:87-107` ürün formu hatalarını snackbar'a çeviriyor; metin alanlarında `isError`/`supportingText` yok. Adet alanı `:156-161` klavye seçenekleri almıyor.  
**Etki:** Snackbar kaybolabilir ve hangi alanın düzeltilmesi gerektiği, özellikle form uzadığında, belirsizleşir. Adet girişi de sayısal olmayan klavyeyle zorlaşır.  
**Plan:** Boş ad, eksik tarih/kategori ve geçersiz adet hatalarını ilgili kontrole bağla; kullanıcı alanı düzelttiğinde hata state'ini temizle. Adet için sayısal klavye ve uygun input semantics ekle. Formu dar ekran ve büyük yazı ölçeğinde doğrula; ad/adet satırının dar ekranda sıkışmasına karşı uyarlamalı yerleşim kullan.  
**Kabul ölçütü:** Her hata doğru alanın yanında ve erişilebilir biçimde görünür; adet alanı sayısal klavye açar; küçük ekran/büyük fontta etiket veya giriş değeri kırpılmaz.

### P2 — Not arama boş durumu tekrar ediyor; aramayı temizleme yok

**Kanıt:** `NoteListScreen.kt:151-155` aynı `empty_notes_search` hem başlık hem alt açıklama. Arama alanında `trailingIcon` temizleme aksiyonu yok (`:132-140`); ürün listesinde bu aksiyon mevcut.  
**Etki:** Sonuç bulunamadığında metin tekrarı ve aramadan çıkmak için gereksiz silme adımları.  
**Plan:** Ayrı bir kısa açıklama/öneri ekle (ör. sorguyu değiştir veya temizle); sorgu boş değilken erişilebilir temizleme düğmesi göster.  
**Kabul ölçütü:** Sonuç yok metni tekrarlanmamalı; temizleme düğmesi sorguyu boşaltıp tam not listesini geri getirmeli.

### P2 — Sabitleme eyleminin simgesi ve erişilebilir etiketi durumu anlatmıyor

**Kanıt:** `NoteListScreen.kt:301-312` sabitlenmiş ve sabitlenmemiş notlar için `contentDescription` aynı `pin` değeri. Sabitlenmemiş durumda pin yerine kalp simgesi kullanılıyor. `NoteEditScreen.kt:140-147` aynı sabitleme etiketi iki durumda da kullanılıyor.  
**Etki:** Ekran okuyucu mevcut durumu ve yapılacak eylemi ayırt edemiyor; kalp simgesi sabitleme davranışını net anlatmıyor.  
**Plan:** Eylem etiketlerini “Sabitle/Sabitlemeyi kaldır” ve İngilizce karşılıkları olarak duruma göre değiştir; görsel işareti pin/star metaforuyla tutarlı hale getir. Gerekirse semantiğe seçili durumu da dahil et.  
**Kabul ölçütü:** Eylem etiketi mevcut duruma göre değişir; dokunma sonrası etiket ve seçili durumu doğru güncellenir.

### P2 — Silme onayındaki olumlu/olumsuz düğmeler belirsiz

**Kanıt:** Ürün ve not silme diyaloğu `delete_confirm` ile birlikte `yes`/`no` etiketlerini kullanıyor (`ProductListScreen.kt:430-456`, `NoteListScreen.kt:224-251`).  
**Etki:** “Evet” düğmesinin hangi yıkıcı işlemi onayladığı, özellikle diyalog başlığı genelse, hızlıca tarayan kullanıcı için açık değil.  
**Plan:** Silme diyaloglarında açık “Sil” ve “İptal” eylemleri kullan; başlık/metni ürün/not bağlamına göre tanımla ve silinecek kayıt adını görünür tut. Geçmiş tarih ve kayıt kurtarma gibi evet/hayır kararlarını bu değişiklikten ayrı bırak.  
**Kabul ölçütü:** Silme eylemi açıkça adlandırılır; iptal silme yapmaz; onaydan sonra Geri Al davranışı korunur.

### P2 — Türkçe boş durum kopyası doğal değil; bazı alan metinleri gözden geçirilmeli

**Kanıt:** `values/strings.xml` içinde `empty_notes` metni “İlk notunuzu alın.”; İngilizce karşılığı “Take your first note.”  
**Etki:** Not oluşturma çağrısı Türkçede ve İngilizcede doğal ürün dili gibi okunmuyor.  
**Plan:** İki dilde kısa, eylem odaklı, aynı anlamı koruyan karşılık yaz (ör. Türkçe “İlk notunuzu oluşturun.”; İngilizcede doğal karşılığı). Dialog, snackbar, boş durum ve düğme metinlerini iki dilde birlikte kontrol et; anahtar kümelerinin eşleşmesini sürdür.  
**Kabul ölçütü:** TR/EN'de dilbilgisi ve eylem anlamı doğal; kaynak anahtarları iki dilde eksiksiz.

### P3 — Ek sağlamlaştırma ve kapsam dışı bırakılabilecek hususlar

- Not içeriği kaydedilirken `trim()` kullanılması (`NoteEditViewModel.kt:135`) baştaki/sondaki boşlukları ve satır sonlarını kaldırıyor. Kullanıcı metninin birebir korunması ürün kararıysa yalnızca başlık `trim()` edilmeli; içerik için beklenen davranış belirlenip test edilmeli.
- Başlık önerisi içerikten üretiliyor; öneri gösterimi ve 60 karakterde kaydetme davranışı aynı sınıra çekilmeli (`NoteEditViewModel.kt:99-106`).
- Ürün adı/kategori/not başlığı için üst uzunluk sınırı veya metin sayacı kodda görünmüyor. Önce ürün gereksinimi belirle; ardından UI sınırı ve yerel veri saklama sınırı birlikte uygulanmalı, keyfi limit ekleme.
- `ProductEditScreen` içinde kullanılmayan özel `CategorySelector` composable bulunuyor. Form değişikliği yapılırken ya ana akışta kullan ya da kaldır; aynı kategori UI'sının iki kopyasını tutma.
- Eski `.kilo/plans/1793215443823-ui-redesign-plan.md` içindeki bazı “bekliyor” durumları güncel kaynakla tekrar karşılaştırılmalı; örneğin Notlar empty state icon açıklaması mevcut kodda zaten verilmiş. Eski durum etiketlerini doğrulamadan uygulama iş listesi sayma.

## Önerilen uygulama sırası

### Aşama 1 — Doğruluk ve navigasyon

1. Kayıp kayıt state'ini ürün/not için ilk yükleme ve kaydetme anlarında tek bir açık akışa getir.
2. Ürün ve not için doğru yerelleştirilmiş kurtarma diyaloğu metinleri ekle.
3. Kurtarma onayıyla eski düzenleme rotasını kaldır; ekleme sonrası doğru listeye dön.
4. Ürün/not için ViewModel ve navigasyon testlerini ekle.

### Aşama 2 — Form doğrulama ve giriş ergonomisi

1. Not başlık doğrulamasını kullanıcıya görünür ve erişilebilir kıl.
2. Ürün alanı hatalarını inline ve ilgili alanla ilişkili hale getir; state düzeltildiğinde hatayı temizle.
3. Adet klavyesi, dar ekran ve yazı ölçeği yerleşimini düzelt.
4. Ürün tarih/kategori seçimi ve not başlık önerisi için uç durum testleri ekle.

### Aşama 3 — Liste, buton ve kopya iyileştirmeleri

1. Sahte pull-to-refresh davranışını ürün ve not listelerinden kaldır veya gerçek yenilemeye bağla.
2. Not arama temizleme butonu ve tekrarsız boş durum ekle.
3. Sabitleme etiketi/simgesini durumla tutarlı hale getir.
4. Silme onaylarında açık eylem metinleri ve doğal TR/EN kopyaları kullan.
5. Not içeriği trim davranışı ve metin uzunluğu limitlerini ürün kararı olarak netleştir.

### Aşama 4 — Doğrulama ve yayın öncesi QA

- Birim testleri: ürün/not kaydetme, boş alanlar, var olan kaydı düzenleme, kayıp kayıt, sabitleme, silme/geri alma.
- UI testi: boş durum, arama + temizleme, silme iptali/onayı, edit'ten kurtarma navigasyonu, klavye açıkken form.
- Kaynak kontrolü: TR/EN anahtar eşleşmesi ve yeni metinlerin görsel kontrolü.
- Android SDK bulunan ortamda `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
- Dar telefon, büyük font ölçeği, Türkçe/İngilizce ve açık/koyu tema ile manuel smoke test.

## Doğrulama durumu ve sınırlar

- Depo incelendi; başlangıçta çalışma ağacı temizdi.
- `./gradlew :app:testDebugUnitTest :app:lintDebug` denendi ancak bu çalışma ortamında Android SDK bulunmadığından Gradle `SDK location not found` hatasıyla görevleri başlatamadı. Bu nedenle kaynak değişikliği doğrulaması yapılmış gibi kabul edilmemelidir.
- Bu plan, ürün kararlarını belirsiz bırakan metin sınırları/ içerik trim davranışı gibi noktaları uygulama öncesi netleştirir; bu inceleme kapsamında keyfi davranış değişikliği yapılmamıştır.