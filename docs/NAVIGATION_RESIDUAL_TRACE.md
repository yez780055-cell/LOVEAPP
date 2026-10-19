# Navigation residue investigation notes

This note records the current implementation for the next device-level investigation. The residual-frame issue has **not** been treated as fixed by this change.

## Current implementation

- `AppShell` in `app/src/main/java/dev/yezi/anniversary/app/AppShell.kt` owns one `rememberNavController()` and a Material 3 `Scaffold`.
- The `NavHost` is inside `Scaffold` content and receives the scaffold `padding` modifier plus an opaque `AppColors.background`.
- The `NavHost` graph contains nested navigation graphs for Todo, Period, and Anniversary. Each tab click navigates to the graph route, pops to `findStartDestination()` with `saveState = true`, and uses `launchSingleTop = true` and `restoreState = true`.
- `enterTransition`, `exitTransition`, `popEnterTransition`, and `popExitTransition` all return `EnterTransition.None` / `ExitTransition.None`.
- The scaffold's `containerColor` and the `NavHost` modifier background are set to `AppColors.background`.
- Todo draws its own full-size `Box` with `MaterialTheme.colorScheme.background`; Period and Anniversary use their own route/screen composables.
- The current smoke test exercises fast tab switching but does not capture frames or assert that a previous destination is absent from rendered pixels.

## Evidence still needed

The reported residue is still reproducible on a real device after the prior transition/background change. This repo currently has no frame capture, screenshot comparison, or pixel-level assertion for tab changes. Emulator instrumentation can assert navigation destinations and stability, but that alone cannot rule out one-frame visual residue. A focused follow-up should record consecutive emulator/device frames during rapid Todo → Period → Anniversary switches and inspect whether old destination pixels persist after the new destination has settled.
