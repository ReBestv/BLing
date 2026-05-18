# Widget

Glance AppWidget showing partner's latest status with click-to-open.

## STRUCTURE

```
widget/
├── StandByWidget.kt        # Glance composable widget
└── StandByWidgetReceiver.kt # BroadcastReceiver for updates
```

## NOTES

- Reads from `widget_cache` SharedPreferences
- Shows partner's feeling emoji + name + doing text
- Click to open MainActivity
- Auto-refreshes when partner status changes (6s polling)
- Shows placeholder text when no partner data yet

## DATA FLOW

```
partner publishes → Supabase → observeStatus(poll 6s, updateWidget=true)
  → updateWidgetCache() → widget_cache SharedPreferences
  → StandByWidget().updateAll() → Glance recomposes
```
