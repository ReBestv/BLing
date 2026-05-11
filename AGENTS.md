# StandByUs

Couples status-sharing Android app. Kotlin + Jetpack Compose + Hilt.

## Build & run

```
./gradlew assembleDebug
```

No custom Gradle tasks. Open in Android Studio and run on device/emulator (minSdk 31).

## Architecture

Single `:app` module. 4 Compose navigation destinations: `home`, `post_status`, `history`, `settings`.

**Entrypoints:**
- `StandByApplication` (`app/.../StandByApplication.kt`) — `@HiltAndroidApp`, eagerly initializes `SupabaseService`
- `MainActivity` (`app/.../MainActivity.kt`) — `@AndroidEntryPoint`, sets up NavHost + celebration overlay
- `StandByWidgetReceiver` / `StandByWidget` — Glance app widget

## Identity

**No user accounts.** Device-based: UUID stored in `supabase_device` SharedPreferences serves as the "userId". Two devices pair by exchanging a 6-character alphanumeric code (no auth).

## Data layer

### Supabase (raw HTTP, no SDK)
- `SupabaseService` uses OkHttp directly against Supabase REST API (hand-written JSON serialization, no Supabase Kotlin SDK).
- Tables: `statuses` (per-user status) and `pairs` (pairing links).
- **No real-time subscriptions.** Status updates use polling every 6 seconds via `callbackFlow`.

### Room
- `AppDatabase` with single `status_cache` table.
- `fallbackToDestructiveMigration()` — schema changes wipe data.

### Widget cache
- `widget_cache` SharedPreferences written on each status update; the Glance widget reads from there.

## Key conventions

- UI language: **Simplified Chinese** throughout (Feeling/Doing enum display names, all screen text).
- `Feeling` enum: 9 values (开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考).
- `Doing` enum: 12 values (搬砖/加班/学习/睡觉/干饭/运动/通勤/看电影/玩游戏/发呆/桌游/自定义). Custom doing capped at 20 chars.
- Celebration overlay: date-based (MM/dd), shown once per day via `celebration` SharedPreferences. Configured in `CelebrationConfig`.

## Testing

No tests found in the repo.

## Firebase

`FIREBASE_SETUP.md` exists but **no Firebase SDK is declared** in `build.gradle.kts`. The app uses Supabase, not Firebase.
