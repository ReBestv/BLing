# Celebration

Date-based overlay animations — shown once per day.

## STRUCTURE

```
celebration/
├── CelebrationConfig.kt      # 3 predefined dates + lookup
├── CelebrationDay.kt         # Data class (date, emoji, message)
└── CelebrationOverlay.kt     # Fullscreen particle animation
```

## CELEBRATION DATES

| Date | Event |
|------|-------|
| 02/14 | Valentine's Day |
| 12/25 | Christmas |
| MM/dd | Partner's birthday |

## NOTES

- Shown once/day via SharedPreferences flag
- Rendered as overlay in MainActivity
