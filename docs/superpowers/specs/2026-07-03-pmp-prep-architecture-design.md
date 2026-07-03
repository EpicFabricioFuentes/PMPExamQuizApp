# PMP Prep — Architecture & Technical Design Spec

**Date:** 2026-07-03
**Status:** Approved (design phase) — pending detailed implementation plan
**App:** Pass Your PMP Exam (`com.fax.passyourpmpexam`)
**Scope:** v1, Android-only build, architected KMP-ready.

---

## 0. Sources of Truth & Context

The **Single Source of Truth (SSOT)** for product & visual design is
`stitch_pmp_prep_mobile_design_system/` (PRD + `core_focus/DESIGN.md`). This document is
the **architecture** SSOT. Where any conflict arises, the design-system SSOT wins for
product/visual questions; this doc governs technical structure.

### Product context (fixed)
- Public Play Store app, **PMP-only**, free, single unobtrusive AdMob banner on **Home only**.
- **Android first** (Kotlin + Compose, Samsung One UI look) but **KMP-ready** so iOS via
  KMP / Compose Multiplatform is a later addition, not a rewrite.
- **Offline-first, hybrid**: question bank bundled, all progress local. **No accounts in v1.**
- **Three modes**: Daily Question (1/day, immediate explanation, feeds streak); Quiz
  (timed 10/25/50 + full mock 180 Q / 230 min, explanations at end); Free Mode (unlimited,
  domain-filterable, explanation after each question).
- **Light gamification**: streak + one user-set daily reminder. Light/dark theme. Violet accent.
- **Out of scope v1**: accounts, cloud sync, bookmarking/wrong-answer pools, XP/hearts/levels,
  multi-certification, iOS build.

### Existing scaffold (locked facts)
- Single `:app` module, package `com.fax.passyourpmpexam`.
- **minSdk 26 / targetSdk 36 / compileSdk 36**, Kotlin 2.4.0, Compose BOM 2026.06, Material3.
- Gradle version catalog present (`gradle/libs.versions.toml`). No DI/persistence/nav/extra deps yet.
- Release optimization (R8) currently **disabled** — to be enabled for release.

### Design tokens (from `core_focus/DESIGN.md`, authoritative)
- **Font:** Inter. **Primary:** `#4200d6`; primary-container `#5a31f4`; on-primary `#ffffff`.
- **Semantics:** green/red reserved **only** for correct/incorrect quiz feedback.
- **Surfaces:** light main `#FFFFFF`, grouped bg `#F2F2F7`; dark base deep charcoal `#121212`.
- **Shape:** cards/bottom-sheets 24px radius; buttons/chips 16px radius; no sharp 90° angles.
- **Spacing:** 4px grid; 24px horizontal margins; 48px min touch target; bottom-anchored primary actions.
- **Elevation:** tonal layers + 1px borders, not heavy shadows.
- **Type scale:** display-lg 34/700, display-lg-mobile 28/700, headline-md 22/600,
  section-header 14/600 uppercase +tracking, body-lg 18/400, body-md 16/400, label-sm 12/500.

### Screens (PRD, 10)
Home · Question Component (Default/Selected/Correct/Incorrect) · Daily Question · Quiz Setup ·
Quiz In-Progress · Quiz Results · Free Mode · Settings · Notification · States & Welcome
(loading, empty, first-run).

---

## 1. Architecture Decision Record

| # | Decision | Choice | Rationale |
|---|----------|--------|-----------|
| ADR-1 | Modularization | Multi-module *lite* (§2) | KMP + team seams without over-engineering; domain stays platform-free. |
| ADR-2 | KMP posture | KMP-*ready*, Android-only build | Pure-Kotlin domain, no Android in core; iOS/CMP is a later addition. |
| ADR-3 | Presentation | MVI (immutable `UiState`, `Intent` in, `Effect` out) | Unidirectional; one reusable question-component state across all 3 modes. |
| ADR-4 | Navigation | Type-safe Compose Navigation (`@Serializable` routes), single-Activity, **3-tab bottom bar** | Home hub + bottom nav (Home / Practice / Settings); Daily & Quiz are flows from Home. |
| ADR-5 | Persistence | Room (KMP) for questions/attempts/sessions; DataStore for settings + streak | Room 2.7+ is KMP-capable; DataStore for scalars. |
| ADR-6 | DI | Koin | KMP-friendly, no KSP/kapt for DI. |
| ADR-7 | Concurrency | Coroutines + Flow; `StateFlow` UI; countdown as cold `Flow` | Timer accrues **only while quiz is foregrounded**; background/process-death = pause. |
| ADR-8 | Content | Bundled JSON in `assets`, imported on first run keyed by `bankVersion`; `ContentSource` seam | Offline day one, remotely updatable later without full release. |
| ADR-9 | Notifications | WorkManager self-rescheduling worker + `BOOT_COMPLETED` receiver + API33+ `POST_NOTIFICATIONS` | Avoids exact-alarm Play-policy restriction; study nudge needn't be clock-exact. |
| ADR-10 | Ads | AdMob behind `AdProvider` in `:core:ads`; UMP consent; Home banner only; NoOp elsewhere | SDK never leaks into core/domain. |
| ADR-11 | Telemetry | `:core:analytics` interfaces; Firebase Crashlytics + minimal Analytics impl; consent-gated; NoOp for debug/tests | Crash signal + light funnel without leaking Firebase into features. |
| ADR-12 | Scoring | Blueprint-weighted mock (People 42% / Process 50% / Biz Env 8% → 76/90/14 of 180); **pass ≥ 61%**; per-domain breakdown | Pure `Scorer`/`QuestionSelector` engines. |
| ADR-13 | Streak | Any study activity (≥1 question answered) that day; miss a day → reset to 0 | Lazy recompute via pure `StreakCalculator`. |
| ADR-14 | Theme | System/Light/Dark selectable in Settings via `ThemeController` (DataStore `themeMode`) | Overrides system default; default = SYSTEM. |
| ADR-15 | Future-proofing | `certificationId`, `updatedAt`, `syncState`, UUID PKs on persisted rows | Multi-cert + cloud sync + accounts add columns, not rewrites. |

---

## 2. Module / Package Layout

```
:app                  Application, MainActivity, NavHost, bottom-nav scaffold, Koin startup, ThemeController wiring
:core:common          Result/dispatchers/date-epoch utils (pure Kotlin)
:core:domain          PURE KOTLIN — models, repository interfaces, engines/use-cases (KMP-movable)
:core:designsystem    Theme from SSOT tokens (Inter, violet, shapes, spacing) + shared composables:
                      QuestionCard, PrimaryButton, DomainChip, ProgressBar, CollapsingHeaderScaffold,
                      BottomSheetScaffold, BottomNavBar
:core:data            Room (KMP) db/entities/DAOs, DataStore, repo impls, JSON importer, ContentSource (KMP-movable core + Android drivers)
:core:ads             AdProvider interface + AdMob impl + UMP consent (+ NoOpAdProvider)
:core:analytics       Analytics + CrashReporter interfaces + Firebase impl (+ NoOp impls)
:core:notifications   ReminderScheduler (WorkManager), BootReceiver, POST_NOTIFICATIONS permission helper
:feature:home         Streak card, daily status, entry buttons, banner slot
:feature:daily        Daily Question → explanation → streak confirm
:feature:quiz         Setup → In-Progress (timer/progress) → Results (score/pass/domain/review)
:feature:free         Domain filter + immediate-feedback loop
:feature:settings     Reminder toggle + time picker, theme selector, About
:build-logic          Gradle convention plugins (android-library, compose, koin-module)
```

**Dependency rule:** features → `:core:domain` + `:core:designsystem` (+ `:core:common`).
- Only `:app` / `:core:data` know Room.
- Only `:core:ads` knows AdMob; only `:core:analytics` knows Firebase; only `:core:notifications` knows WorkManager.
- `:core:domain` and the non-driver logic in `:core:data` are the **KMP-movable core**.
- Features never depend on each other; cross-feature navigation is routed by `:app`.

---

## 3. Data Model / Schema

### 3.1 Room entities (`:core:data`)

**`QuestionEntity`**
| col | type | notes |
|-----|------|-------|
| `id` | String (PK) | stable slug, e.g. `pmp-people-0001` |
| `certificationId` | String | `"PMP"` in v1 |
| `domain` | String | `PEOPLE` / `PROCESS` / `BUSINESS_ENVIRONMENT` |
| `text` | String | question stem |
| `options` | List<String> | JSON TypeConverter; exactly 4 |
| `correctIndex` | Int | 0–3 |
| `explanation` | String | rationale |
| `bankVersion` | Int | provenance |

**`QuizSessionEntity`**
| col | type | notes |
|-----|------|-------|
| `id` | String (PK) | UUID |
| `certificationId` | String | |
| `type` | String | `SHORT_10/25/50`, `MOCK_180`, `FREE` |
| `status` | String | `IN_PROGRESS` / `COMPLETED` / `ABANDONED` |
| `configJson` | String | domain filter, etc. |
| `timeLimitMillis` | Long? | null for Free |
| `elapsedMillis` | Long | persisted for resume (pause semantics) |
| `currentIndex` | Int | resume cursor |
| `scorePercent` | Int? | on completion |
| `passed` | Boolean? | scorePercent ≥ 61 |
| `createdAt` / `completedAt` | Long / Long? | epoch millis |
| `updatedAt` | Long | sync seam |
| `syncState` | String | `LOCAL_ONLY` default; sync seam |

**`SessionQuestionEntity`** — PK (`sessionId`, `orderIndex`)
| col | type | notes |
|-----|------|-------|
| `sessionId` | String (FK) | |
| `orderIndex` | Int | planned order |
| `questionId` | String | |
| `selectedIndex` | Int? | progressive answer |
| `isCorrect` | Boolean? | set on answer |

Powers **process-death resume** and the **results review list**.

**`AttemptEntity`** — single source for aggregated stats across all modes
| col | type | notes |
|-----|------|-------|
| `id` | String (PK) | UUID |
| `questionId` | String | |
| `sessionId` | String? | null for Daily/Free |
| `domain` | String | denormalized for fast stats |
| `mode` | String | `DAILY` / `QUIZ` / `FREE` |
| `selectedIndex` | Int | |
| `isCorrect` | Boolean | |
| `answeredAt` | Long | |
| `updatedAt` | Long | sync seam |
| `syncState` | String | sync seam |

Daily/Free write an attempt immediately on answer; Quiz writes attempts on **submit**
(from the session's answered slots).

### 3.2 DataStore (typed) — settings + streak scalars
`reminderEnabled: Boolean`, `reminderMinuteOfDay: Int`, `currentStreak: Int`, `longestStreak: Int`,
`lastActivityEpochDay: Long`, `dailyLastAnsweredEpochDay: Long`, `dailyQuestionId: String?`,
`installedBankVersion: Int`, `hasCompletedFirstRun: Boolean`, `analyticsConsent: Boolean`,
`adsConsentState: String`, `themeMode: String` (`SYSTEM`/`LIGHT`/`DARK`, default `SYSTEM`).

### 3.3 Bundled bank JSON — `app/src/main/assets/banks/pmp.json`
```json
{
  "bankVersion": 1,
  "certificationId": "PMP",
  "questions": [
    {
      "id": "pmp-people-0001",
      "domain": "PEOPLE",
      "text": "…",
      "options": ["A", "B", "C", "D"],
      "correctIndex": 2,
      "explanation": "…"
    }
  ]
}
```
**Import (`ContentSource` → importer):** on first run, or when `assets.bankVersion > installedBankVersion`,
parse and upsert into Room, then set `installedBankVersion`. Validation: exactly 4 options,
`correctIndex` in 0–3, domain in enum, unique ids. `AssetContentSource` now; `RemoteContentSource`
is the later drop-in behind the same interface.

### 3.4 Pure engines (`:core:domain`, 100% unit-tested, no Android)
- **`QuestionSelector`** — builds the ordered question list per mode. Blueprint-weighted draw for
  `MOCK_180` (76 People / 90 Process / 14 Biz Env; documented rounding), filtered/random for
  short quizzes and Free. Injected `Random` (seedable for deterministic tests). No intra-session repeats.
- **`Scorer`** — from a completed session → overall %, pass/fail @ **61%**, per-domain breakdown
  (correct/total per domain), and time-vs-target.
- **`StreakCalculator`** — pure `onActivity(state, todayEpochDay)` and `displayStreak(state, todayEpochDay)`
  (shows 0 when `lastActivityEpochDay` < yesterday). Any-activity rule.
- **`DailyQuestionSelector`** — deterministic pick by epoch-day (stable across the day), persisted as
  `dailyQuestionId`; avoids recent repeats where pool allows.

---

## 4. Presentation (MVI) & Navigation

- Each feature exposes a `ViewModel` with `StateFlow<XxxUiState>` (immutable), an
  `onIntent(Intent)` entry point, and a one-shot `Effect` channel (navigation, haptics, snackbars).
- **Shared question component:** a stateless `QuestionCard` in `:core:designsystem` renders the four
  SSOT states (Default/Selected/Correct/Incorrect), driven by a `QuestionUiState` produced identically
  by Daily, Quiz, and Free view models — one interaction contract, three hosts.
- **Navigation:** single-Activity, type-safe Compose Navigation with `@Serializable` routes.
  Bottom-nav top-level destinations: **Home**, **Practice (Free)**, **Settings**. Daily and Quiz
  (Setup → In-Progress → Results) are flows pushed from Home. Cross-feature routing lives in `:app`.
- **Config change:** `ViewModel` survives; timer keeps running.
- **Process death:** `QuizSessionEntity` persists `status=IN_PROGRESS`, `currentIndex`, `elapsedMillis`,
  plus `SessionQuestionEntity` answers; `sessionId` kept in `SavedStateHandle`. On relaunch, an
  in-progress session surfaces a **Resume or Discard** prompt. **Timer accrues only while the quiz
  screen is foregrounded** — background/death effectively pauses it; resumes from persisted `elapsedMillis`.
- **Timer:** cold `Flow` ticking ~1s in the Quiz VM; `elapsedMillis` persisted on each answer, on
  `onStop`, and periodically.

---

## 5. Feature ↔ Screen Mapping

| Feature | Screens | Notes |
|---------|---------|-------|
| `:feature:home` | Home | Collapsing "Good Morning" header, streak/daily-status card, entry buttons (Daily/Quiz/Free), bottom banner slot. |
| `:feature:daily` | Daily Question | Question → sliding explanation panel → "Done" streak confirmation. |
| `:feature:quiz` | Quiz Setup, In-Progress, Results | Length 10/25/50 or Mock 180 (230 min); countdown; progress bar; results = score %, pass/fail @61%, per-domain bars, time-vs-target, review list. Resume/Discard on relaunch. |
| `:feature:free` | Free Mode | Domain filter chips; immediate feedback + explanation each question; unlimited. |
| `:feature:settings` | Settings | Reminder toggle + time picker, **theme selector (System/Light/Dark)**, About. |
| `:core:designsystem` + `:app` | Question Component states, Notification, States & Welcome | First-run welcome, loading, empty; motion (sliding explanation, collapsing header, progress fill, haptics). |

---

## 6. Platform Integrations

### 6.1 Notifications (`:core:notifications`)
- `ReminderScheduler.schedule(minuteOfDay)` computes delay to next occurrence, enqueues a **unique
  OneTimeWorkRequest**; the worker posts the notification then reschedules the next day.
- `BootReceiver` reschedules on `BOOT_COMPLETED`.
- API 33+ `POST_NOTIFICATIONS` runtime permission requested when the user enables the reminder.
- Past-time-today → schedule for tomorrow.

### 6.2 Ads (`:core:ads`)
- `AdProvider` interface exposes a `@Composable AdBanner(modifier)`. `AdMobAdProvider` loads a banner;
  `NoOpAdProvider` for debug/tests/consent-declined. Injected via Koin.
- **UMP/GDPR** consent gathered on first launch before loading ads. Home banner only. Test ad unit IDs in debug.

### 6.3 Analytics & Crash (`:core:analytics`)
- `Analytics` (`logEvent`, `setConsent`) + `CrashReporter` interfaces. `FirebaseAnalyticsImpl` +
  Crashlytics behind them; `NoOpAnalytics` for debug/tests. **Consent-gated.**
- Minimal events: `mode_started`, `quiz_completed{type,score}`, `daily_completed`, `streak_milestone`, `reminder_set`.
- Requires `google-services.json` + Google Services & Crashlytics Gradle plugins on `:app` — an explicit setup step.

---

## 7. Testing Strategy

- **Domain (fast JVM):** `Scorer`, `QuestionSelector` (seeded RNG, blueprint weighting), `StreakCalculator`,
  `DailyQuestionSelector`, timer math — pure, no Android.
- **Data:** Room in-memory DAO tests; importer validation tests; DataStore tests (Robolectric/instrumented).
- **Presentation:** ViewModel tests with fake repos + **Turbine** for `StateFlow`/`Effect`; `runTest` + test dispatcher.
- **UI:** Compose tests for `QuestionCard` states, quiz flow, results.
- **CI:** GitHub Actions — build + unit/JVM tests on PR; instrumented tests on demand.

---

## 8. Build & Tooling

- Version catalog (present) expanded with: koin, room (+ KSP), datastore, navigation-compose,
  work-runtime-ktx, play-services-ads, user-messaging-platform, firebase-bom/crashlytics/analytics,
  kotlinx-serialization, turbine, coroutines-test.
- `:build-logic` convention plugins for android-library / compose / koin-module config.
- minSdk 26, targetSdk 36 (unchanged). **Enable R8** for release (currently disabled). App signing config.
- Privacy policy required by ads + analytics (Play listing prerequisite).

---

## 9. Future-Proofing Seams

- `certificationId` on questions/sessions/attempts (single `"PMP"` now → multi-cert later).
- `updatedAt` + `syncState` + UUID PKs → cloud sync is additive; accounts add a `userId` column defaulting to local.
- Repository interfaces in `:core:domain`; `ContentSource` abstraction for remote banks; `SettingsRepository`
  can later back onto remote config.
- No account coupling anywhere in v1.

---

## 10. Implementation Plan (v1)

- **Phase 0 — Foundation:** convert `:app` → multi-module; add `:build-logic` convention plugins; expand
  version catalog; wire Koin. Build `:core:designsystem` from SSOT tokens (replace stock `Color.kt`, add
  Inter, shapes, spacing, base components incl. `BottomNavBar`).
- **Phase 1 — Domain & data (TDD):** `:core:domain` models/interfaces/engines with unit tests first;
  `:core:data` Room + DataStore + importer + sample `pmp.json`; DAO/importer tests.
- **Phase 2 — Features:** Home (+ bottom-nav scaffold) → Daily → Quiz (setup/in-progress/results +
  process-death resume) → Free → Settings (incl. theme selector). ViewModel tests (Turbine) + Compose
  tests for `QuestionCard`.
- **Phase 3 — Platform:** notifications (WorkManager + boot + permission); ads (`:core:ads` + UMP, Home
  banner, test IDs); analytics/Crashlytics (`:core:analytics`, consent-gated; `google-services.json` setup).
- **Phase 4 — Polish & release:** first-run/loading/empty states; motion (sliding explanation, collapsing
  header, progress fill, haptics); CI; enable R8 for release; signing; privacy policy.

---

## 11. Open Items / Judgment Calls (confirmed)

1. Quiz timer **pauses** on background/process death, resuming from persisted `elapsedMillis`. ✅
2. **Bottom navigation in v1**: tabs Home / Practice (Free) / Settings; Daily & Quiz are flows from Home. ✅
3. **Theme selector in Settings** (System/Light/Dark). ✅
4. Spec written to this path; repo not under git, so no commit (init later if desired). ✅
