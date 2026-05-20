# Check-in

Daily check-in form with notes and history view.

## STRUCTURE

```
checkin/
├── CheckinScreen.kt        # Check-in form + past records list
└── CheckinViewModel.kt     # Check-in state + repository calls
```

## NOTES

- Daily check-in with optional notes
- Displays past check-in records in a list
- Data persisted in Room (`checkin_records` table)
