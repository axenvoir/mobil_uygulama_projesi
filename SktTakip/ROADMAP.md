# Expira App - Product Update Roadmap

## Overview
This document outlines the comprehensive update plan for Expira app from v1.1.3 to v2.0.0, including feature development, technical improvements, and design system implementation.

---

## Current State (v1.1.3)

### Completed Fixes
- [x] Google Play Console version code conflict resolved (versionCode: 1 → 30)
- [x] ProGuard/R8 mapping file configuration added (`ANDROID_PROGUARD_MAPPING: "true"`)
- [x] POST_NOTIFICATIONS permission verified correct in `app.json`
- [x] GitHub Actions dependencies updated (setup-node@v4→v5, setup-java@v4→v5, Node.js 20→22)
- [x] All Turkish comments removed from source code
- [x] All double `__DEV__` checks fixed to single
- [x] All console statements properly wrapped with `if (__DEV__)`
- [x] Version aligned to 1.1.3 across `app.json`, `package.json`, `package-lock.json` and release docs
- [x] System (device) language is now the default; the app re-resolves locale on foreground return
- [x] Accessibility: `maxFontSizeMultiplier`, screen-reader labels/states, WCAG AA tab contrast
- [x] deleteProduct/deleteNote state-updater race condition fixed (undo toast now reliable)
- [x] LoadingScreen Animated.loop leak fixed (animations stopped on unmount)
- [x] Daily reminder and test notification content localized (TR/EN)
- [x] Calendar permission cache given a 5-minute TTL (revocation detected)
- [x] 40+ hardcoded UI strings migrated to i18n (TR/EN parity enforced by tests)
- [x] Backup restore verifies checksums; corrupted files rejected
- [x] Bulk category change batched into a single state update (`updateProducts`)
- [x] Jest test infrastructure added (59 tests: validators, formatters, translation parity)
- [x] `Tests` CI workflow added (lint + jest on PRs and on `main`)

### Known Issues
- [ ] Design system v2.0 not yet implemented (`src/theme.js` remains the source of truth for the shipped UI)
- [ ] v1.2.0 features not yet developed
- [ ] Component/integration tests not yet covered (current suites are pure-logic only)


---

## Version Planning

### v1.1.3 (Current - October 2026)
**Status:** Released to Google Play Console for review

**Changes:**
- Critical bug fixes for Play Store submission
- Code cleanup and optimization
- Turkish comments removed
- Double `__DEV__` checks fixed

**Release Date:** October 2026

---

### v1.2.0 (Next Major Update)
**Status:** Planning Phase
**Target Release:** November 2026
**Priority:** High

#### Features

##### 1. Barcode Scanning (Barkod Tarama)
- **Priority:** P0 (Critical)
- **Description:** Integrate barcode scanner for quick product addition
- **Technical Implementation:**
  - Use `expo-barcode-scanner` library
  - Add barcode scanning screen
  - Integrate with AddProductScreen
  - Support EAN-8, EAN-13, UPC-A, UPC-E, Code 39, Code 93, Code 128
  - Auto-fill product details from barcode lookup API
- **Dependencies:**
  - `expo-barcode-scanner`
  - Optional: Barcode lookup API integration
- **Estimated Effort:** 3-5 days
- **Testing:** Required on both Android and iOS

##### 2. Smart Expiry Date Prediction (Akıllı SKT Tahmini)
- **Priority:** P0 (Critical)
- **Description:** AI-powered expiry date prediction based on product type and purchase date
- **Technical Implementation:**
  - Create prediction algorithm based on category averages
  - Add ML model for improved predictions (optional)
  - Store prediction confidence levels
  - Allow manual override
- **Data Requirements:**
  - Category-based expiry averages
  - User correction feedback loop
- **Estimated Effort:** 5-7 days
- **Testing:** Unit tests for prediction logic

##### 3. Bulk Import/Export (Toplu İthalat/İhracat)
- **Priority:** P1 (High)
- **Description:** Import/export products in bulk via CSV/JSON
- **Technical Implementation:**
  - Add import/export functionality
  - Support CSV and JSON formats
  - Add file picker for import
  - Add share functionality for export
  - Validate data before import
- **Dependencies:**
  - `expo-file-system`
  - `expo-sharing`
  - CSV parsing library
- **Estimated Effort:** 4-6 days
- **Testing:** File format validation, data integrity

##### 4. Widget Support (Widget Desteği)
- **Priority:** P1 (High)
- **Description:** Add home screen widgets for quick access
- **Technical Implementation:**
  - Android: Use App Widget framework
  - iOS: Use WidgetKit (Expo support required)
  - Display upcoming expiry dates
  - Quick add button
  - Configuration options
- **Platform Support:**
  - Android: Full support
  - iOS: Limited by Expo capabilities
- **Estimated Effort:** 5-8 days
- **Testing:** Platform-specific testing required

##### 5. Turkish-English Bilingual Support
- **Priority:** P2 (Medium)
- **Description:** Full bilingual support for Turkish and English
- **Technical Implementation:**
  - Complete i18n implementation
  - Language switcher in settings
  - RTL support (if needed)
  - Localize all strings
- **Dependencies:**
  - `i18n-js` or similar
- **Estimated Effort:** 3-5 days
- **Testing:** Verify all strings in both languages

#### Technical Improvements

##### 1. Design System Implementation
- **Priority:** P0 (Critical)
- **Description:** Implement the complete design system from DESIGN_SYSTEM.md
- **Tasks:**
  - Create theme/colors.js with full color palette
  - Create theme/typography.js with font system
  - Create theme/spacing.js with spacing system
  - Update all components to use new theme
  - Update all screens to use new design system
- **Estimated Effort:** 7-10 days
- **Dependencies:** None (uses existing libraries)

##### 2. Performance Optimization
- **Priority:** P1 (High)
- **Description:** Improve app performance and responsiveness
- **Tasks:**
  - Optimize FlatList rendering
  - Implement memoization where needed
  - Reduce unnecessary re-renders
  - Optimize image loading
  - Implement lazy loading
- **Estimated Effort:** 3-5 days
- **Testing:** Performance benchmarks

##### 3. Accessibility Improvements
- **Priority:** P1 (High)
- **Description:** Ensure WCAG 2.1 AA compliance
- **Tasks:**
  - Add proper accessibility labels
  - Ensure color contrast ratios
  - Add screen reader support
  - Implement reduced motion support
  - Add focus management
- **Estimated Effort:** 2-3 days
- **Testing:** Accessibility audit

#### Bug Fixes
- Fix POST_NOTIFICATIONS permission display issue
- Verify all Play Store requirements
- Test on Android 13+ devices

---

### v1.3.0 (Feature Expansion)
**Status:** Planning Phase
**Target Release:** December 2026
**Priority:** Medium

#### Features

##### 1. AI Product Recognition (AI Ürün Tanıma)
- **Priority:** P0 (Critical)
- **Description:** Use ML to recognize products from images
- **Technical Implementation:**
  - Integrate with TensorFlow.js or ML Kit
  - Add camera-based product recognition
  - Match against product database
  - Auto-fill product details
- **Dependencies:**
  - `expo-camera`
  - ML model (custom or pre-trained)
- **Estimated Effort:** 7-10 days
- **Testing:** Image recognition accuracy

##### 2. Family Sharing (Aile Paylaşımı)
- **Priority:** P1 (High)
- **Description:** Allow families to share product data
- **Technical Implementation:**
  - Add family group creation
  - Implement shared data synchronization
  - Add permission system
  - Handle conflict resolution
- **Dependencies:**
  - Firebase or custom backend
  - Real-time sync
- **Estimated Effort:** 8-12 days
- **Testing:** Multi-device synchronization

##### 3. Advanced Statistics
- **Priority:** P2 (Medium)
- **Description:** Enhanced analytics and insights
- **Features:**
  - Expiry trends analysis
  - Consumption patterns
  - Waste reduction metrics
  - Exportable reports
- **Estimated Effort:** 4-6 days

##### 4. Cloud Backup & Sync
- **Priority:** P2 (Medium)
- **Description:** Automatic cloud backup and cross-device sync
- **Technical Implementation:**
  - Integrate with Firebase or custom backend
  - Implement auto-backup
  - Add conflict resolution
  - Offline-first approach
- **Estimated Effort:** 5-8 days

---

### v2.0.0 (Major Rewrite)
**Status:** Planning Phase
**Target Release:** Q1 2027
**Priority:** High

#### Major Changes

##### 1. React Native 0.76+ Upgrade
- **Priority:** P0 (Critical)
- **Description:** Upgrade to latest React Native version
- **Tasks:**
  - Upgrade React Native core
  - Update all dependencies
  - Fix compatibility issues
  - Test thoroughly
- **Estimated Effort:** 5-7 days
- **Risk:** High (breaking changes possible)

##### 2. Expo SDK 51+ Upgrade
- **Priority:** P0 (Critical)
- **Description:** Upgrade Expo SDK
- **Tasks:**
  - Update Expo SDK version
  - Update expo modules
  - Test all native features
- **Estimated Effort:** 3-5 days
- **Risk:** Medium

##### 3. TypeScript Migration
- **Priority:** P0 (Critical)
- **Description:** Migrate entire codebase to TypeScript
- **Tasks:**
  - Add TypeScript configuration
  - Convert all .js files to .tsx
  - Add proper type definitions
  - Implement type checking
- **Estimated Effort:** 10-15 days
- **Benefits:** Better code quality, IDE support

##### 4. New Architecture
- **Priority:** P1 (High)
- **Description:** Implement modern React Native architecture
- **Tasks:**
  - Feature-based module structure
  - Clean separation of concerns
  - Improved state management
  - Better error handling
- **Estimated Effort:** 7-10 days

##### 5. New UI/UX Design
- **Priority:** P1 (High)
- **Description:** Complete visual redesign
- **Tasks:**
  - Implement new design system
  - Redesign all screens
  - Add animations and transitions
  - Improve user experience
- **Estimated Effort:** 10-15 days

##### 6. Advanced Features
- Multi-user collaboration
- Smart notifications
- Voice commands
- AR product scanning
- Integration with smart home devices

---

## Implementation Timeline

### Phase 1: v1.2.0 Development (October - November 2026)
| Week | Task | Status |
|------|------|--------|
| Week 1 | Design System Implementation | Not Started |
| Week 2 | Barcode Scanning Feature | Not Started |
| Week 3 | Smart Expiry Prediction | Not Started |
| Week 4 | Bulk Import/Export | Not Started |
| Week 5 | Widget Support | Not Started |
| Week 6 | Testing & Bug Fixes | Not Started |
| Week 7 | Beta Testing | Not Started |
| Week 8 | Release to Play Store | Not Started |

### Phase 2: v1.3.0 Development (December 2026 - January 2027)
| Week | Task | Status |
|------|------|--------|
| Week 1-2 | AI Product Recognition | Not Started |
| Week 3-4 | Family Sharing | Not Started |
| Week 5 | Advanced Statistics | Not Started |
| Week 6 | Cloud Backup | Not Started |
| Week 7 | Testing & Bug Fixes | Not Started |
| Week 8 | Release to Play Store | Not Started |

### Phase 3: v2.0.0 Development (Q1 2027)
| Month | Task | Status |
|-------|------|--------|
| January | TypeScript Migration | Not Started |
| February | React Native & Expo Upgrade | Not Started |
| March | Architecture & UI/UX Redesign | Not Started |
| April | Advanced Features | Not Started |
| May | Testing & Release | Not Started |

---

## Technical Specifications

### Development Environment
- **React Native:** 0.75.x (current) → 0.76+ (v2.0.0)
- **Expo SDK:** 50.x (current) → 51+ (v2.0.0)
- **Node.js:** 22.x (current)
- **Java:** 17.x (current)
- **Build Tool:** EAS Build

### Dependencies
- `expo-barcode-scanner` (v1.2.0)
- `expo-file-system` (v1.2.0)
- `expo-sharing` (v1.2.0)
- `expo-camera` (v1.3.0)
- `expo-notifications` (already installed)
- `expo-local-authentication` (already installed)
- `expo-calendar` (already installed)

### Testing Strategy
- Unit tests for utility functions
- Component tests for UI elements
- Integration tests for features
- E2E tests for user flows
- Manual testing on Android and iOS

---

## Quality Assurance

### Code Quality
- TypeScript for type safety (v2.0.0)
- ESLint for code linting
- Prettier for code formatting
- Git hooks for pre-commit checks

### Performance
- Target: 60 FPS on all screens
- App startup time: < 2 seconds
- Memory usage: < 100MB
- Battery impact: Minimal

### Accessibility
- WCAG 2.1 AA compliance
- Screen reader support
- Dynamic type support
- Reduced motion support
- High contrast mode support

---

## Release Process

### Beta Testing
1. Internal testing by development team
2. Closed beta with selected users
3. Open beta on Play Store
4. Collect feedback and fix issues

### Production Release
1. Final testing and QA
2. Prepare release notes
3. Submit to Google Play Console
4. Monitor for issues
5. Release to 100% of users

---

## Success Metrics

### v1.2.0 Goals
- [ ] 90% of users can successfully scan barcodes
- [ ] 80% accuracy in smart expiry prediction
- [ ] 95% of users successfully import/export data
- [ ] Widgets functional on 90% of supported devices
- [ ] App Store rating: 4.5+

### v1.3.0 Goals
- [ ] 85% accuracy in AI product recognition
- [ ] 70% of users use family sharing feature
- [ ] 90% user satisfaction with statistics
- [ ] 80% of users enable cloud backup

### v2.0.0 Goals
- [ ] 100% TypeScript coverage
- [ ] 50% performance improvement
- [ ] 95% crash-free sessions
- [ ] App Store rating: 4.7+

---

## Risk Assessment

### High Risk Items
1. **POST_NOTIFICATIONS permission:** May cause Play Store rejection if not fixed
2. **TypeScript Migration:** May introduce breaking changes
3. **React Native Upgrade:** May cause compatibility issues
4. **Multi-device Sync:** Complex synchronization logic

### Mitigation Strategies
1. Thorough testing before release
2. Incremental migration approach
3. Comprehensive backup strategy
4. Rollback plans for each release

---

## Team & Resources

### Development Team
- **Primary Developer:** axenvoir
- **Contributors:** Open for community contributions
- **Testers:** Internal team + beta testers

### Resources
- **Documentation:** This file, DESIGN_SYSTEM.md
- **Project Board:** GitHub Projects
- **Communication:** GitHub Discussions
- **CI/CD:** GitHub Actions

---

## Next Steps

1. **Immediate (This Week):**
   - [ ] Investigate and fix POST_NOTIFICATIONS permission issue
   - [ ] Verify v1.1.3 submission to Play Store
   - [ ] Begin design system implementation

2. **Short Term (Next 2 Weeks):**
   - [ ] Complete design system files (colors.js, typography.js, spacing.js)
   - [ ] Update existing components to use new theme
   - [ ] Begin barcode scanning feature development

3. **Medium Term (Next Month):**
   - [ ] Complete v1.2.0 feature development
   - [ ] Conduct beta testing
   - [ ] Prepare for Play Store release

---

## Changelog

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| Oct 2026 | v1.1.3 | Critical bug fixes, Play Store submission | axenvoir |
| TBD | v1.2.0 | Design system, barcode scanning, smart prediction | TBD |
| TBD | v1.3.0 | AI recognition, family sharing | TBD |
| TBD | v2.0.0 | TypeScript, React Native 0.76+, new architecture | TBD |

---

*Last Updated: October 2026*
*Document Version: 1.0.0*