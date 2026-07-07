# Pass Your PMP Exam

An **offline-first** Android app for studying toward the **PMP® (Project Management Professional)** certification. Practice with a bundled question bank, take timed mock exams, build a daily study streak, and track progress across the three PMP exam domains — no account, no network required.

> Status: version **1.0** (`versionCode 1`), in active development. See [Roadmap / placeholders](#roadmap--known-placeholders).

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
| Crash/analytics | Firebase (Crashlytics + Analytics) — declared |
| Testing | `kotlin-test` (JUnit4), coroutines-test, Turbine |

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
```

---

## Testing

Unit tests use `kotlin-test` (JUnit4), `kotlinx-coroutines-test`, and Turbine.

- **`:core:domain`** — `ScorerTest`, `QuestionSelectorTest`, `DailyQuestionSelectorTest`, `StreakCalculatorTest` (pure logic).
- **`:core:notifications`** — `ReminderTimingTest`.
- **Feature ViewModels** — `DailyViewModelTest`, `FreeViewModelTest`, `QuizViewModelTest`, `SettingsViewModelTest`.

> Note: Android modules must use `libs.kotlin.test` (which maps to `kotlin-test-junit`) rather than `kotlin("test")`, because AGP 9's built-in Kotlin doesn't apply the standalone Kotlin plugin.

---

## Roadmap / known placeholders

The following are intentionally stubbed and must be addressed before a production release:

- **Practice tab** — the bottom-nav *Practice* destination is a placeholder screen.
- **AdMob IDs** — the banner unit ID and AdMob application ID are Google's public **test** IDs.
- **Privacy Policy URL** — currently `https://example.com/privacy-policy` (see `docs/privacy-policy.md`).
- **Inter font** — the design system specifies Inter, but the app currently falls back to the platform default until the font assets are added.
- **Question bank** — the seed bank contains **9 questions** (3 per domain) at `bankVersion 1`; it's meant to grow.
- **Sync seams** — `Attempt`/`QuizSession` entities carry `syncState`/`updatedAt` fields that are local-only for now.

---

## Credits

Made with ❤️ by **Fax Development Studios**.

> PMP is a registered mark of the Project Management Institute, Inc. This project is an independent study aid and is not affiliated with or endorsed by PMI.
