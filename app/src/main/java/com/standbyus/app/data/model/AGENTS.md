# Data models

Domain data classes and enums.

## STRUCTURE

```
model/
├── UserStatus.kt          # Core status data class
├── PairingInfo.kt         # Pair link data class
├── Feeling.kt             # 16 moods (displayName, emoji, colors)
├── Doing.kt               # 12 activities enum
└── AlbumPhoto.kt          # Photo metadata
```

## KEY TYPES

| Model | Fields | Used by |
|-------|--------|---------|
| `UserStatus` | doing, customDoing, feeling, feelingColor, feelingEmoji, note | All layers |
| `Feeling` | HAPPY, SAD, TIRED, SICK, RELAXED, MISSING, SLEEPING, HUSTLING, THINKING, KISS, ANGRY, ANXIOUS, WATCHING, UPSET, LOVE, BORED | PostStatus, Home |
| `Doing` | WORKING, OVERTIME, STUDYING, SLEEPING, EATING, EXERCISING, COMMUTING, MOVIE, GAMING, DAYDREAMING, BOARD_GAME, CUSTOM | PostStatus |
