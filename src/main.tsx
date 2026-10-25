import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { ErrorBoundary } from './components/ErrorBoundary';
import './index.css';

// Global uncaught error listener to ensure errors are captured in console/telemetry
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    console.error('[Expira Global Window Error]', event.error || event.message);
  });
  window.addEventListener('unhandledrejection', (event) => {
    console.error('[Expira Global Unhandled Promise Rejection]', event.reason);
  });
}

const rootElement = document.getElementById('root');

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <ErrorBoundary fallbackTitle="Expira Başlatılamadı / Initialization Error">
        <App />
      </ErrorBoundary>
    </React.StrictMode>
  );
} else {
  console.error('[Expira] Fatal: Root element #root was not found in the DOM.');
}
