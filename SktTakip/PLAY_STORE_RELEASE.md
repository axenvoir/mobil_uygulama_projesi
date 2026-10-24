# Google Play release checklist

## Build

- [ ] Confirm the Play Console application ID is `com.expira.app.tr` and that it is available. It cannot be changed after the first release.
- [ ] Build a production Android App Bundle: `npx eas-cli@latest build --platform android --profile production`.
- [ ] R8 code shrinking/optimization is enabled for release builds (`enableMinifyInReleaseBuilds` + `enableShrinkResourcesInReleaseBuilds` in `app.json` via `expo-build-properties`). After the first R8 build, smoke-test the release binary: biometric unlock, notifications, calendar add, backup restore, PDF export and image picking. The EAS production profile uploads the R8 mapping file (`ANDROID_PROGUARD_MAPPING: "true"`) for Play Console crash de-obfuscation.
- [ ] Review the generated AAB and install/test the signed release build on supported Android devices before upload.
- [ ] Start with an internal testing track, then complete any additional testing requirements shown in Play Console before requesting production access.
- [ ] Keep the EAS-managed Android signing key and Play App Signing enabled. Do not commit signing credentials.

The production profile automatically increments the local Android build number in `app.json`. Its current `android.versionCode` is `32`, above the previously used Play Console code `4`. The current user-facing release is `1.1.3`.

## Play Console listing and declarations

- [ ] Review `PRIVACY_POLICY.md`, replace its publisher/contact placeholders, publish it at a publicly accessible URL, and enter that URL in Play Console.
- [ ] Confirm that the support address `axenvoir@yahoo.com` is monitored and use it in the store listing.
- [ ] Use `PLAY_STORE_LISTING_TR.md` as a draft; verify all wording and provide English and any other store-localized versions as needed.
- [ ] Add the store listing, a 512 x 512 app icon, a 1024 x 500 feature graphic, and genuine screenshots captured from the release app. Do not use mock screens.
- [ ] Complete the Data safety form based on the shipped binary and all SDK/service behavior. Product, note, and profile data are stored on-device; users can explicitly export a backup or add an event to their device calendar. Recheck this description if analytics, ads, cloud sync, or other services are added.
- [ ] Complete the content rating, target-audience, ads, app-access, and financial-features declarations truthfully.
- [ ] Review Play Console's current API-level and testing requirements before production rollout.
- [ ] Recheck dependency advisories before production. On 18 October 2026, the unsafe UUID override was replaced with patched `uuid@11.1.1`. The remaining audit output groups findings under 19 high-severity package paths, rooted in `braces` and `node-forge`; neither upstream has published a patched npm release yet. Track [the braces advisory](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm) and [the node-forge advisory](https://github.com/advisories/GHSA-86w9-cpqp-85rv), and update Expo dependencies when compatible fixes are released. Do not run `npm audit fix --force`; it proposes downgrading Expo to 44.

## Current app details

- Application ID: `com.expira.app.tr`
- User-facing version: `1.1.3`
- Android target: API 36
- Build artifact: Android App Bundle (`.aab`)
- Product images are selected through Android's system picker; the app does not request broad media-storage or microphone access.
- Calendar access is only needed when a user chooses to add a product reminder to the device calendar. Notifications are local reminders configured by the user.

Store listing text, screenshots, privacy policy URL, and account-specific declarations must be finalized by the Play Console owner; they cannot be safely inferred from this codebase.
