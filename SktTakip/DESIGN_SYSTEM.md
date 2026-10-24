# Design System v2.0 - Expira App

## Overview
Target design system specification for the Expira app redesign, planned for v1.2.0 and beyond.

> **Status note:** This is a forward-looking spec and does not fully match the currently shipped UI. The implemented design lives in `src/theme.js` (light/dark palettes, `TYPOGRAPHY`, `FONT_WEIGHTS`, `SHADOWS`), `src/hooks/useResponsive.js` (4px base spacing grid, scaling helpers) and the shared components in `src/components/`. Current key differences:
>
> - **Colors:** The app ships a deep-green palette (light primary `#267968`, dark primary `#70CDB0`, accent `#C4D639`), not the teal `#14B8A6` ramp below. The semantic status colors (success/warning/danger/info) are also defined per light/dark mode in `src/theme.js`.
> - **Fonts:** The app uses the system default sans-serif; no custom font (Inter) is loaded.
> - **Spacing:** The app scales a 4px base grid responsively (`useResponsive().scaledSpacing`), not a fixed 8px grid.
>
> Sections below describe the v2.0 design direction; treat `src/theme.js` as the source of truth for the current build.

---

## 1. Color Palette

### Primary Colors (v2.0 target — see note above)
| Name | Hex | Usage | Accessibility |
|------|-----|-------|---------------|
| Primary 500 | #14B8A6 | Main actions, buttons, accents | AA (4.5:1 white) |
| Primary 600 | #0D9488 | Primary buttons (pressed) | AA (6.2:1 white) |
| Primary 700 | #0F766E | Dark accents | AA (7.2:1 white) |
| Primary 400 | #2DD4BF | Light accents | AA (3.8:1 black) |
| Primary 300 | #5EEAD4 | Backgrounds, highlights | AA (2.5:1 black) |

### Secondary Colors
| Name | Hex | Usage |
|------|-----|-------|
| Secondary 500 | #84CC16 | Success states, confirmations |
| Secondary 600 | #65A30D | Success buttons |
| Secondary 700 | #4D7C0F | Dark success accents |

### Semantic Colors
| Name | Hex | Usage |
|------|-----|-------|
| Success | #10B981 | Success messages, checkmarks |
| Warning | #F59E0B | Warning messages, alerts |
| Error | #EF4444 | Error messages, danger actions |
| Info | #3B82F6 | Information messages |

### Neutral Colors
| Name | Hex | Usage |
|------|-----|-------|
| Neutral 950 | #030712 | Primary text (dark mode) |
| Neutral 900 | #111827 | Secondary text (dark mode) |
| Neutral 800 | #1F2937 | Tertiary text (dark mode) |
| Neutral 700 | #374151 | Disabled text, placeholders |
| Neutral 600 | #4B5563 | Subtle text |
| Neutral 500 | #6B7280 | Borders, dividers |
| Neutral 400 | #9CA3AF | Disabled icons |
| Neutral 300 | #D1D5DB | Backgrounds (dark mode) |
| Neutral 200 | #E5E7EB | Borders, surfaces |
| Neutral 100 | #F3F4F6 | Elevated surfaces |
| Neutral 50 | #F9FAFB | Primary backgrounds |

### Background Colors
| Mode | Primary | Secondary | Tertiary |
|------|---------|-----------|---------|
| Light | #F9FAFB | #FFFFFF | #F3F4F6 |
| Dark | #030712 | #111827 | #1F2937 |

### Surface Colors
| Mode | Elevated | Card | Overlay |
|------|----------|------|--------|
| Light | #FFFFFF | #FFFFFF | rgba(0,0,0,0.5) |
| Dark | #1F2937 | #374151 | rgba(0,0,0,0.7) |

---

## 2. Typography

### Font Family (v2.0 target — not yet implemented)
- **Current**: System default sans-serif (no custom font loaded)
- **Planned**: Inter (via `@expo-google-fonts/inter`) with system fallback

### Font Weights
| Name | Weight | Usage |
|------|--------|-------|
| Light | 300 | Large display text |
| Regular | 400 | Body text, default |
| Medium | 500 | Emphasis, buttons |
| SemiBold | 600 | Headings, labels |
| Bold | 700 | Titles, important text |

### Font Sizes (8px Grid System)
| Name | Size | Line Height | Letter Spacing | Usage |
|------|------|-------------|----------------|-------|
| xs | 12px | 16px (1.33) | 0.02em | Captions, helper text |
| sm | 14px | 20px (1.43) | 0.01em | Body text, secondary info |
| base | 16px | 24px (1.5) | 0 | Primary body text |
| lg | 18px | 28px (1.56) | -0.01em | Subheadings |
| xl | 20px | 28px (1.4) | -0.01em | Headings |
| 2xl | 24px | 32px (1.33) | -0.02em | Section titles |
| 3xl | 30px | 36px (1.2) | -0.02em | Page titles |
| 4xl | 36px | 40px (1.11) | -0.02em | Modal titles |
| 5xl | 48px | 56px (1.17) | -0.03em | Hero text |

### Typography Styles

#### Headings
```css
.h1 { font-size: 36px; font-weight: 700; line-height: 40px; letter-spacing: -0.02em; }
.h2 { font-size: 30px; font-weight: 700; line-height: 36px; letter-spacing: -0.02em; }
.h3 { font-size: 24px; font-weight: 600; line-height: 32px; letter-spacing: -0.01em; }
.h4 { font-size: 20px; font-weight: 600; line-height: 28px; letter-spacing: -0.01em; }
.h5 { font-size: 18px; font-weight: 600; line-height: 28px; }
.h6 { font-size: 16px; font-weight: 600; line-height: 24px; }
```

#### Body
```css
.body-lg { font-size: 18px; font-weight: 400; line-height: 28px; }
.body { font-size: 16px; font-weight: 400; line-height: 24px; }
.body-sm { font-size: 14px; font-weight: 400; line-height: 20px; }
.body-xs { font-size: 12px; font-weight: 400; line-height: 16px; }
```

#### Buttons
```css
.btn-lg { font-size: 16px; font-weight: 600; line-height: 24px; }
.btn { font-size: 14px; font-weight: 500; line-height: 20px; }
.btn-sm { font-size: 12px; font-weight: 500; line-height: 16px; }
```

---

## 3. Spacing System (8px Grid)

| Name | Value | Usage |
|------|-------|-------|
| spacing-xs | 8px | Small gaps, icon padding |
| spacing-sm | 16px | Default padding, small margins |
| spacing-md | 24px | Card padding, medium gaps |
| spacing-lg | 32px | Section spacing, large padding |
| spacing-xl | 40px | Section margins |
| spacing-2xl | 48px | Large sections |
| spacing-3xl | 56px | Extra large gaps |
| spacing-4xl | 64px | Page margins |

### Spacing Utilities
```javascript
// Usage in StyleSheet
const styles = StyleSheet.create({
  p-xs: { padding: 8 },
  p-sm: { padding: 16 },
  p-md: { padding: 24 },
  p-lg: { padding: 32 },
  m-xs: { margin: 8 },
  m-sm: { margin: 16 },
  m-md: { margin: 24 },
  m-lg: { margin: 32 },
  gap-xs: { gap: 8 },
  gap-sm: { gap: 16 },
  gap-md: { gap: 24 },
  gap-lg: { gap: 32 },
});
```

---

## 4. Elevation & Shadows

### Light Mode Shadows
| Level | Offset | Radius | Opacity | Usage |
|-------|--------|--------|---------|-------|
| shadow-sm | 0,1px | 2px | 0.05 | Cards, subtle depth |
| shadow | 0,1px | 3px | 0.1 | Elevated cards |
| shadow-md | 0,4px | 6px | 0.1 | Modal, dropdown |
| shadow-lg | 0,10px | 15px | 0.1 | Floating elements |
| shadow-xl | 0,20px | 25px | 0.1 | Tooltips, popovers |

### Dark Mode Shadows
| Level | Offset | Radius | Color | Usage |
|-------|--------|--------|-------|-------|
| shadow-sm | 0,1px | 2px | rgba(0,0,0,0.3) | Cards |
| shadow | 0,1px | 3px | rgba(0,0,0,0.4) | Elevated cards |
| shadow-md | 0,4px | 6px | rgba(0,0,0,0.5) | Modal |

---

## 5. Border Radius

| Name | Value | Usage |
|------|-------|-------|
| rounded-none | 0px | No rounding |
| rounded-sm | 4px | Small elements, inputs |
| rounded | 6px | Default rounding |
| rounded-md | 8px | Cards, buttons |
| rounded-lg | 12px | Large cards, modals |
| rounded-xl | 16px | Floating elements |
| rounded-2xl | 24px | Hero elements |
| rounded-full | 9999px | Pills, avatars |

---

## 6. Component Library

### 6.1 Buttons

#### Primary Button
```javascript
<Button
  variant="primary"
  size="md"
  onPress={() => {}}
  disabled={false}
  loading={false}
>
  Button Text
</Button>
```

**Props:**
- `variant`: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
- `size`: 'sm' | 'md' | 'lg'
- `disabled`: boolean
- `loading`: boolean
- `icon`: ReactNode (left icon)
- `rightIcon`: ReactNode (right icon)
- `fullWidth`: boolean

**Styles:**
```javascript
// Primary
backgroundColor: colors.primary500,
color: colors.neutral50,
// Primary:pressed
backgroundColor: colors.primary600,
// Primary:disabled
backgroundColor: colors.primary300,
color: colors.neutral50,
opacity: 0.5,

// Secondary
backgroundColor: colors.neutral100,
color: colors.neutral900,

// Outline
borderWidth: 1,
borderColor: colors.primary500,
color: colors.primary500,

// Ghost
color: colors.primary500,

// Danger
backgroundColor: colors.error,
color: colors.neutral50,
```

#### Button Sizes
| Size | Height | Padding | Font Size |
|------|--------|---------|-----------|
| sm | 36px | 12px 20px | 14px |
| md | 44px | 16px 24px | 16px |
| lg | 52px | 20px 32px | 18px |

### 6.2 Cards

#### Product Card
```javascript
<ProductCard
  product={product}
  onPress={() => {}}
  onEdit={() => {}}
  onDelete={() => {}}
  showExpiryWarning={true}
/>
```

**Styles:**
```javascript
// Card Container
backgroundColor: colors.surface,
borderRadius: 12,
shadow: shadows.sm,
padding: spacing.md,
marginBottom: spacing.md,

// Card:pressed
opacity: 0.9,
scale: 0.98,

// Card Image
width: 64,
height: 64,
borderRadius: 8,

// Card Content
flex: 1,
marginLeft: spacing.sm,

// Card Actions
flexDirection: 'row',
gap: spacing-xs,
```

### 6.3 Inputs

#### Text Input
```javascript
<TextInput
  placeholder="Enter product name"
  value={value}
  onChangeText={setValue}
  error={error}
  label="Product Name"
  helperText="Enter the name of your product"
/>
```

**Styles:**
```javascript
// Input Container
marginBottom: spacing.sm,

// Input Label
fontSize: 14,
fontWeight: 500,
color: colors.neutral700,
marginBottom: spacing.xs,

// Input Field
height: 44,
paddingHorizontal: spacing.sm,
borderWidth: 1,
borderColor: colors.neutral300,
borderRadius: 8,
backgroundColor: colors.surface,
color: colors.neutral900,
fontSize: 16,

// Input:focus
borderColor: colors.primary500,
shadow: shadows.sm,

// Input:error
borderColor: colors.error,

// Input:disabled
backgroundColor: colors.neutral100,
color: colors.neutral500,
```

### 6.4 Lists

#### Product List
```javascript
<ProductList
  products={products}
  onItemPress={(item) => {}}
  onRefresh={() => {}}
  refreshing={refreshing}
  ListEmptyComponent={<EmptyState />}
/>
```

**Styles:**
```javascript
// List Container
flex: 1,

// List Item
paddingHorizontal: spacing.md,
paddingVertical: spacing.sm,
borderBottomWidth: 1,
borderBottomColor: colors.neutral200,

// List Item:last
borderBottomWidth: 0,

// List Item:pressed
backgroundColor: colors.neutral100,

// Section Header
backgroundColor: colors.surface,
padding: spacing.sm spacing.md,
borderBottomWidth: 1,
borderBottomColor: colors.neutral200,
```

### 6.5 Badges

#### Status Badge
```javascript
<Badge
  variant="success" | "warning" | "error" | "info" | "neutral"
  size="sm" | "md"
>
  Badge Text
</Badge>
```

**Styles:**
```javascript
// Success Badge
backgroundColor: colors.success100,
color: colors.success700,

// Warning Badge
backgroundColor: colors.warning100,
color: colors.warning700,

// Error Badge
backgroundColor: colors.error100,
color: colors.error700,

// Info Badge
backgroundColor: colors.info100,
color: colors.info700,

// Neutral Badge
backgroundColor: colors.neutral100,
color: colors.neutral700,

// Badge Sizes
paddingHorizontal: 8 (sm) | 12 (md),
paddingVertical: 4 (sm) | 6 (md),
borderRadius: 4 (sm) | 6 (md),
fontSize: 12 (sm) | 14 (md),
fontWeight: 500,
```

### 6.6 Modals

#### Bottom Sheet Modal
```javascript
<BottomSheetModal
  visible={visible}
  onDismiss={() => {}}
  title="Modal Title"
  subtitle="Modal description"
>
  <ModalContent />
</BottomSheetModal>
```

**Styles:**
```javascript
// Modal Overlay
position: 'absolute',
top: 0,
left: 0,
right: 0,
bottom: 0,
backgroundColor: 'rgba(0,0,0,0.5)',

// Modal Container
position: 'absolute',
left: 0,
right: 0,
bottom: 0,
backgroundColor: colors.surface,
borderTopLeftRadius: 16,
borderTopRightRadius: 16,
shadow: shadows.lg,

// Modal Handle
width: 40,
height: 4,
backgroundColor: colors.neutral300,
borderRadius: 2,
marginTop: spacing.sm,
alignSelf: 'center',

// Modal Header
padding: spacing.md,
borderBottomWidth: 1,
borderBottomColor: colors.neutral200,

// Modal Title
fontSize: 20,
fontWeight: 600,
color: colors.neutral900,

// Modal Body
padding: spacing.md,

// Modal Footer
padding: spacing.md,
borderTopWidth: 1,
borderTopColor: colors.neutral200,
flexDirection: 'row',
justifyContent: 'flex-end',
gap: spacing.sm,
```

### 6.7 Alerts & Toasts

#### Toast Notification
```javascript
<Toast
  variant="success" | "warning" | "error" | "info"
  message="Toast message"
  duration={3000}
  onDismiss={() => {}}
/>
```

**Styles:**
```javascript
// Toast Container
position: 'absolute',
bottom: spacing.lg,
left: spacing.md,
right: spacing.md,
padding: spacing.sm spacing.md,
borderRadius: 8,
shadow: shadows.md,
maxWidth: 300,
alignSelf: 'center',

// Success Toast
backgroundColor: colors.success500,
color: colors.neutral50,

// Warning Toast
backgroundColor: colors.warning500,
color: colors.neutral900,

// Error Toast
backgroundColor: colors.error500,
color: colors.neutral50,

// Info Toast
backgroundColor: colors.info500,
color: colors.neutral50,
```

---

## 7. Accessibility

### Color Contrast Requirements
- All text must meet WCAG 2.1 AA (4.5:1 for normal text, 3:1 for large text)
- Interactive elements must have minimum 44x44px touch targets
- Focus indicators must be visible (2px minimum)

### Accessibility Features
- Screen reader support via accessibility labels
- Reduced motion support
- Dynamic type support
- High contrast mode support

---

## 8. Animation & Micro-interactions

### Transitions
| Name | Duration | Easing | Usage |
|------|----------|--------|-------|
| fast | 150ms | ease-out | Button presses, toggles |
| normal | 250ms | ease-in-out | Modal opens, dropdowns |
| slow | 350ms | ease-in-out | Page transitions |
| spring | 500ms | spring(0.9, 100) | Bouncy interactions |

### Hover States
```javascript
// Button hover
transform: [{ scale: 1.02 }],
shadow: shadows.md,

// Card hover
shadow: shadows.md,
transform: [{ translateY: -2 }],
```

### Press States
```javascript
// Button press
opacity: 0.9,
transform: [{ scale: 0.98 }],

// Card press
opacity: 0.9,
transform: [{ scale: 0.98 }],
```

---

## 9. Implementation Guidelines

### Theme Integration
```javascript
// theme.js
import { Platform } from 'react-native';

export const colors = {
  light: {
    primary500: '#14B8A6',
    primary600: '#0D9488',
    neutral950: '#030712',
    neutral50: '#F9FAFB',
    surface: '#FFFFFF',
    // ... other colors
  },
  dark: {
    primary500: '#2DD4BF',
    primary600: '#22C5B4',
    neutral950: '#F9FAFB',
    neutral50: '#030712',
    surface: '#1F2937',
    // ... other colors
  },
};

export const spacing = {
  xs: 8,
  sm: 16,
  md: 24,
  lg: 32,
  xl: 40,
  '2xl': 48,
  '3xl': 56,
  '4xl': 64,
};

export const typography = {
  fontFamily: Platform.OS === 'ios' ? 'Inter' : 'Inter-Regular',
  fontWeights: {
    light: '300',
    regular: '400',
    medium: '500',
    semiBold: '600',
    bold: '700',
  },
  // ... font sizes
};
```

### Using the Design System
```javascript
import { colors, spacing, typography } from './theme';

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surface,
    padding: spacing.md,
  },
  title: {
    fontFamily: typography.fontFamily,
    fontWeight: typography.fontWeights.bold,
    fontSize: typography.fontSizes['3xl'],
    color: colors.neutral900,
  },
  button: {
    backgroundColor: colors.primary500,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: spacing.sm,
  },
});
```

---

## 10. Migration Guide

### From v1.1.x to v2.0.0

1. **Update theme.js**
   - Replace existing colors with new palette
   - Add spacing system
   - Add typography system

2. **Update components**
   - Replace hardcoded colors with theme colors
   - Replace hardcoded spacing with spacing system
   - Update typography to use new font sizes and weights

3. **Update screens**
   - Apply new component library
   - Update layouts to use new spacing
   - Ensure accessibility compliance

4. **Test**
   - Verify all colors render correctly
   - Verify spacing is consistent
   - Verify typography is legible
   - Test accessibility features

---

## 11. Resources

- [Inter Font](https://github.com/rsms/inter)
- [Expo Google Fonts](https://github.com/expo/google-fonts)
- [React Native StyleSheet](https://reactnative.dev/docs/stylesheet)
- [WCAG Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)

---

*Last Updated: October 2026*
*Version: 2.0.0*
---

## Implementation status (current build)

The shipped UI predates this spec. Current implementation notes for developers:

- `src/theme.js` exports the live palettes (`LIGHT_COLORS`, `DARK_COLORS`), `TYPOGRAPHY`, `FONT_WEIGHTS`, `SHADOWS` (`soft`/`medium`/`heavy`) and `CATEGORIES`
- Components must import `SHADOWS` (and palettes) from `src/theme.js` — never from `useResponsive()`. This contract is enforced by regression tests in `__tests__/themeShadows.test.js` after a startup crash caused by destructuring `SHADOWS` from the responsive hook
- Spacing and scaling come from `useResponsive()` (4px base grid, responsive scaling)
