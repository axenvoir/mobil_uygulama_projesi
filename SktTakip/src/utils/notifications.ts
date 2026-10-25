import notifee, {
  AndroidImportance,
  AuthorizationStatus,
  RepeatFrequency,
  TriggerType,
  type TimestampTrigger,
} from '@notifee/react-native';
import type {Settings} from '../types';

const REMINDER_ID = 'expira-daily-reminder';

const ensureChannel = async (): Promise<void> => {
  await notifee.createChannel({
    id: 'reminders',
    name: 'Reminders',
    importance: AndroidImportance.DEFAULT,
  });
};

export const requestPermission = async (): Promise<boolean> => {
  try {
    const settings = await notifee.requestPermission();
    return settings.authorizationStatus === AuthorizationStatus.AUTHORIZED;
  } catch {
    return false;
  }
};

export const cancelDailyReminder = async (): Promise<void> => {
  try {
    await notifee.cancelTriggerNotification(REMINDER_ID);
  } catch {
    // notifications unavailable (e.g. test environment)
  }
};

export const scheduleDailyReminder = async (settings: Settings): Promise<void> => {
  await cancelDailyReminder();
  if (!settings.dailyReminder) {
    return;
  }
  await ensureChannel();
  const now = new Date();
  const next = new Date();
  next.setHours(settings.reminderHour, settings.reminderMinute, 0, 0);
  if (next.getTime() <= now.getTime()) {
    next.setDate(next.getDate() + 1);
  }
  const trigger: TimestampTrigger = {
    type: TriggerType.TIMESTAMP,
    timestamp: next.getTime(),
    repeatFrequency: RepeatFrequency.DAILY,
    alarmManager: {allowWhileIdle: true},
  };
  await notifee.createTriggerNotification(
    {
      id: REMINDER_ID,
      title: 'Expira',
      body: 'Check your products expiring soon.',
      android: {channelId: 'reminders', smallIcon: 'ic_launcher', pressAction: {id: 'default'}},
    },
    trigger,
  );
};
