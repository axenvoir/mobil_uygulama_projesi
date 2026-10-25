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

export const captureError = (error: unknown, context?: string): void => {
  const message = error instanceof Error ? error.message : String(error);
  const enriched = context ? `${context}: ${message}` : message;
  emit('error', enriched);
  if (__DEV__) {
    console.error(`[Expira] ${enriched}`);
  }
  import('@sentry/react-native')
    .then(Sentry => {
      Sentry.captureException(error instanceof Error ? error : new Error(enriched));
    })
    .catch(() => undefined);
};

export const captureBreadcrumb = (message: string, data?: Record<string, unknown>): void => {
  import('@sentry/react-native')
    .then(Sentry => {
      Sentry.addBreadcrumb({message, data, category: 'app', level: 'info'});
    })
    .catch(() => undefined);
};

export const showFatalErrorDialog = (message: string): void => {
  Alert.alert('Expira', message);
};
