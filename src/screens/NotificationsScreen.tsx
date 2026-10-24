import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { formatDate, getDaysLeft } from '../utils/formatters';
import {
  Bell,
  Clock,
  Send,
  AlertTriangle,
  CheckCircle2,
  Calendar,
} from 'lucide-react';

export const NotificationsScreen: React.FC = () => {
  const { settings, updateSettings, products, locale, t, showToast } = useApp();

  const [message, setMessage] = useState(
    settings.notificationMessage || t('notification_default_message')
  );

  const TIME_PRESETS = [
    { label: t('time_picker_morning'), time: '08:00' },
    { label: t('time_picker_noon'), time: '12:00' },
    { label: t('time_picker_evening'), time: '18:00' },
    { label: t('time_picker_night'), time: '21:00' },
  ];

  // Expiring soon items for preview
  const expiringItems = products.filter((p) => {
    const days = getDaysLeft(p.expDate);
    return days <= 7;
  });

  const handleToggleDaily = () => {
    const next = !settings.dailyReminder;
    updateSettings({ dailyReminder: next });
    showToast(
      next
        ? t('notification_daily_enabled', { time: settings.reminderTime })
        : t('notification_daily_disabled'),
      next ? 'success' : 'info'
    );
  };

  const handleTimeSelect = (time: string) => {
    updateSettings({ reminderTime: time });
    showToast(t('notification_time_updated', { time }), 'success');
  };

  const handleSaveMessage = () => {
    updateSettings({ notificationMessage: message.trim() });
    showToast(t('notification_message_saved'), 'success');
  };

  const handleSendTestNotification = () => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(t('notification_test_title'), {
        body: t('notification_test_body'),
        icon: '/SktTakip/assets/branding/expira-icon.png',
      });
    }

    showToast(t('notification_test_title') + ': ' + t('notification_test_body'), 'success');
  };

  return (
    <div className="space-y-5 pb-24 max-w-lg mx-auto">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('notifications')}
        </h2>
      </div>

      {/* Daily Reminder Toggle Card */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-teal-50 dark:bg-teal-950/40 text-[#267968] dark:text-[#70CDB0] flex items-center justify-center">
              <Bell className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t('notification_daily_reminder')}
              </h3>
              <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
                {settings.dailyReminder
                  ? `${t('notification_active_daily')} (${settings.reminderTime})`
                  : t('notification_off')}
              </p>
            </div>
          </div>

          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={settings.dailyReminder}
              onChange={handleToggleDaily}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#267968]"></div>
          </label>
        </div>

        {/* Time Selector */}
        {settings.dailyReminder && (
          <div className="pt-3 border-t border-[#E1E8E3] dark:border-[#344A40] space-y-2.5 animate-in fade-in">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC] flex items-center justify-between">
              <span>{t('notification_time')}</span>
              <span className="text-[11px] font-mono text-[#267968] dark:text-[#70CDB0]">
                {settings.reminderTime}
              </span>
            </label>

            {/* Presets */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {TIME_PRESETS.map((preset, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handleTimeSelect(preset.time)}
                  className={`px-2.5 py-2 rounded-xl text-xs font-semibold text-center transition-all ${
                    settings.reminderTime === preset.time
                      ? 'bg-[#267968] text-white dark:bg-[#70CDB0] dark:text-[#0D2623] shadow-sm'
                      : 'bg-[#EDF2EE] dark:bg-[#253A33] text-[#19332D] dark:text-[#F0F4EC] hover:bg-[#E1E8E3]'
                  }`}
                >
                  <div className="text-[10px] opacity-80">{preset.time}</div>
                  <div className="truncate">{preset.label.split(' ')[0]}</div>
                </button>
              ))}
            </div>

            {/* Custom time picker */}
            <div className="flex items-center gap-2 pt-1">
              <Clock className="w-4 h-4 text-[#5A6B63] dark:text-[#B5C5BC]" />
              <input
                type="time"
                value={settings.reminderTime}
                onChange={(e) => handleTimeSelect(e.target.value)}
                className="px-3 py-1.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-xs font-mono font-bold focus:outline-none"
              />
            </div>
          </div>
        )}
      </div>

      {/* Reminder Message Card */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-3">
        <div>
          <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('notification_message')}
          </h3>
          <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
            {t('notification_message_description')}
          </p>
        </div>

        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder={t('notification_default_message')}
          className="w-full px-4 py-2.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-xs focus:outline-none focus:ring-2 focus:ring-[#267968]"
        />

        <div className="flex justify-end">
          <button
            onClick={handleSaveMessage}
            className="px-4 py-1.5 bg-[#267968] dark:bg-[#70CDB0] text-white dark:text-[#0D2623] rounded-xl text-xs font-bold shadow-sm"
          >
            {t('save')}
          </button>
        </div>
      </div>

      {/* Test notification trigger */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm flex items-center justify-between gap-3">
        <div>
          <h4 className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('notification_send_test')}
          </h4>
          <p className="text-[11px] text-[#5A6B63] dark:text-[#B5C5BC]">
            Check in-app notification alerts
          </p>
        </div>

        <button
          onClick={handleSendTestNotification}
          className="flex items-center gap-1.5 px-3 py-2 bg-[#EDF2EE] hover:bg-[#E1E8E3] dark:bg-[#253A33] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold transition-transform active:scale-95"
        >
          <Send className="w-3.5 h-3.5 text-[#267968] dark:text-[#70CDB0]" />
          <span>{t('notification_send_test')}</span>
        </button>
      </div>

      {/* Live Upcoming Expiry Alerts section */}
      <div className="space-y-2.5">
        <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC] flex items-center justify-between">
          <span>{t('statistics_upcoming_expiry')}</span>
          <span className="text-xs font-normal text-[#5A6B63] dark:text-[#B5C5BC]">
            {t('notification_upcoming_count', { count: expiringItems.length })}
          </span>
        </h3>

        {expiringItems.length === 0 ? (
          <div className="p-4 bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/40 rounded-2xl flex items-center gap-2.5 text-xs text-emerald-800 dark:text-emerald-300 font-medium">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{t('notification_none_scheduled')}</span>
          </div>
        ) : (
          <div className="space-y-2">
            {expiringItems.map((p) => {
              const days = getDaysLeft(p.expDate);
              return (
                <div
                  key={p.id}
                  className="p-3 bg-white dark:bg-[#1A2B26] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] flex items-center justify-between gap-3 text-xs"
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <AlertTriangle className={`w-4 h-4 shrink-0 ${days < 0 ? 'text-rose-500' : 'text-amber-500'}`} />
                    <span className="font-bold text-[#19332D] dark:text-[#F0F4EC] truncate">{p.name}</span>
                  </div>
                  <div className="text-[#5A6B63] dark:text-[#B5C5BC] font-medium shrink-0">
                    {formatDate(p.expDate, locale)} ({days < 0 ? `${Math.abs(days)}d overdue` : `${days}d left`})
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
