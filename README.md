# Pass Your PMP Exam

An **offline-first** Android app for studying toward the **PMP® (Project Management Professional)** certification. Practice with a bundled question bank, take timed mock exams, build a daily study streak, and track progress across the three PMP exam domains — no account, no network required.

> Status: version **2026.1.0** (`versionCode 1`). Production hardening (release signing, R8, splash, crash reporting, error handling, and unit + instrumented tests) is done; the remaining pre-launch steps are your own credentials/assets. See [Releasing](#releasing) and [Roadmap](#roadmap--remaining-work).

---

## Features

### Learning modes
- **Daily Question** — one deterministic question per day (stable per calendar day). Answering reveals an explanation with haptic feedback, updates your streak, and counts toward your daily goal. The day can't be double-counted.
- **Quiz Mode** — configurable practice/mock exams:
  | Type | Questions | Time limit |
  |------|-----------|------------|
  | Short | 10 | 13 min |
  | Medium | 25 | 32 min |
  | Long | 50 | 64 min |
  | Full Mock | 180 | 230 min |

  The full mock mirrors the real exam (180 questions / 230 minutes) and selects questions **weighted by the PMP blueprint** (People 42% → 76q, Process 50% → 90q, Business Environment 8% → 14q). Includes a live countdown timer that **pauses/resumes with the app lifecycle** (time accrues only while the app is foregrounded), auto-submit when time expires, Previous/Next navigation, and a results screen with overall score, pass/fail, per-domain breakdown, and a full answer review with explanations.
- **Free Practice** — unlimited, untimed practice with optional **domain filter chips**. The question queue reshuffles and refills endlessly.

### Progress & scoring
- **Pass threshold: 61%** (`Scorer.PASS_THRESHOLD_PERCENT`).
- **Per-domain scoring** across People, Process, and Business Environment.
- **Study streak** — current & longest streak with consecutive-day tracking (resets after a missed day).
- **Daily goal** — questions-per-day target (range **1–20**, default 1), counted across all modes.
- **Session resume** — an in-progress quiz survives navigation and process death; you're prompted to resume on cold start.

### Quality-of-life
- **Theme** — System / Light / Dark (fixed violet brand palette, primary `#4200D6`).
- **Daily reminder** — optional local notification at a chosen time (default 20:00), rescheduled across reboots. Requests `POST_NOTIFICATIONS` on Android 13+.
- **Onboarding** — a one-time, theme-aware welcome screen on first launch, with a top-weighted header and three feature cards (Daily Question / Quiz Mode / Free Mode).
- **Ads** — a single anchored adaptive banner on the Home screen, shown only after UMP consent.
- **Trademark disclaimer** — the Settings screen footer carries the PMP/PMI independence notice.

---

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin `2.4.0` |
| UI | Jetpack Compose (Material 3), Compose BOM `2026.06.01` |
| Build | Android Gradle Plugin `9.2.1`, Gradle version catalog (`gradle/libs.versions.toml`) |
| DI | Koin `4.2.2` |
| Navigation | Navigation-Compose `2.9.8` (type-safe `@Serializable` routes) |
| Persistence | Room `2.8.4` (KSP) + DataStore Preferences `1.2.1` |
| Async | Kotlinx Coroutines `1.11.0` |
| Serialization | kotlinx.serialization JSON `1.11.0` |
| Background work | WorkManager `2.11.2` (daily reminder) |
| Ads / consent | Google Mobile Ads `25.4.0` + UMP `4.0.0` |
| Crash/analytics | Firebase (Crashlytics + Analytics) — wired, opt-in via `app/google-services.json` |
| Testing | `kotlin-test` (JUnit4), coroutines-test, Turbine; instrumented: Room + Compose UI tests |

**SDK levels:** `minSdk 26`, `targetSdk 37`, `compileSdk 37`.

---

## Architecture

Multi-module, clean-architecture project following an **MVI** pattern in the presentation layer (immutable `UiState` + `Intent`, state owned by `ViewModel`s, dumb Composables).

```
:app                     — MainActivity, PmpApplication (Koin), NavHost + bottom nav, onboarding
:core:common             — TimeProvider, IdGenerator, Outcome
:core:domain             — models + pure business logic (scoring, selection, streaks); repository interfaces
:core:data               — Room DB, DataStore, repository implementations, question-bank importer
:core:designsystem       — theme (color/type/spacing/shape) + reusable Compose components
:core:notifications      — WorkManager-based daily reminder scheduler + notifier
:core:ads                — AdMob banner + UMP consent flow
:feature:home            — dashboard (greeting, streak/goal cards, mode cards, banner ad)
:feature:daily           — daily question
:feature:quiz            — quiz setup / in-progress / results / resume
:feature:free            — free practice
:feature:settings        — theme, reminder, daily goal, about, PMP/PMI disclaimer
```

**Dependency rule:** features and `:core:data` depend on `:core:domain` (interfaces + models); domain logic is pure and framework-free. The app is offline — the only network use is ads/consent.

### Domain model highlights (`:core:domain`)
- `Domain` — `PEOPLE (33%)`, `PROCESS (41%)`, `BUSINESS_ENVIRONMENT (26%)`.
- `Question` — enforces exactly 4 options and an in-range `correctIndex`; carries `domain`, `explanation`, `bankVersion`.
- `QuizSession` / `SessionQuestion` — a session's questions, current index, status, and foreground-only elapsed time.
- `Attempt` — the single source of truth for aggregated stats, tagged by `QuizMode { DAILY, QUIZ, FREE }`.
- `ScoreResult` / `DomainScore`, `ThemeMode`, `DailyGoal`, `StreakState`, `QuestionBank`.
- Pure logic: `Scorer`, `QuestionSelector` (deterministic-by-seed, blueprint-weighted for mocks), `DailyQuestionSelector`, `StreakCalculator`, `ReminderTiming`.

### Data layer (`:core:data`)
- **Room** database `pmp.db` (v1): `QuestionEntity`, `QuizSessionEntity`, `SessionQuestionEntity`, `AttemptEntity`.
- **DataStore** `pmp_settings`: theme, reminder enabled/time, daily goal, streak counters, last-answered day, installed bank version, first-run flag.
- **Question bank** is bundled as a JSON asset at `app/src/main/assets/banks/pmp.json` and imported on first run (or when its `bankVersion` increases) via `BankImporter.importIfNeeded()`. Import is validated and idempotent.

---

## Getting started

### Prerequisites
- Android Studio (a recent Ladybug+/2026-era build matching AGP 9)
- JDK 11+ (project targets Java 11 source/target compatibility)
- Android SDK with API 37 installed

### Build & run
```bash
# from the repo root
./gradlew :app:assembleDebug      # build the debug APK
./gradlew installDebug            # install on a connected device/emulator
```
Or open the project in Android Studio and run the **app** configuration.

### Test
```bash
./gradlew test                    # all JVM unit tests
./gradlew :feature:quiz:testDebugUnitTest
./gradlew connectedDebugAndroidTest   # instrumented tests (needs a device/emulator)
```

---

## Releasing

The repo builds and runs out of the box with safe placeholder/test values. To cut a real release you
supply your own credentials and assets — none of which are committed:

1. **Per-owner config** — `cp secrets.properties.template secrets.properties`, then fill in your real
   `ADMOB_APP_ID`, `ADMOB_BANNER_UNIT_ID`, `PRIVACY_POLICY_URL`, and `SUPPORT_EMAIL`. The file is
   git-ignored; absent it, the build falls back to Google's public **test** ad IDs and `example.com`
   placeholders. (These values aren't secret — they ship in the APK — but are kept out of this public
   repo so forks don't build with the maintainer's ad IDs.)
2. **Signing** — `cp keystore.properties.template keystore.properties`, create a release keystore, and
   point the properties at it. Without it, release builds are simply unsigned.
3. **Crash reporting** — drop `app/google-services.json` in to activate Firebase Crashlytics/Analytics
   (the `google-services`/`crashlytics` plugins auto-apply only when the file is present).
4. **Privacy policy** — host `docs/privacy-policy.md` at a public URL (required by Play + UMP consent
   because ads are enabled) and set `PRIVACY_POLICY_URL`.
5. **Version** — bump `versionCode` in `app/build.gradle.kts` before each store upload.

Then build a **signed, minified** release (`:app:assembleRelease` / `bundleRelease`) and verify R8
didn't break serialization or Room. `app/proguard-rules.pro` holds the kotlinx.serialization keep rules.

---

## Testing

Unit tests use `kotlin-test` (JUnit4), `kotlinx-coroutines-test`, and Turbine.

- **`:core:domain`** — `ScorerTest`, `QuestionSelectorTest`, `DailyQuestionSelectorTest`, `StreakCalculatorTest` (pure logic).
- **`:core:data`** — `MappersTest`, `ConvertersTest`, `BankImporterTest` (idempotency + version-bump import).
- **`:core:notifications`** — `ReminderTimingTest`.
- **Feature ViewModels** — `HomeViewModelTest`, `DailyViewModelTest`, `FreeViewModelTest`, `QuizViewModelTest`, `SettingsViewModelTest` (incl. error-path/retry coverage).

Instrumented tests (`src/androidTest/`, run on a device/emulator):

- **`:core:data`** — `PmpDaoTest` (real Room round-trips, `CASCADE` delete, FK enforcement) and `MigrationTest` (`MigrationTestHelper` against the exported schema).
- **`:feature:daily` / `:feature:quiz`** — Compose smoke tests for the daily-question and take-a-quiz flows.

> Note: Android modules must use `libs.kotlin.test` (which maps to `kotlin-test-junit`) rather than `kotlin("test")`, because AGP 9's built-in Kotlin doesn't apply the standalone Kotlin plugin.

---

## Roadmap / remaining work

Production hardening is complete (see [Releasing](#releasing)). What's left before a store launch is
**your** credentials/assets, plus longer-term enhancements:

**Before you can ship** (all covered in [Releasing](#releasing)):
- Fill `secrets.properties` with real AdMob IDs, privacy-policy URL, and support email.
- Add a release keystore (`keystore.properties`) and `app/google-services.json`.
- Host the privacy policy and bump `versionCode`.

**Future enhancements** (deliberately deferred, not blockers):
- **Question bank growth** — currently **139 questions** at `bankVersion 6`; can keep growing (bump `bankVersion` to re-import).
- **Cloud sync** — `Attempt`/`QuizSession` carry `syncState`/`updatedAt` seams that are local-only for now.
- **Localization / i18n** — strings are currently inline English.
- **`build-logic` convention plugins** — the catalog declares `pmp.*` plugin aliases that no module applies yet; modules use catalog aliases directly.

---

## Credits

Made with ❤️ by **Fax Development Studios**.

> PMP is a registered mark of the Project Management Institute, Inc. This project is an independent study aid and is not affiliated with or endorsed by PMI.
