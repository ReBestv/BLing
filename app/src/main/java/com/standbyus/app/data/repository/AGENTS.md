# Repository layer

Orchestrates remote ↔ local sync and exposes data to ViewModels.

## STRUCTURE

```
repository/
├── StatusRepository.kt     # Status CRUD + polling + widget cache
└── PairingRepository.kt    # Code generation + pair/join/unpair
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Change polling interval | `StatusRepository.kt` |
| Modify pairing logic | `PairingRepository.kt` |
| Add widget cache field | `StatusRepository.updateWidgetCache()` |

## CONVENTIONS

- Polling via `callbackFlow` every 6s
- Widget cache via SharedPreferences
