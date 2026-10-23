import * as Haptics from 'expo-haptics';
import { Platform } from 'react-native';

export const tapLight = async () => {
  if (Platform.OS === 'android') return;
  try {
    await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  } catch (error) {
    if (__DEV__) console.warn('Dokunsal geri bildirim kullanılamıyor:', error);
  }
};

export const tapMedium = async () => {
  try {
    await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
  } catch (error) {
    if (__DEV__) console.warn('Dokunsal geri bildirim kullanılamıyor:', error);
  }
};

export const notifySuccess = async () => {
  try {
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
  } catch (error) {
    if (__DEV__) console.warn('Dokunsal geri bildirim kullanılamıyor:', error);
  }
};

export const notifyError = async () => {
  try {
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
  } catch (error) {
    if (__DEV__) console.warn('Dokunsal geri bildirim kullanılamıyor:', error);
  }
};

export const notifyWarning = async () => {
  try {
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning);
  } catch (error) {
    if (__DEV__) console.warn('Dokunsal geri bildirim kullanılamıyor:', error);
  }
};