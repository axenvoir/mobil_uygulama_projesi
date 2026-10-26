import {AppRegistry} from 'react-native';
import * as Sentry from '@sentry/react-native';
import App from './src/App';
import {name as appName} from './app.json';

const FALLBACK_DSN =
  'https://2132815ad0c611f1b9c77a66a3057630@o4512318976163840.ingest.de.sentry.io/4512319010439248';
const DSN = process.env.SENTRY_DSN || FALLBACK_DSN;

Sentry.init({
  dsn: DSN,
  enableNativeCrashHandling: true,
  enableAutoSessionTracking: true,
  attachScreenshot: false,
});

AppRegistry.registerComponent(appName, () => App);

// TEMPORARY DIAGNOSTIC: connection test event for Sentry onboarding.
// Confirms DSN wiring when the JS runtime reaches this point.
// Remove once crash diagnosis is complete.
Sentry.captureException(new Error('Expira Sentry connection test'));
Sentry.addBreadcrumb({message: 'index_js_reached', category: 'app', level: 'info'});
