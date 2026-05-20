# Repository layer

Orchestrates remote ↔ local sync and exposes data to ViewModels.

## STRUCTURE

```
repository/
├── StatusRepository.kt     # Status CRUD + polling + widget cache
├── PairingRepository.kt    # Code generation + pair/join/unpair
├── AlbumRepository.kt      # Photo upload/delete to Supabase Storage
└── CheckinRepository.kt    # Check-in CRUD + sync
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Change polling interval | `StatusRepository.kt` |
| Modify pairing logic | `PairingRepository.kt` |
| Add widget cache field | `StatusRepository.updateWidgetCache()` |
| Album photo management | `AlbumRepository.kt` |
| Check-in features | `CheckinRepository.kt` |

## CONVENTIONS

- Polling via `callbackFlow` every 6s
- Widget cache via SharedPreferences
