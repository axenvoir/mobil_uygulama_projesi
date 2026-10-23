import * as Calendar from 'expo-calendar';
import { Platform } from 'react-native';

let hasCalendarPermission = null;

const getCalendarPermissions = async () => {
  if (hasCalendarPermission !== null) return hasCalendarPermission;
  const { status } = await Calendar.requestCalendarPermissionsAsync();
  hasCalendarPermission = status === 'granted';
  return hasCalendarPermission;
};

const getDefaultCalendarId = async () => {
  try {
    if (Platform.OS === 'ios') {
      const defaultCalendar = await Calendar.getDefaultCalendarAsync();
      return defaultCalendar?.id || null;
    }
    
    const calendars = await Calendar.getCalendarsAsync(Calendar.EntityTypes.EVENT);
    if (calendars.length === 0) return null;
    const local = calendars.find(c => c.source?.name === 'Local' || c.isLocalAccount);
    return (local || calendars[0]).id;
  } catch (e) {
    if (__DEV__) console.error('Takvim bulunamadı:', e);
    return null;
  }
};

export const addProductToCalendar = async (product) => {
  try {
    const granted = await getCalendarPermissions();
    if (!granted) {
      return { success: false, error: 'Takvim izni verilmedi.' };
    }

    const calendarId = await getDefaultCalendarId();
    if (!calendarId) {
      return { success: false, error: 'Uygun takvim bulunamadı.' };
    }

    const [year, month, day] = product.expDate.split('-').map(Number);
    const expDate = new Date(year, month - 1, day);
    const reminderDate = new Date(expDate);
    reminderDate.setDate(reminderDate.getDate() - 3);

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const targetDate = reminderDate < today ? expDate : reminderDate;

    const startDate = new Date(targetDate);
    startDate.setHours(10, 0, 0, 0);
    const endDate = new Date(targetDate);
    endDate.setHours(11, 0, 0, 0);

    const eventId = await Calendar.createEventAsync(calendarId, {
      title: `⏰ ${product.name} - SKT yaklaşıyor`,
      notes: `${product.name}${product.productCode ? ` [${product.productCode}]` : ''} ürününün son kullanma tarihi: ${product.expDate}`,
      startDate,
      endDate,
      alarms: [{ relativeOffset: 0 }],
    });

    return { success: true, eventId };
  } catch (e) {
    if (__DEV__) console.error('Takvim etkinliği ekleme hatası:', e);
    return { success: false, error: e.message };
  }
};