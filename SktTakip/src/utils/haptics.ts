import {Vibration, Platform} from 'react-native';

const IMPACT_MS = 10;

let enabled = true;

export const setHapticsEnabled = (value: boolean): void => {
  enabled = value;
};

export const impactLight = (): void => {
  if (enabled && Platform.OS === 'android') {
    Vibration.vibrate(IMPACT_MS);
  }
};

export const notificationWarning = (): void => {
  if (enabled && Platform.OS === 'android') {
    Vibration.vibrate([0, 20, 60, 20]);
  }
};
