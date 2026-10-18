# Architecture and implementation status

## Structure

Single Android application module; packages follow feature/core responsibilities so the project can split modules later without introducing premature Gradle complexity.

- `domain/DateEngine.kt`: pure `LocalDate` date math and recurrence calculations.
- `data/`: Room entities, DAO, database singleton and repository.
- `ui/`: lifecycle-scoped ViewModel and Compose navigation/screens.
- `notification/`: `ReminderScheduler`, receiver, boot/date/time/timezone rescheduling.
- `widget/`: single-event AppWidget provider and Room-backed refresh.

Room stores events, categories, reminders, image metadata and milestone records. `targetDate` is ISO-8601 local calendar date; event `timezone` is an IANA ZoneId; timestamps are epoch milliseconds. Date-only arithmetic intentionally avoids converting local days to fixed 86,400,000 ms intervals.

## Current implementation

Implemented in source: new-event creation, edit, detail, soft-delete to trash timestamp, active-event list, count-up/count-down presentation, same-day inclusion for elapsed counting, annual repeat including Feb 29 → Feb 28 in non-leap years, Room persistence, optional one-time/yearly reminder, notification channel/permission request, exact-alarm permission settings entry, reboot/time/date/timezone rescheduling, and a minimal first-active-event home-screen widget.

Not implemented yet: category editing/selection, lunar calendar, custom recurrence editor, milestones/history UI, date calculator UI, Photo Picker/crop/save, multi-event widgets, backup/restore UI and transactional archive format. See `REVERSE_GAPS.md` for behavior unknowns and build constraints.

## Reminder flow

`EventEditor → EventViewModel → EventRepository → ReminderScheduler → AlarmManager → ReminderReceiver → Notification`. Event save replaces the stable reminder PendingIntent; delete cancels it. Boot and clock/zone/date changes re-query enabled reminders and schedule them again. On Android 12+ exact alarms may be denied: the scheduler falls back to an inexact idle-allowed alarm. On Android 13+ notification delivery requires `POST_NOTIFICATIONS`.

## Widget flow

`AppWidgetProvider → Room active-event Flow → RemoteViews`. It currently selects the first event from the repository's active ordering; tap opens that event's detail route. Data saves/deletes and system date/zone events trigger refresh. Configuration UI, per-widget event selection, list/grid widget and midnight scheduling need follow-up.

## Date semantics

All event dates are parsed to `LocalDate`. `ChronoUnit.DAYS` measures calendar-date intervals independent of the current UTC offset. `includeDueDate` currently adds the event date to elapsed (`countUp`) count; future countdown remains the number of dates until the target. Yearly Feb 29 events map to Feb 28 in a non-leap year (matching the supplied project prompt). These semantics are explicit in `DateEngineTest` and can be adjusted if later device evidence contradicts them.

## Build

Requires JDK 17, Android SDK platform 35 and a compatible Gradle installation (AGP 8.7.3). Run `gradle :app:testDebugUnitTest :app:assembleDebug`; expected APK path: `app/build/outputs/apk/debug/app-debug.apk`. This workspace currently has Java 17 but lacks Gradle, Android SDK, ADB and emulator, and network access to Gradle distribution timed out; no APK build or runtime install is claimed.
