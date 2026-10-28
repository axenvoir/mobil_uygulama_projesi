# Expira — Kritik Hata Düzeltme Planı

**Dosya:** `.kilo/plans/1793293717000-expira-critical-fix-plan.md`
**Proje:** Expira (Android, Kotlin + Compose + Room + Hilt + WorkManager)
**Uygulama Kimliği:** `com.expira.app.tr` / **Namespace:** `com.axenvoir.sktplus`
**DB:** `skt_plus.db` (sürüm 2)

## Amaç

Kaynak kodunda bulunan kritik/orta/düşük öncelikli hataların güvenilir düzeltilmesi. Sadece kaynak-kod değişikliği; yeni özellik yok.

## Onaylanan Tasarım Kararları

- **D1 (Edit-mode null kayıt):** `observeById(id).first()` null dönerse ve düzenleme modundaysa yeni kayıt **oluşturulmaz**. Kullanıcıya "Kayıt bulunamadı. Yeni bir tane oluşturmak ister misiniz?" onayı sorulur. Onaylıysa **doğrudan add ekranına navigasyon** yapılır (`ProductEditScreen`/`NoteEditScreen` yeni `onNavigateToAdd: () -> Unit` parametresi alır; `AppNavHost` bunu `navController.navigate(Screen.AddProduct.route)` ile bağlar). Reddedilirse ekranda kalınır ve hata gösterilir. Yeni kayıt sadece `isEditMode == false` için. (Kullanıcı onayı alındı; navigation callback seçimi yapıldı.)
- **D2 (Splash NPE):** `provider.iconView` null-safe (`?.let`); nullsa animasyon atlanır, `provider.remove()` çağrılır.
- **D3 (Edge-to-edge):** `enableEdgeToEdge()` `setContent`tan **önce** çalıştırılır.
- **D4 (CrashReporter):** Handler içindeki `startActivity()` **kaldırılır**. Sadece `cacheDir/crash_last.txt` yazılır, sonra `previousHandler` zincirlenir. Çökme ekranı otomatik açılmaz (sessiz kalır); çünkü handler içinde Aktivite başlatmak güvenli değildir.
- **D5 (Migration):** `fallbackToDestructiveMigration()` production'da **kaldırılır**. `MIGRATION_1_2` korunur. Yeni sürüm için açık `Migration` eklenir.
- **D6 (Lint):** `abortOnError = true`, `checkReleaseBuilds = true`; sonra `warningsAsErrors = true` (release).
- **D7 (AllowBackup):** `android:allowBackup="false"`.

## Görev Listesi (Sıralı)

### Aşama 1 — Kritik

1. `ui/expiry/ProductEditViewModel.kt` `save()`: `existing == null && isEditMode` → `_editRecordMissing` event/state set et, `upsert` yapma.
2. `ui/notes/NoteEditViewModel.kt` `save()`: Aynı mantık, Note ile eşdeğer.
3. `ui/expiry/ProductEditScreen.kt`: `onNavigateToAdd: () -> Unit` parametresi ekle; `_editRecordMissing` onay sonrası callback çağır.
4. `ui/notes/NoteEditScreen.kt`: `onNavigateToAdd: () -> Unit` parametresi ekle; aynı mantık.
5. `ui/navigation/AppNavHost.kt`: `ProductEditScreen`/`NoteEditScreen` composiciónunda `onNavigateToAdd = { navController.navigate(Screen.AddProduct.route) }` (veya `Screen.AddNote.route`) bağla.
6. `ui/main/MainActivity.kt`: `provider.iconView?.let { ... animate ... }` + `provider.remove()`.
7. `ui/main/MainActivity.kt`: `enableEdgeToEdge()`'yi `setContent { ... }` blokundan önce taşı.
8. `CrashReporter.kt`: `startActivity()` satırını kaldır; sadece dosya yaz + `previousHandler`.
9. `data/local/AppDatabase.kt`: `fallbackToDestructiveMigration()` çıkarıldı; açık migration kalıyor.
10. `app/build.gradle.kts`: `abortOnError = true`, `checkReleaseBuilds = true`.
11. `AndroidManifest.xml`: `android:allowBackup="false"`.

### Aşama 2 — Davranış / güvenilirlik

12. `ui/main/MainScreen.kt`: Düzenleme ekranı açık ve dirty ise sekme geçişini engelle; kayıtsız-değişiklik onayı göster (cross-tab dirty state: edit route kontrolü + ViewModel dirty state).
13. `domain/util/DateFlow.kt`: `delay(...)` önce `coerceAtLeast(0)`.
14. `data/repository/ProductRepositoryImpl.kt`: `LOWER(name) = LOWER(:name)` ile case-insensitive kategori eşleşmesi.
15. `ProductRepositoryImpl.upsert()` + `ProductEditViewModel.save()`: Yeni kayıt (`id == 0`) için `insert` döndürülen `rowId`'yi nesne içine yansıt; `restoreProduct` undo'da duplicate oluşmayı önle (yeni id ile upsert).
16. `ui/expiry/ProductEditScreen.kt`: `rememberDatePickerState(key = uiState.expiryDate)`.
17. `ui/main/MainActivity.kt`: Bildirim izni `shouldShowRequestPermissionRationale` kontrolüyle akıllı istek.

### Aşama 3 — Kalite / test

18. `app/build.gradle.kts`: `warningsAsErrors = true` (sadece release).
19. `src/test/java/com/axenvoir/sktplus/...`: Repository ve ViewModel birim testleri (mevcut `ModelsTest`, `DateUtilsTest`, `FilterTest` yanına; D1/D11/D15 odaklı).
20. `app/build.gradle.kts`: `isMinifyEnabled = true` (release); ProGuard kuralları doğrula.
21. `libs.versions.toml`, `README.md`, `.github/workflows/build-aab.yml`: AGP/Gradle versiyon tutarlılığı (toml=9.4.1, README=9.3.1, workflow=9.5.0, wrapper=9.8.2).
22. `CrashDisplayActivity.kt`: `MaterialTheme` → `ExpiraTheme` (kosmetik, opsiyonel).

## Uç Durumlar / Riskler

- **D4 sessiz çökme:** Çökme dosyası yazılıyor ama otomatik gösterilmiyor. Kullanıcı çökmeyi görmez. Alternatif (kapsam dışı): uygulama başlangıçında `crash_last.txt` varsa göster.
- **D15 undo duplicate:** `restoreProduct` eski `id=0` nesnesi ile `upsert` çağırırsa yeni satır oluşturur. D15'de undo nesnesinin `id`'yi mevcut `rowId` ile güncellemek gerekir.
- **D12 cross-tab dirty:** `hiltViewModel()` düzenleme ekranına ait; `MainScreen`'den dirty state'e erişim navigation sınırı nedeniyle zordur. Geçici çözüm: tab değişiminde şu anki hedef edit route ise ve dirty ise onay diyaloğu göster (ViewModel factory ile veya üst seviye state ile).

## Doğrulama Planı

- `./gradlew :app:assembleDebug` başarılı.
- `./gradlew :app:lintDebug` uyarısız.
- Mevcut testler geçerli kalır; yeni D1/D11/D15 testleri geçer.
- Senaryolar: silinmiş kayıt düzenlemede yeni kayıt oluşturmuyor; onay sonrası add ekranına yönlendiriliyor; splash NPE yok; klavyeli ekranlarda içerik kapanmıyor; şema yükseltmede veri silinmiyor; "Gıda"/"gıda" kategori çoğaltılmıyor; undo sonrası duplicate oluşmuyor.

## Rollback

Her görev ayrı commit. Regresyon olduğunda ilgili dosya önceki haliyle geri alınır. Migration değişikliği production'da önce beta `skt_plus.db` ile test edilir.

## Açık Soru (yok)

Tüm kritik tasarım kararları net. Plan implementation-ready.