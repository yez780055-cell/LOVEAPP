# Life Space Native

LOVEAPP is the Android native foundation for bringing Todo, Period, and Anniversary into one local-first app. This branch establishes the shared app shell and feature boundaries; Todo and Period remain migration skeletons, while the existing Anniversary flows continue to work.

## Architecture

- One Activity with Navigation Compose and three state-restoring bottom navigation destinations: 待办、小月历、纪念日.
- Feature packages are separated under `app/src/main/java/dev/yezi/anniversary/feature/`; the anniversary-specific RemoteViews widget and reminder delivery adapter live inside Anniversary. Core owns generic Room reminder records, DAO, and an absolute-time AlarmManager scheduler.
- Anniversary data remains in `anniversary.db`. Room v1→v2 adds Todo and Period tables and maps legacy reminder `eventId` to `sourceType/sourceId`; v2→v3 removes the Anniversary-only reminder column while preserving existing records and scheduling settings.
- Todo and Period mount feature-owned routes, repositories/ViewModels, entities, and DAOs, but remain migration placeholders. Backup, media storage, and app-preference interfaces are contracts only; their product workflows are not implemented.
- The application ID remains `dev.yezi.anniversary` to preserve Android upgrade identity.

See `docs/ARCHITECTURE.md`, `docs/MIGRATION_PLAN.md`, and `docs/FEATURE_PARITY.md` for boundaries and remaining work.

## Build

Requires JDK 17 and Android SDK Platform 35.

```sh
./gradlew test
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

The JVM unit-test job includes a Robolectric execution of the real v1→v3 Room migration, then builds and uploads the debug APK. Android emulator smoke is isolated in an experimental, continue-on-error job because the hosted emulator repeatedly reported `adb: device offline` and crashed before discovering instrumentation tests. A green build job does not mean emulator smoke passed.
