# History

Status timeline screen — time-ordered list of past statuses.

## STRUCTURE

```
history/
├── HistoryScreen.kt       # Scrollable timeline list
└── HistoryViewModel.kt    # Load history from Supabase
```

## NOTES

- Fetches from Supabase via StatusRepository
- Time-ordered descending
