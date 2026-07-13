# Pass Your PMP Exam — Developer Guide

> A guided tour of the codebase for anyone opening this project for the first time.
> If you read one file to understand the app, read this one.

---

## 1. What is this app? (the 60-second version)

**Pass Your PMP Exam** is an **offline-first Android study app** for people preparing for the [PMP](https://www.pmi.org/certifications/project-management-pmp) (Project Management Professional) certification.

- **No account, no backend.** All content and progress live on the device. The only network access is for ads and the privacy-consent flow.
- **100% Kotlin + Jetpack Compose** — no XML layouts, no Java.
- **Three study modes:** a **Daily Question** (one per day), **Quiz** (timed sessions, including a full 180-question mock exam), and **Free Practice** (untimed, filter-by-domain).
- **Habit features:** study **streaks**, a configurable **daily goal**, and an optional **daily reminder** notification.
- **Multi-module architecture** — a Clean-Architecture-style split (domain / data / presentation), MVI on the presentation side, wired together with **Koin** dependency injection.

That's the whole app. The rest of this document explains how it's built.

---

## 2. Tech stack at a glance

Everything is pinned in the Gradle **version catalog** at `gradle/libs.versions.toml`.

| Area | Choice | Version |
|------|--------|---------|
| Language | Kotlin | `2.4.0` |
| Build | Android Gradle Plugin | `9.2.1` |
| UI | Jetpack Compose (Material 3) | BOM `2026.06.01` |
| DI | Koin | `4.2.2` |
| Navigation | Navigation-Compose (type-safe routes) | `2.9.8` |
| Local DB | Room (via KSP) | `2.8.4` |
| Key-value store | DataStore Preferences | `1.2.1` |
| Async | Kotlinx Coroutines | `1.11.0` |
| Serialization | kotlinx.serialization (JSON) | `1.11.0` |
| Background work | WorkManager | `2.11.2` |
| Ads / consent | Google Mobile Ads + UMP | `25.4.0` / `4.0.0` |
| Crash / analytics | Firebase (Crashlytics + Analytics) | BOM `34.15.0` |
| Testing | kotlin-test (JUnit4), coroutines-test, Turbine | — |

**SDK levels** (`app/build.gradle.kts`): `minSdk 26`, `targetSdk 37`, `compileSdk 37`. Java source/target **11**.

---

## 3. How to build & run

Open the project in **Android Studio** and run the `:app` run configuration on an emulator or device (min API 26).

> ⚠️ **Heads-up:** Building from the command line with `./gradlew` currently fails in this environment because of an incomplete Android Studio JBR. Build and run **inside Android Studio**, not the terminal.

On first launch the app seeds its question bank from a bundled JSON asset (see [§10](#10-data-layer)) and shows a one-time onboarding screen.

---

## 4. The big picture (architecture)

The project follows a **layered, uni-directional dependency rule**. Nothing in an inner layer knows about an outer layer.

```
┌─────────────────────────────────────────────────────────────┐
│  :app            host — Application, MainActivity, NavHost     │
├─────────────────────────────────────────────────────────────┤
│  :feature:*      Presentation (MVI) — home/daily/quiz/free/    │
│                  settings.  Compose screens + ViewModels        │
├───────────────────────────────┬─────────────────────────────┤
│  :core:data                   │  :core:designsystem           │
│  Room, DataStore, repo impls, │  theme + shared Composables   │
│  question-bank importer       │  :core:notifications / :ads   │
├───────────────────────────────┴─────────────────────────────┤
│  :core:domain    Pure Kotlin — models, repository INTERFACES,  │
│                  and pure engines (Scorer, selectors, streak)  │
├─────────────────────────────────────────────────────────────┤
│  :core:common    Pure Kotlin — TimeProvider, IdGenerator,      │
│                  Outcome/AppError result types                 │
└─────────────────────────────────────────────────────────────┘
```

Three ideas to hold in your head:

1. **Clean-ish layering.** `:core:domain` and `:core:common` are *pure JVM* modules — no Android imports at all. They define the business rules and the *interfaces* for data. `:core:data` provides the concrete implementations (Room, DataStore). Features depend on the **interfaces**, never the implementations. This keeps business logic testable without an emulator, and would make it straightforward to move the domain into Kotlin Multiplatform later.

2. **MVI presentation.** Every feature screen has an immutable **UiState**, a sealed set of **Intents** (user actions), and a **ViewModel** with a single `onIntent()` entry point. See [§7](#7-the-mvi-presentation-pattern).

3. **Koin DI.** There's no compile-time DI graph (no Hilt/Dagger). Koin modules declare `single { ... }` for infrastructure and `viewModel { ... }` for ViewModels. The whole graph is started once in `PmpApplication`.

---

## 5. Project map — every folder explained

Modules are declared in `settings.gradle.kts`. Root package: `com.fax.passyourpmpexam`.

### Modules

| Module | Kind | What lives here |
|--------|------|-----------------|
| **`:app`** | Android application | The host. `PmpApplication` (starts Koin, seeds the bank), `MainActivity` (theme + consent + first-run gate), `ui/PmpApp.kt` (the NavHost + bottom bar), `ui/WelcomeScreen.kt` (onboarding), and the bundled questions at `app/src/main/assets/banks/pmp.json`. Depends on **every** other module. |
| **`:core:common`** | Pure Kotlin (JVM) | The smallest building blocks with zero Android dependencies: `TimeProvider`/`SystemTimeProvider`, `IdGenerator`/`UuidGenerator`, and the `Outcome`/`AppError` result types. Injecting time and IDs here is what makes everything deterministic in tests. |
| **`:core:domain`** | Pure Kotlin (JVM) | The heart of the app. Domain **models**, repository **interfaces** (`repository/Repositories.kt`), and pure stateless **engines** (`Scorer`, `QuestionSelector`, `DailyQuestionSelector`, `StreakCalculator`). No Android, no framework code. |
| **`:core:data`** | Android library | Concrete data implementations: the Room database, DataStore, repository impls, the question-bank importer, and the `dataModule` Koin definitions. Implements the interfaces declared in `:core:domain`. |
| **`:core:designsystem`** | Android library (Compose) | The visual language: `PmpTheme`, color/typography/spacing/shape tokens, and reusable Composables (buttons, cards, answer sheets, score ring, loading/empty states). |
| **`:core:notifications`** | Android library | The daily-reminder feature: a WorkManager-based scheduler, the notifier, a boot receiver, and `notificationsModule`. |
| **`:core:ads`** | Android library (Compose) | AdMob banner + Google UMP consent flow (`ConsentManager`, `AdsInitializer`, `HomeBannerAd`) and `adsModule`. |
| **`:feature:home`** | Android library (Compose) | The dashboard: greeting, streak/goal status, three mode cards, banner ad. |
| **`:feature:daily`** | Android library (Compose) | The one-per-day deterministic question. |
| **`:feature:quiz`** | Android library (Compose) | The richest feature: timed quizzes, mock exam, results, process-death resume. |
| **`:feature:free`** | Android library (Compose) | Untimed unlimited practice with domain filters (the "Practice" tab). |
| **`:feature:settings`** | Android library (Compose) | Theme, reminder, daily goal, about/disclaimer. |

### Non-module folders

| Folder | Purpose |
|--------|---------|
| `docs/` | Documentation. `privacy-policy.md`, the original architecture spec under `docs/superpowers/specs/…-pmp-prep-architecture-design.md`, and this guide. |
| `stitch_pmp_prep_mobile_design_system/` | HTML/PNG design mockups, the PRD, and design notes. **Reference-only** — not compiled or shipped. |
| `gradle/` | The Gradle wrapper and the `libs.versions.toml` version catalog. |
| `build/`, `.idea/` | Generated build output and IDE settings. |

### Module dependency graph

```
:app  ───▶ every :core and :feature module
:feature:*  ───▶ :core:common, :core:domain, :core:designsystem
:core:data  ───▶ :core:common, :core:domain
:core:notifications ───▶ :core:domain
:core:ads   ───▶ (Compose, Play Ads, UMP, Koin)   — no domain dep
:core:domain ───▶ :core:common
:core:common ───▶ (nothing — pure Kotlin)
```

Notice the shape: **arrows only point inward**. Features and data both point at `:core:domain`; the domain points only at `:core:common`; common points at nothing.

---

## 6. App startup & navigation flow

Follow the code in this order:

1. **`app/.../PmpApplication.kt`** → `onCreate()`
   - `startKoin { modules(dataModule, notificationsModule, adsModule, homeModule, dailyModule, quizModule, freeModule, settingsModule) }` — the entire DI graph is assembled here.
   - Launches `BankImporter.importIfNeeded()` on a background dispatcher to seed the question bank into Room.

2. **`app/.../MainActivity.kt`** → `onCreate()`
   - Triggers the ads/consent flow (`consentManager.gatherConsent(this)`).
   - `setContent { ... }` observes the user's `ThemeMode` and wraps the UI in `PmpTheme`.
   - **First-run gate:** reads `observeHasCompletedFirstRun()`. `false` → show `WelcomeScreen`; `true` → show `PmpApp()`; `null` (DataStore still loading) → render nothing, so returning users never see a flash of onboarding.

3. **`app/.../ui/PmpApp.kt`** → `PmpApp()`
   - A `Scaffold` with a `NavigationBar` (bottom nav) and a `NavHost`.
   - Three top-level tabs: **Home**, **Practice**, **Settings**. Tab switching uses `popUpTo(startDestination){ saveState = true }` + `launchSingleTop` + `restoreState` so each tab keeps its own back stack/state.

Routes are **type-safe** — `@Serializable` route objects, not string paths.

```
WelcomeScreen (first run only)
        │  "Get Started"
        ▼
┌──────────────── Bottom Nav ────────────────┐
│  Home tab          Practice tab   Settings  │
│  (nested graph)     FreeScreen    Settings  │
│   ├─ HomeRoute                              │
│   ├─ DailyRoute   ← from Home               │
│   └─ QuizRoute    ← from Home               │
└─────────────────────────────────────────────┘
```

Home is a **nested navigation graph** so that Daily and Quiz (pushed from the dashboard) keep the Home tab highlighted. Free Practice is the exception: launching it from Home **switches to the Practice tab** rather than pushing a screen, keeping tab selection consistent. Quiz and Daily receive an `onBack` lambda; Quiz additionally intercepts the back button while in progress to show a leave-confirmation dialog.

---

## 7. The MVI presentation pattern

Every feature module follows the **same five-file shape**. Learn it once and every feature reads the same way:

| File | Role |
|------|------|
| `XxxUiState.kt` | Immutable state. A **sealed interface** for multi-phase screens (Quiz, Daily) or a flat **data class** for simpler ones (Home, Free, Settings). |
| `XxxIntent.kt` | A sealed interface enumerating **every** user action. |
| `XxxViewModel.kt` | Holds a `MutableStateFlow<UiState>`, exposes a read-only `StateFlow`, and has a single `onIntent(intent)` dispatcher (`when` over the sealed intents). |
| `XxxScreen.kt` | A stateful `FeatureScreen(...)` that gets the ViewModel via Koin and collects state, delegating to a private, **stateless** `FeatureContent(state, onIntent)` (great for `@Preview`). |
| `XxxModule.kt` | The Koin module declaring `viewModel { XxxViewModel(get(), …) }`. |

**Effects/navigation:** there is no separate one-shot event channel. Navigation is done via **lambdas passed into the screen** (`onBack`, `onStartQuiz`, …); transient UI concerns (dialogs, expand toggles) use local `remember { mutableStateOf(...) }`.

**Error handling:** repository reads are wrapped in `try/catch`; failures surface as an error state (an `Error` branch on the sealed `UiState`s, or an `error` field on the flat data-class states) rendered with `ErrorState` + a retry `Intent` — so a failure is never silently indistinguishable from "no data".

### Worked example — Quiz (the richest MVI screen)

`feature/quiz/.../`

- **`QuizIntent`** (sealed): `SelectType`, `Start`, `ResumeSaved`, `DiscardSaved`, `SelectOption`, `Next`, `Previous`, `Submit`, `Restart`, `ExitToSetup`.
- **`QuizUiState`** (sealed): `Loading`, `Empty`, `ResumePrompt(session)`, `Setup(selectedType)`, `InProgress(session, remainingMillis)`, `Results(result, session)`.
- **`QuizViewModel`** maps each intent to a private handler; all transitions flow through one `MutableStateFlow`. It also exposes `onForeground()` / `onBackground()` lifecycle hooks so the **timer only accrues while the screen is foregrounded** (wired from the screen via a `DisposableEffect` on `ON_START`/`ON_STOP`).
- Progress is persisted to both Room and `SavedStateHandle`, enabling **resume after process death** (`ResumePrompt`) as well as same-process restore.
- **`QuizScreen`** collects the state and `when`-branches over the sealed states to render the matching content Composable.

---

## 8. Feature-by-feature tour

### Onboarding / Welcome
One-time intro shown only on first launch: a header, three feature cards (Daily / Quiz / Free), and a "Get Started" button that persists the first-run flag. Stateless — no ViewModel.
`app/.../ui/WelcomeScreen.kt` (gating in `MainActivity`).

### Home (dashboard)
The landing hub: time-based greeting, a status row (streak + daily-goal progress), three "Learning Mode" cards, and a banner ad. **Read-only MVI** — `HomeViewModel` just `combine`s streak, daily goal, and today's answered count into `HomeUiState`; there are no intents.
`feature/home/.../{HomeScreen,HomeComponents,HomeViewModel,HomeUiState,HomeModule}.kt`

### Daily Question
One **deterministic** question per day (`DailyQuestionSelector.pick(pool, today)`). Selecting an option grades it immediately, reveals the explanation in a result sheet, records a `DAILY` attempt, and advances the streak (idempotent per day). If today's question is already done, it opens in a neutral review state.
`feature/daily/.../{DailyScreen,DailyViewModel,DailyUiState,DailyIntent,DailyModule}.kt`

### Quiz
Timed sessions with a **setup → answering → results** lifecycle. Types: `SHORT_10` (13 min), `SHORT_25` (32 min), `SHORT_50` (64 min), and `MOCK_180` (180 questions / 230 min, drawn weighted by domain to mirror the real exam). On submit it scores via `Scorer`, records attempts, bumps the streak, and shows a results screen (score ring, per-domain breakdown, and expandable per-question review). See [§7](#7-the-mvi-presentation-pattern) for the full state machine.
`feature/quiz/.../{QuizScreen,QuizSetupContent,QuizViewModel,QuizUiState,QuizIntent,QuizModule}.kt`

### Free / Practice
Untimed, unlimited practice. Filter by domain via chips (no filter = all domains); an infinite reshuffling queue serves one question at a time, grading instantly and recording `FREE` attempts. Uses a **flat** `FreeUiState` data class.
`feature/free/.../{FreeScreen,FreeViewModel,FreeUiState,FreeIntent,FreeModule}.kt`

### Settings
Theme (System/Light/Dark), daily-reminder toggle + time picker (schedules via WorkManager, handles `POST_NOTIFICATIONS` on API 33+), daily goal, and a privacy-policy link. `SettingsViewModel` `combine`s four settings flows; intents write to `SettingsRepository` and drive the `ReminderScheduler`.
`feature/settings/.../{SettingsScreen,SettingsViewModel,SettingsUiState,SettingsIntent,SettingsModule}.kt`

---

## 9. Domain layer

`:core:domain` — pure Kotlin, no Android. This is where the rules live.

**Models** (`core/domain/.../model/`):
- `Question` — enforces its invariants in `init` (exactly `OPTION_COUNT = 4` options, a valid `correctIndex`); helpers `isCorrect(index)`, `correctOption`.
- `Domain` — enum `PEOPLE / PROCESS / BUSINESS_ENVIRONMENT`, each with a `blueprintWeight` (0.33 / 0.41 / 0.26) matching the PMP exam blueprint.
- `Quiz` — `QuizType`, `QuizMode` (DAILY/QUIZ/FREE), `QuizStatus` (IN_PROGRESS/COMPLETED/ABANDONED), plus `QuizConfig`, `SessionQuestion`, `QuizSession`.
- `Attempt`, `ScoreResult` (+ `DomainScore`), `StreakState`, `DailyGoal`, `ThemeMode`, `QuestionBank`.

**Repository interfaces** — all in one file, `core/domain/.../repository/Repositories.kt`: `ContentSource`, `QuestionRepository`, `AttemptRepository`, `QuizSessionRepository`, `StreakRepository`, `SettingsRepository`. Comments here flag the intended future seams (remote content, cloud sync).

**Engines (the "use cases").** Rather than injected `UseCase` classes, business logic is expressed as **pure stateless Kotlin `object`s**:
- `scoring/Scorer` — overall %, pass/fail at a fixed 61% threshold, per-domain breakdown.
- `selection/QuestionSelector` — deterministic (seeded `Random`) selection, including the blueprint-weighted 180-question mock draw.
- `daily/DailyQuestionSelector` — a stable pick from `epochDay`, avoiding recently seen questions.
- `streak/StreakCalculator` — pure streak state transitions.

Because these are pure functions with injected randomness, they're trivial to unit-test.

---

## 10. Data layer

`:core:data` — Android library implementing the domain interfaces. Two persistence technologies, **no remote API** (the remote seam is documented but not built).

### Room database — `pmp.db`
`core/data/.../local/PmpDatabase.kt` (`@Database version = 1, exportSchema = true`; schema exported to `core/data/schemas/` for future migration tests). Four entities:

| Entity / table | Purpose |
|----------------|---------|
| `QuestionEntity` / `questions` | The installed question pool (`options` stored as a JSON string). |
| `QuizSessionEntity` / `quiz_sessions` | Persisted quiz sessions for resume + results review (config, timer state, status). |
| `SessionQuestionEntity` / `session_questions` | Ordered question slots + progressive answers (FK → sessions, `CASCADE` delete). |
| `AttemptEntity` / `attempts` | Every graded answer across all modes — the single source of truth for stats (`domain` denormalized to avoid joins). |

DAOs live in `core/data/.../local/dao/`; a `Converters` class handles `List<String>` ⇄ JSON.

### DataStore — `pmp_settings`
Scalars only: theme, reminder enabled/time, daily goal, current/longest streak, last-activity day, installed bank version, first-run flag, consent flags. Keys are centralized in `core/data/.../datastore/PmpPreferencesKeys.kt`.

### Where the questions come from
A **bundled JSON asset**, not a remote API and not pre-seeded into the DB:

```
app/src/main/assets/banks/pmp.json     { bankVersion, certificationId, questions[] }
        │
        ▼  AssetContentSource   (reads off IO, parses, validates: unique ids,
        │                        known Domain, exactly 4 options)
        ▼  BankImporter.importIfNeeded()   (imports on first run or when the
        │                                   asset bankVersion > installed version;
        │                                   idempotent — returns 0 if current)
        ▼  QuestionRepository.upsertAll(...) ──▶ Room `questions` table
```

`BankImporter` is invoked from `PmpApplication.onCreate()` on a background coroutine.

### Data flow: source → repository → presentation
1. **Seed** at app start (above).
2. **Read:** a ViewModel depends on repository **interfaces**; `QuestionRepositoryImpl` reads via `QuestionDao` and maps entities → domain models through `mapper/Mappers.kt`.
3. **Sessions:** the ViewModel uses `QuestionSelector`/`Scorer` to build and grade a `QuizSession`, persisted via `QuizSessionRepositoryImpl` (serializes config, writes session + slot rows, rehydrates on read). Graded answers become `Attempt` rows.
4. **Reactivity:** `AttemptRepositoryImpl` exposes Room `Flow`s (e.g. answered-count-between-dates for the daily goal); settings/streak expose DataStore `Flow`s. ViewModels collect these and Compose renders them.

### DI wiring
`core/data/.../di/DataModule.kt` (`val dataModule`) binds: `Json`, `TimeProvider`, `IdGenerator`, the Room database + each DAO, the DataStore, every repository impl (bound to its domain interface), and the `ContentSource` + `BankImporter`. It's the first module registered in `PmpApplication`.

---

## 11. Design system

`:core:designsystem` (`.../theme/` and `.../component/`).

- **`PmpTheme`** (`Theme.kt`) — wraps `MaterialTheme` with the app's color scheme, typography, and shapes. **Deliberately no Material You dynamic color** — a fixed **violet** brand. Light/dark is chosen from the user's `ThemeMode`, resolved in `MainActivity`.
- **`Color.kt`** — full light + dark M3 palettes (brand primary violet `0xFF4200D6`). Semantic `PmpCorrect` / `PmpIncorrect` (green/red) are reserved strictly for answer feedback.
- **`Type.kt`** — `PmpTypography` built on the **Inter** font family (`core/designsystem/src/main/res/font/inter_*.ttf`).
- **`Dimens.kt`** — `PmpSpacing` on a 4px grid (`gridUnit 4`, `itemGap 12`, `safeMargin 16`, `basePadding 24`, `touchTargetMin 48`).
- **Shared components** — `PmpTopBar`, `PrimaryButton`, `QuestionCard`, `AnswerOption` / `AnswerFeedback` / `AnswerResultSheet`, `CircularScoreRing`, and `StateViews` (`LoadingState`, `EmptyState`, `ErrorState`). These give Daily, Quiz, and Free an identical question-answering UX. `ErrorState` (a retry-able failure state, distinct from `EmptyState`) is wired into every study screen.

The design derives from a Stitch design system; the reference mocks and tokens live under `stitch_pmp_prep_mobile_design_system/`.

---

## 12. Testing

- **Frameworks:** `kotlin-test` on JUnit4 (the catalog alias maps to `kotlin-test-junit`; see the rationale comment in `libs.versions.toml` — AGP 9's bundled Kotlin needs this artifact for the `@Test`/`@BeforeTest` typealiases), plus `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`, virtual time) and **Turbine** for Flow assertions.
- **No mocking library.** Tests use **hand-written fakes** implementing the `:core:domain` interfaces (e.g. `FakeQuestionRepository`, `FakeAttemptRepository`, `FakeStreakRepository`).
- **Determinism is injected.** `FakeTimeProvider`, a fixed `IdGenerator`, and a seeded `Random(0)` make time, IDs, and question selection reproducible — so ViewModels and engines are tested with **no Android, no Room, no Robolectric**.
- **Where tests live:**
  - Domain engines (the meatiest tests): `core/domain/src/test/.../{scoring,selection,daily,streak}/`.
  - Feature ViewModels: `feature/*/src/test/...` (Home, Quiz, Daily, Free, Settings), including error-path/retry coverage.
  - Data layer: `core/data/src/test/...` — `MappersTest`, `ConvertersTest`, `BankImporterTest`.
  - Notifications timing: `core/notifications/src/test/...`.
- **Instrumented tests** (`src/androidTest/`, run on a device/emulator via `connectedDebugAndroidTest`):
  - `:core:data` — `PmpDaoTest` (real Room round-trips, `CASCADE` delete, FK enforcement) and `MigrationTest`, which stands up `MigrationTestHelper` against the exported schema (only v1 exists, so it validates the plumbing; a `migrate1To2` template is ready for the first schema change).
  - `:feature:daily` / `:feature:quiz` — Compose smoke tests for the daily-question and take-a-quiz flows (fake-backed ViewModels, no Koin). They poll with `waitUntil` rather than `waitForIdle` because the quiz timer updates state every second.

---

## 13. Release config & remaining work

Production hardening is done. What's left is per-owner config and longer-term enhancements.

**Per-owner config (git-ignored — set before shipping):**
- **`secrets.properties`** (copy from `secrets.properties.template`) supplies the real `ADMOB_APP_ID`, `ADMOB_BANNER_UNIT_ID`, `PRIVACY_POLICY_URL`, and `SUPPORT_EMAIL`. Absent it, the build falls back to Google's public **test** ad IDs and `example.com` placeholders. Wiring: the app ID is a manifest placeholder set in `:app`; the unit ID is `:core:ads` `BuildConfig`; the URLs are `:feature:settings` `BuildConfig`. These aren't secrets (they ship in the APK) — they're externalized so forks of this **public** repo don't build with the maintainer's ad IDs.
- **`keystore.properties`** (from `keystore.properties.template`) + a release keystore enable release signing; absent, release builds are unsigned.
- **`app/google-services.json`** activates Firebase Crashlytics/Analytics — the `google-services`/`crashlytics` plugins auto-apply only when it's present.
- **`versionCode`** is still `1` in `app/build.gradle.kts` — bump before each store upload.

**Gotchas & deferred work:**
- **Sync fields are local-only** (`syncState = "LOCAL_ONLY"`) — no cloud sync exists yet; the seams are documented in `Repositories.kt`.
- The `Outcome`/`AppError` types in `:core:common` are **unused** — error handling uses per-`UiState` error branches instead. Safe to delete if you don't plan to adopt them.
- The version catalog **declares** convention plugins (`pmp.android.library`, etc.) that **no module currently applies** — there is no `build-logic` composite build. Modules apply catalog plugin aliases directly.
- The question bank is currently **139 questions at `bankVersion 6`**; bump `bankVersion` in `pmp.json` to trigger a re-import.

---

## 14. Where to go next

A good reading order to get productive:

1. `README.md` — the short intro and feature list.
2. `docs/superpowers/specs/2026-07-03-pmp-prep-architecture-design.md` — the original architecture spec and the "why".
3. `settings.gradle.kts` + `gradle/libs.versions.toml` — the module list and every pinned version.
4. `app/.../PmpApplication.kt` → `MainActivity.kt` → `ui/PmpApp.kt` — the runtime startup and navigation flow.
5. `core/domain/.../repository/Repositories.kt` + one feature triplet (e.g. `QuizUiState` / `QuizIntent` / `QuizViewModel`) — the MVI template you'll copy for any new screen.
6. `core/data/.../di/DataModule.kt` — how the concrete implementations bind to the domain interfaces.

Welcome aboard. 🎯
