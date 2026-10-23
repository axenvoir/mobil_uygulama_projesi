import { useMemo } from 'react';
import { useWindowDimensions } from 'react-native';

const BASE_WIDTH = 390;

export const useResponsive = () => {
  const { width, height, fontScale } = useWindowDimensions();

  return useMemo(() => {
    const isSmall = width < 360;
    const isTablet = width >= 768;
    const isWideScreen = width >= 1200;
    const isLandscape = width > height;
    const isCompactHeight = height < 480;
    const widthScale = Math.min(Math.max(Math.min(width, 600) / BASE_WIDTH, 0.88), 1.12);
    const heightScale = Math.min(Math.max(height / 812, 0.85), 1.1);

    const scale = (size) => widthScale * size;
    const verticalScale = (size) => heightScale * size;
    const moderateScale = (size, factor = 0.5) => size + (scale(size) - size) * factor;
    const fontSize = (base) => Math.max(14, Math.round(moderateScale(base, 0.35)));
    const icon = (base) => Math.max(18, Math.round(moderateScale(base, 0.4)));
    const spacing = (base) => Math.round(moderateScale(base, 0.5));
    const contentMaxWidth = isWideScreen ? 1080 : isTablet ? 840 : width;
    const itemColumns = width >= 1200 ? 3 : isTablet ? 2 : 1;

    // ============================================
    // SPACING HELPERS (4px base grid)
    // ============================================
    const spacingMap = {
      none: 0,
      xs: 4,
      sm: 8,
      md: 16,
      lg: 24,
      xl: 32,
      '2xl': 48,
      '3xl': 64
    };

    const getSpacing = (key) => spacingMap[key] || spacingMap.md;
    
    // Pre-scaled spacing values for common use cases
    const scaledSpacing = {
      xs: scale(4),
      sm: scale(8),
      md: scale(16),
      lg: scale(24),
      xl: scale(32),
      '2xl': scale(48),
      '3xl': scale(64)
    };

    return {
      width,
      height,
      fontScale,
      isSmall,
      isTablet,
      isWideScreen,
      isLandscape,
      isCompactHeight,
      scale,
      verticalScale,
      moderateScale,
      fontSize,
      icon,
      spacing,
      getSpacing,
      scaledSpacing,
      spacingMap,
      horizontalPadding: isWideScreen ? scale(40) : isTablet ? scale(32) : isSmall ? scale(16) : scale(20),
      contentMaxWidth,
      itemColumns,
      itemGap: isTablet ? scale(18) : scale(12),
      cardPadding: isTablet ? scale(24) : scale(16),
      sectionSpacing: isTablet ? scale(32) : scale(24),
    };
  }, [width, height, fontScale]);
};