# Theme

Design system — colors, typography, emoji themes, mood colors.

## STRUCTURE

```
theme/
├── Color.kt              # Coral palette (light + dark)
├── Theme.kt              # Material3 light/dark color schemes
├── Type.kt               # Typography scale
├── EmotionColors.kt      # Feeling → background color mapping
└── EmojiTheme.kt         # Emoji set switching (default/cat)
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Change brand colors | `Color.kt`, `Theme.kt` |
| Add emoji theme | `EmojiTheme.kt` |
| Modify mood colors | `EmotionColors.kt` |
| Adjust fonts | `Type.kt` |
