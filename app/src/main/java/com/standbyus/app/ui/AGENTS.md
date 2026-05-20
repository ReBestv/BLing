# UI layer

## OVERVIEW

Single-activity architecture. Jetpack Compose + Material3 with warm coral color scheme. 6 screens via NavHost. Simplified Chinese throughout.

## STRUCTURE

```
ui/
├── home/              # Partner status + quick-post button
│   ├── HomeScreen.kt         # StatusCard × 2 + FAB
│   └── HomeViewModel.kt      # Polling + partner resolution
├── poststatus/        # Mood/activity picker
│   ├── PostStatusScreen.kt   # FeelingPicker + DoingPicker
│   └── PostStatusViewModel.kt
├── history/           # Status timeline
│   ├── HistoryScreen.kt      # Time-ordered list
│   └── HistoryViewModel.kt
├── album/             # Photo album (Supabase Storage)
│   ├── AlbumScreen.kt        # Grid + photo picker
│   └── AlbumViewModel.kt
├── settings/          # Pairing + unlink
│   ├── SettingsScreen.kt     # Generate/join code
│   └── SettingsViewModel.kt
├── checkin/           # Daily check-in with notes
│   ├── CheckinScreen.kt      # Check-in form + history
│   └── CheckinViewModel.kt
├── celebration/       # Date-based overlay
│   ├── CelebrationConfig.kt  # 3 dates (birthday, xmas, valentine)
│   ├── CelebrationDay.kt     # Data class
│   └── CelebrationOverlay.kt # Fullscreen particle animation
├── components/        # Reusable composables
│   ├── StatusCard.kt         # Gradient card for status display
│   ├── FeelingPicker.kt      # Horizontal scroll mood selector
│   ├── DoingPicker.kt        # Chips + custom text input
│   ├── AvatarWithGlow.kt     # Breathing glow animation
│   └── AppHeader.kt          # Title bar with back button
└── theme/             # Design system
    ├── Color.kt              # Coral palette + dark mode
    ├── Theme.kt              # Light/dark color schemes
    ├── Type.kt               # Typography scale
    ├── EmotionColors.kt      # Mood→Color mapping function (16 feelings)
    ├── EmojiTheme.kt         # Emoji set switching (default/cat)
    ├── NavIcons.kt           # Bottom nav icon composables
    └── StandByUsTokens.kt    # Design token constants
```

## WHERE TO LOOK

| Task | File | Notes |
|------|------|-------|
| Add a screen | All: `Screen.kt` + `ViewModel.kt` | Register in `MainActivity.kt` NavHost |
| Change a picker | `components/` | FeelingPicker, DoingPicker |
| Modify theme | `theme/Color.kt`, `Theme.kt` | Light + dark defined |
| Add celebration | `celebration/CelebrationConfig.kt` | Date + emoji + message |

## CONVENTIONS

- **Screen pattern**: Each screen = `@Composable fun ScreenName(onBack, viewModel = hiltViewModel())`
- **ViewModel pattern**: `@HiltViewModel`, `@Inject constructor`, `StateFlow` exposed
- **NavHost config**: Single `MainActivity.kt` — `composable(Routes.X) { ScreenName(...) }`
- **Icons**: Extended Material icons (`material-icons-extended`)
- **Theme**: Warm coral — Primary `#FF7E67`, background `#FFF8F5`, dark bg `#1A1412`
- **Bottom nav**: Home / Album / History / Settings — visibility toggles on main routes
