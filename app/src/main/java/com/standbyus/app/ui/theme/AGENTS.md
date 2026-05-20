# Theme

Design system — colors, typography, emoji themes, mood colors.

## STRUCTURE

```
theme/
├── Color.kt              # Coral palette (light + dark)
├── Theme.kt              # Material3 light/dark color schemes
├── Type.kt               # Typography scale
├── EmotionColors.kt      # Feeling → background color mapping
├── EmojiTheme.kt         # Emoji set switching (default/cat)
├── NavIcons.kt           # Bottom nav icon composables
└── StandByUsTokens.kt    # Design token constants (spacing, radius, etc.)
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Change brand colors | `Color.kt`, `Theme.kt` |
| Add emoji theme | `EmojiTheme.kt` |
| Modify mood colors | `EmotionColors.kt` |
| Adjust fonts | `Type.kt` |
| Change nav icons | `NavIcons.kt` |
| Tweak design tokens | `StandByUsTokens.kt` |
