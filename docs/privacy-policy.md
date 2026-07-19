<!--
  Hosting: this file is written to be published as-is. Options:
    - GitHub Pages from the /docs folder (Settings → Pages → source: main /docs), then the URL is
      https://<user>.github.io/<repo>/privacy-policy
    - Or paste the rendered content into any free static host.
  After hosting: replace PRIVACY_POLICY_URL in
  feature/settings/.../SettingsScreen.kt with the live URL, and paste the same URL into the
  Play Console listing + Data safety form.

-->

# Privacy Policy — Pass Your PMP Exam

**Effective date:** July 14, 2026

This Privacy Policy explains how the **Pass Your PMP Exam** app ("the app", "we") handles
information. The app is a free, offline-first PMP® exam-preparation quiz app for Android. Please
read it alongside the [Google Play Terms of Service](https://play.google.com/about/play-terms/).

## Summary

- The app does **not** require an account and does **not** ask you for any personal information.
- Your quiz progress, streak, and settings are stored **only on your device**.
- The app shows ads through **Google AdMob**, which may collect device and advertising information
  as described below. You control ad personalization through the consent prompt shown on first
  launch.
- The app may use **Firebase Crashlytics and Analytics** if enabled by the developer (see below).
- Uninstalling the app removes all data the app stored on your device.

## Information the app itself collects

**None that identifies you.** The app has no user accounts and no sign-in. All of the following is
kept **locally on your device** (in the app's private storage) and is never uploaded to us:

- Your answers, quiz sessions, and results.
- Your study streak and daily-question history.
- Your preferences (theme, daily-reminder time, and ad-consent choice).

We have no servers that receive this data, and we cannot see it.

## Advertising (Google AdMob)

The app displays a banner ad on the Home screen using **Google AdMob**. To serve ads, Google's
advertising services may collect and process information such as your **advertising ID**, general
device information, and approximate, non-precise location derived from your IP address.

On first launch the app shows a **consent request** (Google's User Messaging Platform, UMP) so you
can choose how your data is used for ads, in line with GDPR and Google's requirements. Ads are only
requested after this consent step.

Google's use of advertising data is governed by Google's own policies:

- [How Google uses information from sites or apps that use our services](https://policies.google.com/technologies/partner-sites)
- [Google Privacy Policy](https://policies.google.com/privacy)
- [Google AdMob](https://support.google.com/admob/answer/6128543) (about advertising IDs)

The following Android permissions support advertising and network access:
`INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, `ACCESS_ADSERVICES_AD_ID`,
`ACCESS_ADSERVICES_ATTRIBUTION`, and `ACCESS_ADSERVICES_TOPICS`.

## Notifications

If you enable the optional daily study reminder, the app schedules a **local notification** on your
device. This is handled entirely on-device; no reminder data is sent anywhere. Related permissions:
`POST_NOTIFICATIONS` (Android 13+ asks your permission when you enable reminders),
`RECEIVE_BOOT_COMPLETED` (to restore your reminder after a restart), and `WAKE_LOCK` /
`FOREGROUND_SERVICE` (used by the Android scheduling components).

## Analytics and crash reporting

The app may use **Firebase Crashlytics** for crash reporting and **Firebase Analytics** for
aggregate usage statistics. These services are **not active by default** — they only function when
the developer has provided a Firebase configuration file (`google-services.json`) and built a
release. When active, Firebase may collect:

- Device model, OS version, and crash stack traces (Crashlytics).
- Non-personally identifiable usage events and sessions (Analytics).

Firebase data is governed by [Google's Privacy Policy](https://policies.google.com/privacy) and
[Firebase's privacy and security documentation](https://firebase.google.com/support/privacy). You
may opt out of Analytics collection by running the app with the **Analytics-free** setting if
available, or by uninstalling.

## Third parties

The app uses the following third-party services:

- **Google AdMob** (Google LLC) — advertising, described above.
- **Firebase Crashlytics & Analytics** (Google LLC) — crash reporting and analytics, optional as
  described above.

We do not sell or share your personal information with any other third parties, because we do not
collect personal information ourselves.

## Data retention and deletion

Because the app stores data only on your device, you can delete all of it at any time by
**uninstalling the app** or clearing the app's storage in Android Settings. There is no server-side
data for us to retain or delete.

## Children

The app is intended for adults preparing for a professional certification and is **not directed to
children**. We do not knowingly collect personal information from children.

## Changes to this policy

We may update this policy from time to time. Material changes will be reflected by updating the
**Effective date** above and publishing the revised policy at the same URL.

## Contact

If you have questions about this policy, contact: **ffuentes@ixnec.mx**

---

*PMP is a registered mark of the Project Management Institute, Inc. This app is an independent
study aid and is not affiliated with or endorsed by PMI.*
