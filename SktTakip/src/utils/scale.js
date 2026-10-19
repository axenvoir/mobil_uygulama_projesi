import { Dimensions } from 'react-native';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const BASE_WIDTH = 375;

export const scale = (size) => (SCREEN_WIDTH / BASE_WIDTH) * size;
export const moderateScale = (size, factor = 0.5) =>
  size + (scale(size) - size) * factor;
export const verticalScale = (size) => (SCREEN_WIDTH / BASE_WIDTH) * size;