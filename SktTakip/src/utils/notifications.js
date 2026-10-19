import { isRunningInExpoGo } from 'expo';
import { Platform } from 'react-native';

const isAndroidExpoGo = Platform.OS === 'android' && isRunningInExpoGo();
const Notifications = isAndroidExpoGo ? null : require('expo-notifications');

export const isNotificationsAvailable = !isAndroidExpoGo;

if (Notifications) {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowAlert: true,
      shouldPlaySound: true,
      shouldSetBadge: false,
    }),
  });
}

export const requestPermissions = async () => {
  if (!Notifications) return false;

  try {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }

    if (finalStatus !== 'granted') {
      return false;
    }

    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('default', {
        name: 'default',
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#14B8A6',
      });
    }

    return true;
  } catch (e) {
    console.error('Bildirim izni hatası:', e);
    return false;
  }
};

export const scheduleDailyReminder = async (hour, minute, customMessage = null) => {
  if (!Notifications) return null;

  try {
    await cancelAllReminders();

    const id = await Notifications.scheduleNotificationAsync({
      content: {
        title: '⏰ Expira Hatırlatması',
        body: customMessage || 'Buzdolabını kontrol etmeyi unutma! Son kullanma tarihi yaklaşan ürünlerin olabilir.',
        sound: true,
      },
      trigger: {
        hour,
        minute,
        repeats: true,
      },
    });
    return id;
  } catch (e) {
    console.error('Bildirim planlama hatası:', e);
    return null;
  }
};

export const cancelAllReminders = async () => {
  if (!Notifications) return;

  try {
    await Notifications.cancelAllScheduledNotificationsAsync();
  } catch (e) {
    console.error('Bildirim iptal hatası:', e);
  }
};

export const sendTestNotification = async (customMessage = null) => {
  if (!Notifications) return false;

  try {
    await Notifications.scheduleNotificationAsync({
      content: {
        title: '🎉 Expira Test Bildirimi',
        body: customMessage || 'Harika! Bildirimlerin çalışıyor. Artık ürünlerinin SKT\'sini takip edebilirsin.',
        sound: true,
      },
      trigger: null,
    });
    return true;
  } catch (e) {
    console.error('Test bildirim hatası:', e);
    return false;
  }
};

export const getScheduledNotifications = async () => {
  if (!Notifications) return [];

  try {
    return await Notifications.getAllScheduledNotificationsAsync();
  } catch (error) {
    console.error('Planlanmış bildirimler okunamadı:', error);
    return [];
  }
};