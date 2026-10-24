import tr from '../translations/tr.json';
import en from '../translations/en.json';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const Notifications = require('expo-notifications');
const REMINDER_ID_KEY = '@dailyReminderNotificationId';

export const isNotificationsAvailable = true;

if (Notifications) {
  try {
    Notifications.setNotificationHandler({
      handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
      }),
    });
  } catch (error) {
    if (__DEV__) console.warn('Notification handler could not be registered:', error);
  }
}

const parseReminderTime = (reminderTime = '09:00') => {
  const [hour, minute] = String(reminderTime).split(':').map((part) => parseInt(part, 10));
  if (!Number.isFinite(hour) || !Number.isFinite(minute) || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
    return null;
  }
  return { hour, minute };
};

const getStoredReminderId = async () => {
  try {
    return await AsyncStorage.getItem(REMINDER_ID_KEY);
  } catch (error) {
    if (__DEV__) console.error('Reminder ID could not be read:', error);
    return null;
  }
};

const setStoredReminderId = async (id) => {
  try {
    if (id) await AsyncStorage.setItem(REMINDER_ID_KEY, id);
    else await AsyncStorage.removeItem(REMINDER_ID_KEY);
  } catch (error) {
    if (__DEV__) console.error('Reminder ID could not be saved:', error);
  }
};

export const requestPermissions = async () => {
  if (!Notifications) return false;

  try {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync({
        ios: { allowAlert: true, allowSound: true },
        android: { allowAlert: true, allowSound: true }
      });
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
    if (__DEV__) console.error('Notification permission error:', e);
    return false;
  }
};


/** Cancels only the tracked daily reminder (not other scheduled notifications). */
export const cancelDailyReminder = async () => {
  if (!Notifications) {
    await setStoredReminderId(null);
    return true;
  }

  try {
    const id = await getStoredReminderId();
    if (id) {
      await Notifications.cancelScheduledNotificationAsync(id);
    }
    await setStoredReminderId(null);
    return true;
  } catch (e) {
    if (__DEV__) console.error('Daily reminder cancellation error:', e);
    return false;
  }
};

/** Nuclear cancel used for factory reset / clearing orphaned OS schedules. */
export const cancelAllReminders = async () => {
  if (!Notifications) {
    await setStoredReminderId(null);
    return true;
  }

  try {
    await Notifications.cancelAllScheduledNotificationsAsync();
    await setStoredReminderId(null);
    return true;
  } catch (e) {
    if (__DEV__) console.error('Notification cancellation error:', e);
    return false;
  }
};

export const scheduleDailyReminder = async (hour, minute, customMessage = null, locale = 'tr') => {
  if (!Notifications) return null;

  try {
    if (!(await cancelDailyReminder())) return null;

    const strings = locale === 'en' ? en : tr;
    const content = {
      title: strings.notification_reminder_title,
      body: customMessage || strings.notification_reminder_body,
      sound: true,
    };

    const trigger = {
      type: Notifications.SchedulableTriggerInputTypes.DAILY,
      hour,
      minute,
    };
    if (Platform.OS === 'android') {
      trigger.channelId = 'default';
    }

    const id = await Notifications.scheduleNotificationAsync({ content, trigger });
    await setStoredReminderId(id);
    return id;
  } catch (e) {
    if (__DEV__) console.error('Notification scheduling error:', e);
    await setStoredReminderId(null);
    return null;
  }
};

/**
 * Align OS scheduled reminders with persisted settings.
 * Call after load, restore, and reset.
 */
export const syncRemindersFromSettings = async (settings = {}, locale = 'tr') => {
  if (!settings.dailyReminder) {
    const cancelled = await cancelDailyReminder();
    return { success: cancelled, scheduled: false };
  }

  if (!Notifications) {
    return { success: true, scheduled: false, reason: 'unavailable' };
  }

  const granted = await requestPermissions();
  if (!granted) {
    return { success: false, scheduled: false, reason: 'permission' };
  }

  const parsed = parseReminderTime(settings.reminderTime);
  if (!parsed) {
    return { success: false, scheduled: false, reason: 'time' };
  }

  const id = await scheduleDailyReminder(
    parsed.hour,
    parsed.minute,
    settings.notificationMessage || null,
    locale,
  );
  return { success: !!id, scheduled: !!id, id };
};

export const sendTestNotification = async (customMessage = null, locale = 'tr') => {
  if (!Notifications) return false;

  try {
    await Notifications.scheduleNotificationAsync({
      content: {
        title: (locale === 'en' ? en : tr).notification_test_title,
        body: customMessage || (locale === 'en' ? en : tr).notification_test_body,
        sound: true,
        ...(Platform.OS === 'android' ? { channelId: 'default' } : {}),
      },
      trigger: null,
    });
    return true;
  } catch (e) {
    if (__DEV__) console.error('Test notification error:', e);
    return false;
  }
};

export const getScheduledNotifications = async () => {
  if (!Notifications) return [];

  try {
    return await Notifications.getAllScheduledNotificationsAsync();
  } catch (error) {
    if (__DEV__) console.error('Scheduled notifications could not be read:', error);
    return [];
  }
};
