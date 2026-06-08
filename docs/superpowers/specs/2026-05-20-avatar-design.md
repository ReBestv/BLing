# Avatar Feature Design

**Date**: 2026-05-20 | **Status**: Approved | **Scope**: Single implementation plan

## Overview

Add emoji-based avatar selection to the StandByUs app. Each user chooses an emoji avatar during pairing (or later in Settings). Avatars display on the History (时光轴) timeline entries and as a personal card at the top of Settings (设置).

## Design Decisions

### Avatar Type
- **Emoji only** — no photo upload. A curated set of ~26 emojis across 4 categories (🐾动物, 👻趣味, ✨治愈, 😎酷).
- Distinct from Feeling enum emojis — avatars are personal identity markers, not mood indicators.

### Selection Timing
- **During pairing flow**: Both parties choose when pairing — creator on code generation, joiner on code join.
- **Post-pairing**: Editable in Settings page.

### Placement
| Screen | Placement | Behavior |
|--------|-----------|----------|
| **History (时光轴)** | Inline — avatar replaces the emoji dot in each timeline entry | Own entries: show selected avatar. Partner entries: keep feeling-based emoji dot. |
| **Settings (设置)** | Top section card — personal profile card | Shows avatar + self-name + "Tap to change avatar" affordance. Tapping opens avatar picker. |

### Visibility
- **Shown on**: History (own entries only), Settings (always)
- **Not shown on**: Home, Album, Checkin, PostStatus

### Storage
- **SharedPreferences only** — key `avatar_emoji` in `pairing` prefs.
- **No Supabase sync** — avatars are personal choices, not shared state.
- **No Room table** — single scalar value, no querying needed.

## Data Model

```
SharedPreferences key: "avatar_emoji" (String, default: "🐱")
Location: pairing SharedPreferences (existing)
```

## UI Components

### 1. AvatarPicker composable (new)
- Grid of emoji options, grouped by category
- Highlight currently selected emoji
- Callback: `onAvatarSelected: (String) -> Unit`
- File: `ui/components/AvatarPicker.kt`

### 2. SettingsScreen modification
- Add "个人头像" section card at top (before "配对状态"), always visible
- Shows current avatar (64dp circle with glow) + self-name
- Tap opens AvatarPicker in a dialog/bottom sheet
- File: `ui/settings/SettingsScreen.kt`

### 3. HistoryScreen modification
- In TimelineEntry: replace `StatusEmojiImage` in the dot with avatar emoji when `isMe == true`
- Partner entries unchanged (keep feeling-based dot)
- `myAvatar` exposed from HistoryViewModel
- Files: `ui/history/HistoryScreen.kt`, `ui/history/HistoryViewModel.kt`

### 4. Pairing flow modification
- Create code flow: add avatar picker step between name input and "生成配对码" button
- Join flow: add avatar picker step between name input/join code and "连接" button
- Save avatar to SharedPreferences on pairing success
- File: `ui/settings/SettingsScreen.kt`

## Files Changed

| File | Change | Impact |
|------|--------|--------|
| `ui/components/AvatarPicker.kt` | **NEW** — emoji grid composable | New component |
| `ui/components/AvatarWithGlow.kt` | Modify — accept generic emoji String (not just Feeling) | Low |
| `ui/settings/SettingsScreen.kt` | Add personal card + avatar picker in pairing flow + edit | Medium |
| `ui/settings/SettingsViewModel.kt` | Add avatar state + save/load from SharedPreferences | Medium |
| `ui/history/HistoryScreen.kt` | Modify TimelineEntry dot for own entries | Low |
| `ui/history/HistoryViewModel.kt` | Add myAvatar state from SharedPreferences | Low |

## Edge Cases
- **Default**: `🐱` (猫咪) if no avatar set
- **Un-pair**: Clear `avatar_emoji` on unpair
- **Empty state**: Always show default avatar, never empty
