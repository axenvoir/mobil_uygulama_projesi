import {Alert} from 'react-native';

interface LogListener {
  (level: 'error' | 'warn', message: string): void;
}

const listeners: LogListener[] = [];

export const addLogListener = (listener: LogListener): (() => void) => {
  listeners.push(listener);
  return () => {
    const index = listeners.indexOf(listener);
    if (index >= 0) {
      listeners.splice(index, 1);
    }
  };
};

const emit = (level: 'error' | 'warn', message: string): void => {
  for (const listener of listeners) {
    try {
      listener(level, message);
    } catch {
      // listener must never crash the app
    }
  }
};

let sentryInitialized = false;

export const initCrashLogging = async (): Promise<void> => {
  const dsn = process.env.SENTRY_DSN;
  if (!dsn) {
    if (__DEV__) {
      console.warn('[Expira] SENTRY_DSN not set — crash logging disabled');
    }
    return;
  }
  try {
    const Sentry = await import('@sentry/react-native');
    Sentry.init({
      dsn,
      enableAutoSessionTracking: true,
      attachScreenshot: false,
    });
    sentryInitialized = true;
    Sentry.addBreadcrumb({
      message: 'crash-logging-initialized',
      category: 'app',
      level: 'info',
    });
    emit('warn', 'crash-logging-initialized');
  } catch (error) {
    emit('error', `sentry-init-failed: ${String(error)}`);
  }
};

export const captureError = (
  error: unknown,
  context?: string,
): void => {
  const message = error instanceof Error ? error.message : String(error);
  const enriched = context ? `${context}: ${message}` : message;
  emit('error', enriched);
  if (__DEV__) {
    console.error(`[Expira] ${enriched}`);
  }
  import('@sentry/react-native')
    .then(Sentry => {
      if (sentryInitialized) {
        Sentry.captureException(error instanceof Error ? error : new Error(enriched));
      }
    })
    .catch(() => undefined);
};

export const captureBreadcrumb = (message: string, data?: Record<string, unknown>): void => {
  import('@sentry/react-native')
    .then(Sentry => {
      if (sentryInitialized) {
        Sentry.addBreadcrumb({message, data});
      }
    })
    .catch(() => undefined);
};

export const showFatalErrorDialog = (message: string): void => {
  Alert.alert('Expira', message);
};
