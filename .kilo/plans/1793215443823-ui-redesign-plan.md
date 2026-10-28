# Expira — Kapsamlı Kod İnceleme ve Düzeltme Planı

## Mevcut Durum Analizi

Proje 57 Kotlin dosyası, 5 Compose ekranı, Room DB, Hilt DI, Google Play Billing, WorkManager ve Compose tema sistemi içeriyor. 8 farklı bakış açısından analiz yapıldı.

---

## Tespit Edilen Sorunlar

### KATEGORİ 1: Kritik (Crash / Data Loss Riski)

#### 1.1 Veri Kaybı — DB Adı Değişikliği
- **Dosya:** `AppDatabase.kt:16`
- **Sorun:** `skt_plus.db` → `expira.db` değişikliği. Room, yeni dosya adıyla boş veritabanı oluşturur. Mevcut kullanıcılar tüm verilerini kaybeder.
- **Etki:** Yüksek
- **Çözüm:** DB adını geri al (`skt_plus.db`) veya Room migration ekle.
- **Durum:** ✅ Uygulandı (DB adı geri alındı)

#### 1.2 BillingRepositoryImpl — Restore Purchases No-Op
- **Dosya:** `BillingRepositoryImpl.kt:46-48`
- **Sorun:** `refreshEntitlements()` boş. "Satın alımları geri yükle" butonu hiçbir şey yapmaz.
- **Etki:** Orta
- **Çözüm:** `billingManager.connect()` çağrısı + purchases re-query.
- **Durum:** ✅ Uygulandı (connect() çağrısı eklendi)

#### 1.3 BillingManager — Reconnection Logic Yok
- **Dosya:** `BillingManager.kt:44-50`
- **Sorun:** `onBillingServiceDisconnected` sadece state'i false yapar, yeniden bağlanma denemesi yok.
- **Etki:** Orta
- **Çözüm:** `onBillingServiceDisconnected` içinde `billingClient.startConnection(this)` çağrısı.
- **Durum:** ✅ Uygulandı

#### 1.4 BillingManager — acknowledgePurchase Hata Handling Yok
- **Dosya:** `BillingManager.kt:120-127`
- **Sorun:** `acknowledgePurchase(params) { }` boş callback.
- **Etki:** Orta (finansal risk)
- **Çözüm:** Callback'te hata logla.
- **Durum:** ✅ Uygulandı

#### 1.5 MainActivity — SideEffect Cast Riski
- **Dosya:** `MainActivity.kt:60-66`
- **Sorun:** `view.context as Activity` unsafe cast.
- **Etki:** Düşük
- **Çözüm:** Safe cast `as? Activity` + null check.
- **Durum:** ✅ Uygulandı

### KATEGORİ 2: Performans

#### 2.1 ProductListScreen — LaunchedEffect snapshotFlow
- **Dosya:** `ProductListScreen.kt:286-289`
- **Sorun:** `snapshotFlow { listState.firstVisibleItemIndex }` her scroll'da çalışır.
- **Etki:** Düşük
- **Çözüm:** `derivedStateOf` kullan.
- **Durum:** ⏳ Bekliyor

#### 2.2 FilterRow — trailingBadge Lambda
- **Dosya:** `ProductListScreen.kt:544-554`
- **Sorun:** Her recomposition'da yeni lambda oluşturur.
- **Etki:** Çok düşük
- **Durum:** ⏳ Bekliyor (backlog)

### KATEGORİ 3: Erişilebilirlik (Accessibility)

#### 3.1 ExpiraEmptyState — Icon contentDescription
- **Dosya:** `ExpiraEmptyState.kt:70`
- **Sorun:** `iconContentDescription` parametresi `null` default.
- **Etki:** Orta (a11y)
- **Çözüm:** Default değer olarak title veya generic description ver.
- **Durum:** ✅ Uygulandı (iconContentDescription ?: title)

#### 3.2 StatusDot — Semantics Eksik
- **Dosya:** `ExpiraStatusBadge.kt:18-27`
- **Sorun:** Decorative element ama `contentDescription = null` açıkça belirtilmemiş.
- **Etki:** Düşük
- **Çözüm:** `Modifier.semantics { contentDescription = null }` ekle.
- **Durum:** ✅ Uygulandı

#### 3.3 FilterRow Chips — Leading Icon Semantics
- **Dosya:** `ProductListScreen.kt:594, 603, 612`
- **Sorun:** `ExpiraStatusDot` leadingIcon olarak kullanılıyor.
- **Etki:** Düşük
- **Çözüm:** Dot'ları `Modifier.semantics { invisibleToUser() }` ile işaretle.
- **Durum:** ✅ Uygulandı

#### 3.4 ProductCard — Status Accent Bar Semantics
- **Dosya:** `ProductListScreen.kt:659-664`
- **Sorun:** Status accent bar (Box) decorative.
- **Etki:** Düşük
- **Çözüm:** `Modifier.semantics { invisibleToUser() }` ekle.
- **Durum:** ✅ Uygulandı

#### 3.5 NoteListScreen — Empty State Icon Content Description
- **Dosya:** `NoteListScreen.kt:151, 157`
- **Sorun:** `ExpiraEmptyState` iconContentDescription belirtilmemiş.
- **Etki:** Düşük
- **Çözüm:** iconContentDescription parametresi ekle.
- **Durum:** ⏳ Bekliyor

### KATEGORİ 4: Kod Kalitesi & Tutarlılık

#### 4.1 Shape.kt — Redundant Alias
- **Dosya:** `Theme.kt:64`
- **Sorun:** `val AppShapes = ExpiraShapes` — gereksiz alias.
- **Etki:** Çok düşük
- **Çözüm:** `AppShapes` kaldır, `ExpiraShapes` kullan.
- **Durum:** ✅ Uygulandı

#### 4.2 Motion.kt — Redundant Type Parameter
- **Dosya:** `Motion.kt:20-22`
- **Sorun:** `tween<Float>(...)` — type parameter gereksiz.
- **Etki:** Çok düşük
- **Çözüm:** `tween(...)` olarak değiştir.
- **Durum:** ✅ Uygulandı

#### 4.3 ExpiraStatusBadge.kt — Inconsistent Style
- **Dosya:** `ExpiraStatusBadge.kt:20, 33`
- **Sorun:** Fully-qualified `Box` ve `RoundedCornerShape` kullanımı.
- **Etki:** Çok düşük
- **Çözüm:** Import ekle ve fully-qualified kaldır.
- **Durum:** ✅ Uygulandı

#### 4.4 SettingsScreen.kt — Formatting
- **Dosya:** `SettingsScreen.kt:204`
- **Sorun:** `Spacer` satırı 0 indentation ile.
- **Etki:** Çok düşük
- **Çözüm:** Düzgün indentation uygula.
- **Durum:** ✅ Uygulandı

#### 4.5 SettingsScreen.kt — Switch Import Ordering
- **Dosya:** `SettingsScreen.kt:27`
- **Sorun:** `Switch` import'u `Text`'ten sonra.
- **Etki:** Çok düşük (lint warning)
- **Çözüm:** Import sırasını düzelt.
- **Durum:** ✅ Uygulandı

#### 4.6 NoteEditScreen.kt — leadingIcon Indentation
- **Dosya:** `NoteEditScreen.kt:142`
- **Sorun:** `leadingIcon = {` satırı tutarsız indentation.
- **Etki:** Çok düşük
- **Çözüm:** Indentation düzelt.
- **Durum:** ⏳ Bekliyor

#### 4.7 Unused Import: withStyle
- **Dosya:** `ProductListScreen.kt:65`
- **Sorun:** `import androidx.compose.ui.text.withStyle` kullanılmıyor.
- **Etki:** Çok düşük (lint warning)
- **Çözüm:** Import'u kaldır.
- **Durum:** ✅ Uygulandı

### KATEGORİ 5: UX İyileştirmeleri

#### 5.1 Tema Toggle Eksik
- **Dosya:** `SettingsScreen.kt`
- **Sorun:** Plan'da tema toggle (system/light/dark) ekleneceği belirtilmişti ama sadece dynamic color toggle var.
- **Etki:** Orta
- **Çözüm:** Settings ekranına tema seçimi RadioGroup ekle.
- **Durum:** 🔄 Kısmen (SettingsViewModel'e themeMode + setThemeMode eklendi, SettingsScreen UI'sı eksik)

#### 5.2 Reduce Motion Desteği Eksik
- **Dosya:** `ExpiraEmptyState.kt`, `Motion.kt`
- **Sorun:** Animasyon her zaman oynar.
- **Etki:** Düşük (a11y)
- **Çözüm:** `Settings.Global.ANIMATOR_DURATION_SCALE` kontrolü.
- **Durum:** ⏳ Bekliyor

#### 5.3 NoteListScreen — Search Field Styling
- **Dosya:** `NoteListScreen.kt:132-141`
- **Sorun:** OutlinedTextField shape belirtilmemiş.
- **Etki:** Düşük
- **Çözüm:** `shape = MaterialTheme.shapes.extraLarge` ekle.
- **Durum:** ✅ Uygulandı (zaten mevcut)

#### 5.4 NoteEditScreen — Content Field heightIn
- **Dosya:** `NoteEditScreen.kt:190-192`
- **Sorun:** `heightIn(min = 160.dp, max = 320.dp)` — max 320dp çok kısıtlayıcı.
- **Etki:** Düşük
- **Çözüm:** `max = 480.dp` veya max kaldır.
- **Durum:** ⏳ Bekliyor

### KATEGORİ 6: Güvenlik & Gizlilik

#### 6.1 CrashReporter — Hassas Veri
- **Dosya:** `CrashReporter.kt`
- **Sorun:** Crash raporlarında kullanıcı verisi loglanıyor olabilir.
- **Etki:** Orta (gizlilik)
- **Çözüm:** Crash raporlarında PII filtrele.
- **Durum:** ⏳ Bekliyor

#### 6.2 CSV Export — Dosya İzinleri
- **Dosya:** `CsvExporter.kt`
- **Sorun:** Export edilen CSV dosyası world-readable olabilir.
- **Etki:** Düşük
- **Çözüm:** Dosya izinlerini `MODE_PRIVATE` olarak ayarla.
- **Durum:** ⏳ Bekliyor

---

## Öncelik Sıralaması

### P0 (Kritik — Hemen Düzeltilmeli)
1. **1.1** DB adı değişikliği → veri kaybı (geri al veya migration ekle) — ✅ Uygulandı
2. **1.2** Restore purchases no-op — ✅ Uygulandı

### P1 (Yüksek — Bu Sprint)
3. **1.3** Billing reconnection — ✅ Uygulandı
4. **1.4** acknowledgePurchase error handling — ✅ Uygulandı
5. **1.5** SideEffect safe cast — ✅ Uygulandı
6. **5.1** Tema toggle UI — 🔄 Kısmen (ViewModel done, UI eksik)

### P2 (Orta — Sonraki Sprint)
7. **3.1** ExpiraEmptyState contentDescription default — ✅ Uygulandı
8. **3.2-3.4** Accessibility (semantics) — ✅ Uygulandı
9. **2.1** snapshotFlow optimizasyonu — ⏳ Bekliyor
10. **5.4** NoteList search field styling — ✅ Uygulandı
11. **4.3** ExpiraStatusBadge import tutarlılığı — ✅ Uygulandı
12. **3.5** NoteList empty state icon contentDescription — ⏳ Bekliyor

### P3 (Düşük — Backlog)
13. **4.1** AppShapes alias kaldırma — ✅ Uygulandı
14. **4.2** tween type param kaldırma — ✅ Uygulandı
15. **4.4** SettingsScreen formatting — ✅ Uygulandı
16. **4.5** Import ordering — ✅ Uygulandı
17. **4.6** NoteEditScreen indentation — ⏳ Bekliyor
18. **4.7** withStyle unused import — ✅ Uygulandı
19. **5.2** Reduce motion support — ⏳ Bekliyor
20. **6.1-6.2** Güvenlik iyileştirmeleri — ⏳ Bekliyor

---

## Kalan İşler (Uygulanacak)

### A. Tema Toggle UI (P1-5.1) — SettingsScreen'e eklenecek
- RadioGroup: System / Light / Dark
- ThemeMode observe + setThemeMode çağrısı
- Mevcut dynamic color toggle'ın üstüne eklenecek

### B. ExpiraEmptyState contentDescription (P2-3.5) — NoteListScreen
- iconContentDescription parametresi eklenecek (search icon ve edit icon için)

### C. NoteEditScreen indentation (P3-4.6)
- leadingIcon indentation düzeltilecek

### D. NoteEditScreen content field heightIn (P3-5.4)
- max = 320.dp → max = 480.dp

### E. Reduce Motion Support (P3-5.2)
- Settings.Global.ANIMATOR_DURATION_SCALE kontrolü
- ExpiraEmptyState animasyonunu koşullu yap

### F. Billing Reconnection Backoff (P1-1.3 iyileştirme)
- Exponential backoff ile yeniden bağlanma
- Max retry count

### G. CrashReporter PII Filtering (P3-6.1)
- Crash raporlarında ürün adları ve notlar filtrele

### H. CSV Export File Permissions (P3-6.2)
- Dosya izinlerini MODE_PRIVATE olarak ayarla

---

## Önerilen Uygulama Sırası

1. **A-D (Hızlı Düzeltmeler):** Tema toggle UI, empty state contentDescription, indentation, heightIn
2. **E-F (Orta):** Reduce motion, billing reconnection backoff
3. **G-H (Güvenlik):** CrashReporter PII, CSV permissions

Bu planı onaylıyor musunuz? Hangi öncelik seviyesinden başlayalım?