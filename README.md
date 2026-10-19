# Life Space Native

LOVEAPP is the Android native foundation for bringing Todo, Period, and Anniversary into one local-first app. This branch establishes the shared app shell and feature boundaries; Todo and Period remain migration skeletons, while the existing Anniversary flows continue to work.

## Architecture

- One Activity with Navigation Compose and three state-restoring bottom navigation destinations: 待办、小月历、纪念日.
- Feature packages are separated under `app/src/main/java/dev/yezi/anniversary/feature/`; shared date, Room, notification, widget, backup, media, settings ports, and design tokens live under `core/`.
- Anniversary data remains in the existing `anniversary.db`. Room migration 1→2 adds Todo and Period-owned tables and source identifiers to shared reminders; no destructive migration is configured.
- Todo and Period currently show migration placeholders. Backup, media storage, and app-preference interfaces are contracts only; their product workflows are not implemented.
- The application ID remains `dev.yezi.anniversary` to preserve Android upgrade identity.

See `docs/ARCHITECTURE.md`, `docs/MIGRATION_PLAN.md`, and `docs/FEATURE_PARITY.md` for boundaries and remaining work.

## Build

Requires JDK 17 and Android SDK Platform 35.

```sh
./gradlew test
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads it as a build artifact. Unit tests and APK build run in the `Unit tests and debug APK` job; emulator smoke runs in a separate job.
