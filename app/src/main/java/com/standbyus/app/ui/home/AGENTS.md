# Home

Main dashboard — partner status card + self status + quick-post FAB.

## STRUCTURE

```
home/
├── HomeScreen.kt         # StatusCard × 2 + FAB + bottom nav
└── HomeViewModel.kt      # Polling + partner resolution
```

## NOTES

- Polls every 6s via `callbackFlow`
- Shows both partner's and own status
- FAB navigates to PostStatusScreen
