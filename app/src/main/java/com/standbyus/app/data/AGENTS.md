# Data layer

## OVERVIEW

3-layer data pipeline: **Supabase REST API** (remote) → **Repository** (orchestration) → **Room cache** (local). Polls every 6s. No realtime.

## STRUCTURE

```
data/
├── remote/           # OkHttp + Supabase REST
│   ├── SupabaseConfig.kt    # URL + anon key (hardcoded)
│   ├── SupabaseService.kt   # CRUD + file upload
│   ├── StickerManager.kt    # Storage upload for album photos
│   └── ThemeRepository.kt   # Emoji theme pack REST client
├── model/            # Data classes
│   ├── UserStatus.kt        # Status entity with toMap/fromMap
│   ├── PairingInfo.kt       # Pair link between 2 devices
│   ├── Feeling.kt           # 16 moods enum (emoji+gradient colors)
│   ├── Doing.kt             # 12 activities enum
│   ├── AlbumPhoto.kt        # Photo metadata
│   ├── CheckinData.kt       # Check-in record data class
│   └── ThemePack.kt         # Emoji theme pack model
├── repository/       # Business logic
│   ├── StatusRepository.kt  # Sync Supabase ↔ Room ↔ Widget cache
│   ├── PairingRepository.kt # Code generation + join logic
│   ├── AlbumRepository.kt   # Photo upload/delete orchestration
│   └── CheckinRepository.kt # Check-in CRUD + sync
└── local/            # Room
    ├── AppDatabase.kt       # Tables: status_cache, checkin_records
    ├── StatusDao.kt         # get/upsert/clearAll
    ├── StatusEntity.kt      # Room entity + extension mappers
    ├── CheckinDao.kt        # Check-in CRUD operations
    └── CheckinRecordEntity.kt # Check-in Room entity
```

## WHERE TO LOOK

| Task | File | Notes |
|------|------|-------|
| Add a new API call | `remote/SupabaseService.kt` | Uses `query/update/create` |
| Change sync strategy | `repository/StatusRepository.kt` | Polling interval, cache flow |
| Add a new data model | `model/` | Add enum or data class |
| Modify pairing logic | `repository/PairingRepository.kt` | 6-char code gen, join validation |
| Add check-in features | `repository/CheckinRepository.kt` | Check-in CRUD |
| Manage album photos | `repository/AlbumRepository.kt` | Upload/delete to Supabase Storage |
| Change emoji themes | `remote/ThemeRepository.kt` | Theme pack REST client |

## CONVENTIONS

- **JSON**: `org.json.JSONObject/JSONArray` + hand-written `toMap()`/`fromMap()` — no Moshi/Kotlinx Serialization
- **HTTP**: OkHttp directly (not Retrofit); one interceptor adds Supabase auth headers
- **No offline-first**: Room is a write-through cache, not source of truth
- **Widget cache**: `StatusRepository` writes to `widget_cache` SharedPreferences on every update
