import { useMemo } from 'react';
import { useWindowDimensions } from 'react-native';

const BASE_WIDTH = 375;
const BASE_HEIGHT = 812;

export const useResponsive = () => {
  const { width, height } = useWindowDimensions();

  return useMemo(() => {
    const isSmall = width < 360;
    const isMedium = width >= 360 && width < 414;
    const isLarge = width >= 414;
    const isTablet = width >= 768;
    const isLandscape = width > height;
    const isCompactHeight = height < 480;
    const widthScale = Math.min(Math.max(width / BASE_WIDTH, 0.88), 1.12);
    const heightScale = Math.min(Math.max(height / BASE_HEIGHT, 0.85), 1.1);

    const scale = (size) => widthScale * size;
    const verticalScale = (size) => heightScale * size;
    const moderateScale = (size, factor = 0.5) => size + (scale(size) - size) * factor;
    const fontSize = (base) => Math.max(12, Math.round(moderateScale(base, 0.4)));
    const icon = (base) => Math.max(18, Math.round(moderateScale(base, 0.4)));
    const spacing = (base) => Math.round(moderateScale(base, 0.5));

    return {
      width,
      height,
      isSmall,
      isMedium,
      isLarge,
      isTablet,
      isLandscape,
      isCompactHeight,
      scale,
      verticalScale,
      moderateScale,
      fontSize,
      icon,
      spacing,
      horizontalPadding: isTablet ? scale(32) : isSmall ? scale(16) : scale(22),
      contentMaxWidth: isTablet ? Math.min(760, width) : width,
      itemColumns: isTablet ? 2 : 1,
      itemGap: isTablet ? scale(16) : scale(10),
      cardPadding: isTablet ? scale(20) : scale(16),
      sectionSpacing: isTablet ? scale(32) : scale(20),
    };
  }, [width, height]);
};