# Expira — UI Tasarım ve Kod Kalitesi Planı

## Mevcut Durum Analizi

### Tespit Edilen Sorunlar

1. **Unused import:** `ProductListScreen.kt` ve `NoteListScreen.kt`'de `EmptyState` import'u hâlâ mevcut (kullanılmıyor, `ExpiraEmptyState` kullanılıyor). Lint warning oluşturur.

2. **Dynamic color varsayılan açık:** `MainActivity.kt`'de `dynamicColor = true` varsayılan. Android 12+ cihazlarda Material You dinamik renk paleti kullanılır, bu da özel teal paletimizi ezerek uygulamanın farklı cihazlarda farklı görünmesine neden olur. Kullanıcı "aynı görünüyor" diyorsa muhtemelen dinamik renk devrede ve teal paleti görünmüyor.

3. **NoteListScreen FAB:** ExtendedFloatingActionButton kullanılıyor ama `modifier` ile konumlandırma eksik olabilir.

4. **ProductCard status accent bar:** `MaterialTheme.shapes.extraSmall` kullanılıyor (4dp radius) ama bar 4dp genişliğinde, radius gereksiz.

5. **SummaryCard elevation:** 2dp eklendi ama görsel fark minimal.

6. **FilterRow chips:** `FilterChip` kullanılıyor ama seçili durumda görsel feedback (renk değişimi) Material3 varsayılanlarına bırakılmış.

### Plan

#### Adım 1: Unused import temizliği
- `ProductListScreen.kt`: `import com.axenvoir.expira.ui.components.EmptyState` kaldır
- `NoteListScreen.kt`: `import com.axenvoir.expira.ui.components.EmptyState` kaldır

#### Adım 2: Dynamic color varsayılanını kapat
- `MainActivity.kt`: `dynamicColor = false` varsayılan (teal paleti her cihazda tutarlı)
- Settings ekranına "Dinamik renk" toggle ekle (kullanıcı isterse açabilir)

#### Adım 3: ProductCard görsel iyileştirme
- Status accent bar: `RoundedCornerShape(2.dp)` → sadece sol üst/alt köşeler rounded
- Card elevation: 2dp → 3dp (daha belirgin gölge)
- Card shape: `large` (16dp) → `medium` (12dp) (daha modern)
- Title font: `titleMedium` → `titleLarge` (daha büyük, daha okunabilir)
- Days remaining: `headlineSmall` → `headlineMedium` (daha prominent)

#### Adım 4: SummaryCard görsel iyileştirme
- Card elevation: 2dp → 0dp (flat, modern)
- Border ekle: 1dp outlineVariant
- SummaryItem: count font `headlineSmall` → `headlineMedium`
- Progress bar: 4dp → 6dp height

#### Adım 5: FilterRow görsel iyileştirme
- Seçili chip: primaryContainer background
- Leading icon: StatusDot yerine ExpiraStatusDot (tutarlı)
- Chip shape: full rounded (pill)

#### Adım 6: NoteCard görsel iyileştirme
- Elevation: 1dp → 2dp
- Pinned border: primary color, 2dp width
- Title: titleMedium → titleLarge

#### Adım 7: Empty state görsel iyileştirme
- Icon size: 72dp → 96dp
- Title: titleMedium → headlineSmall
- Subtitle: bodyMedium → bodyLarge

#### Adım 8: TopAppBar görsel iyileştirme
- LargeTopAppBar → MediumTopAppBar (daha kompakt)
- Title maxLines: 2 → 1

#### Adım 9: FAB görsel iyileştirme
- ExtendedFloatingActionButton → standard FloatingActionButton (daha temiz)
- Position: bottom-end, 16dp margin

#### Adım 10: Pull-to-refresh ekleme
- ProductListScreen: PullToRefreshBox ekle
- NoteListScreen: PullToRefreshBox ekle

#### Adım 11: Animasyon iyileştirme
- LazyColumn items: animateItem() + fadeIn + slideInVertically
- Stagger: min(index, 6) * 50ms

#### Adım 12: Dark mode kontrast düzeltme
- OnSurfaceVariant: #9CA3AF → #B0B8C4 (daha iyi kontrast)
- Outline: #2D3142 → #3D4455 (daha görünür border)

### Risk Değerlendirmesi
- Tüm değişiklikler mevcut yapıyı koruyarak görsel iyileştirme
- Build kırılma riski: Düşük (sadece styling değişiklikleri)
- Regression riski: Düşük (layout yapısı korunuyor)

### Uygulama Sırası
1. Adım 1-2 (import + dynamic color) — en kritik
2. Adım 3-5 (ProductList görsel) — en görünür
3. Adım 6-8 (NoteList + TopAppBar) — orta
4. Adım 9-11 (FAB + pull-to-refresh + animasyon) — UX
5. Adım 12 (Dark mode kontrast) — erişilebilirlik