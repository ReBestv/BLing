# Theme Snapshot Keyed Status Design

## Summary

Status moods should stop using Chinese display text as the system identity. The app will use stable English keys internally, while each emoji theme pack can provide its own Chinese label and image for the same key.

Example:

- Internal key: `upset`
- Xiaoxin label: `沮丧`
- Another theme label: `低电量`
- Another theme label: `emo了`

All three labels can represent the same mood key. The app logic, database fields, image filenames, and URL paths use the stable key and English asset filename. Chinese text remains display content only.

## User-Facing Behavior

When a user publishes a status, the app saves the theme presentation used at publish time. The receiver sees the sender's published theme snapshot, not the receiver's current theme.

If Alice publishes with Xiaoxin:

```text
themeId = xiaoxin
themeName = 小新
feelingKey = upset
feelingLabel = 沮丧
feelingAsset = https://.../themes/xiaoxin/upset.png
```

Bob sees Xiaoxin's `upset.png` and the label `沮丧`, even if Bob has selected another theme.

## Why This Design

Chinese labels are good for people, but fragile as system identifiers. They are awkward in filenames, URL paths, database logic, and theme-to-theme mapping. English keys make the model stable, while theme labels keep the UI flexible.

Saving a publish-time snapshot keeps historical statuses stable. If a theme pack later changes its label or image, old statuses continue to show what the sender actually published at the time.

## Theme Manifest Format

Theme packs define moods as objects instead of raw Chinese strings:

```json
{
  "version": 2,
  "themes": [
    {
      "id": "xiaoxin",
      "name": "小新",
      "bucket": "xiaoxin",
      "icon": "happy.png",
      "feelings": [
        {
          "key": "happy",
          "label": "开心",
          "asset": "happy.png"
        },
        {
          "key": "missing",
          "label": "想你",
          "asset": "missyou.png"
        },
        {
          "key": "upset",
          "label": "沮丧",
          "asset": "upset.png"
        }
      ]
    }
  ]
}
```

Rules:

- `key` is required and stable.
- `label` is required and theme-specific.
- `asset` is optional for default/text-only themes, but required for image themes.
- Asset filenames use ASCII-safe names.
- Theme `id` and `bucket` use ASCII-safe names.

## Status Data Model

Replace the old status mood fields:

```text
feeling
feelingEmoji
```

with:

```text
themeId
themeName
feelingKey
feelingLabel
feelingAsset
feelingFallbackEmoji
feelingColor
```

`feelingColor` can stay because screens already use mood colors. It should be derived from `feelingKey`, not from `feelingLabel`.

`feelingAsset` stores the resolved publish-time URL or emoji/text display value. Displays should use it directly.

## Core Types

Add a stable mood definition for built-in keys:

```kotlin
data class FeelingDefinition(
    val key: String,
    val defaultLabel: String,
    val fallbackEmoji: String,
    val color: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)
```

`Feeling` can either be migrated from an enum with Chinese display names to an enum with English keys, or replaced by a registry object. The important requirement is that lookups use `key`, not Chinese label.

Theme-specific item:

```kotlin
data class ThemeFeeling(
    val key: String,
    val label: String,
    val asset: String?
)
```

Publish snapshot:

```kotlin
data class StatusFeelingSnapshot(
    val themeId: String,
    val themeName: String,
    val feelingKey: String,
    val feelingLabel: String,
    val feelingAsset: String,
    val feelingFallbackEmoji: String,
    val feelingColor: String
)
```

## Publish Flow

The post status screen should select a `ThemeFeeling`, not a raw `Feeling.displayName`.

When publishing:

1. Get the current theme.
2. Get the selected theme feeling item by `key`.
3. Resolve the asset URL from `bucket + asset`.
4. Resolve fallback emoji and color from the built-in key registry.
5. Save the snapshot fields into `UserStatus`.

If the selected theme does not define an item for the key, the app falls back to the built-in default label and emoji.

## Display Flow

All display surfaces use the saved snapshot:

- Home status card
- Partner status card
- History timeline
- Widget cache
- Room cache

Display priority:

1. If `feelingAsset` is a remote image URL, render image.
2. Else if `feelingAsset` is non-empty text, render it.
3. Else render `feelingFallbackEmoji`.
4. Label uses `feelingLabel`.

The receiver's current selected theme is not consulted when displaying another user's saved status.

## Migration Strategy

This is a breaking model cleanup, so old fields should be removed from app code after replacement.

Room:

- Bump database version.
- Existing `fallbackToDestructiveMigration()` means local status cache can be wiped safely.
- Replace `StatusEntity.feeling` and `StatusEntity.feelingEmoji` with the new snapshot fields.

Supabase:

- The `statuses` table needs new columns for the snapshot fields.
- Existing old rows will not have those fields. The app should still parse them temporarily with a fallback adapter during rollout.
- After rollout, new publishes write only the new fields.

Fallback for old remote rows:

- If `feelingKey` is missing, infer from old Chinese `feeling` using a small legacy map.
- If inference fails, use `happy`.
- If `feelingLabel` is missing, use old `feeling` or built-in default label.
- If `feelingAsset` is missing, use old `feelingEmoji` or fallback emoji.

## Legacy Label Map

The app needs a temporary map for old rows:

```text
开心 -> happy
想你 -> missing
亲亲 -> kiss
爱你 -> love
悠闲 -> relaxed
奋斗 -> hustling
思考 -> thinking
追剧 -> watching
难过 -> sad
沮丧 -> upset
生气 -> angry
焦虑 -> anxious
疲惫 -> tired
生病 -> sick
无聊 -> bored
哭哭 -> crying
睡觉 -> sleeping
```

This map is only for reading old data. New writes use keys directly.

## Testing

Add focused unit tests for:

- Manifest parsing from version 2 object items.
- Theme item lookup by key.
- Asset URL resolution with ASCII filenames.
- Publishing snapshot creation.
- Old-row fallback from Chinese `feeling` to new keyed status.
- Display fallback for image URL, text emoji, and missing asset.

## Open Implementation Notes

The current app has several fields named `feeling` across UI, Room, widget cache, and Supabase serialization. During implementation, rename at boundaries carefully:

- `feelingKey` for logic.
- `feelingLabel` for text shown to users.
- `feelingAsset` for image URL or emoji value.

Avoid adding another parallel display-name API. The whole purpose of this change is to stop treating display text as the identifier.
