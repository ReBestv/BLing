# Post Status

Mood/activity picker — publish new status to Supabase.

## STRUCTURE

```
poststatus/
├── PostStatusScreen.kt      # FeelingPicker + DoingPicker + publish button
└── PostStatusViewModel.kt   # Publish logic + field state
```

## NOTES

- Feeling: grid selector (emoji + name)
- Doing: free-text input (max 30 chars), defaults to feeling name if empty
- Publishes to Supabase via StatusRepository
