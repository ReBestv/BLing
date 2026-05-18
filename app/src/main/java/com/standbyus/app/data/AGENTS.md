# Data layer

## OVERVIEW

3-layer data pipeline: **Supabase REST API** (remote) → **Repository** (orchestration) → **Room cache** (local). Polls every 6s. No realtime.

## STRUCTURE

```
data/
├── remote/           # OkHttp + Supabase REST
│   ├── SupabaseConfig.kt    # URL + anon key (hardcoded)
│   ├── SupabaseService.kt   # CRUD + file upload
│   └── StickerManager.kt    # Storage upload for album photos
├── model/            # Data classes
│   ├── UserStatus.kt        # Status entity with toMap/fromMap
│   ├── PairingInfo.kt       # Pair link between 2 devices
│   ├── Feeling.kt           # 9 moods enum (emoji+gradient colors)
│   ├── Doing.kt             # 12 activities enum
│   └── AlbumPhoto.kt        # Photo metadata
├── repository/       # Business logic
│   ├── StatusRepository.kt  # Sync Supabase ↔ Room ↔ Widget cache
│   └── PairingRepository.kt # Code generation + join logic
└── local/            # Room
    ├── AppDatabase.kt       # Single table: status_cache
    ├── StatusDao.kt         # get/upsert/clearAll
    └── StatusEntity.kt      # Room entity + extension mappers
```

## WHERE TO LOOK

| Task | File | Notes |
|------|------|-------|
| Add a new API call | `remote/SupabaseService.kt` | Uses `query/update/create` |
| Change sync strategy | `repository/StatusRepository.kt` | Polling interval, cache flow |
| Add a new data model | `model/` | Add enum or data class |
| Modify pairing logic | `repository/PairingRepository.kt` | 6-char code gen, join validation |

## CONVENTIONS

- **JSON**: `org.json.JSONObject/JSONArray` + hand-written `toMap()`/`fromMap()` — no Moshi/Kotlinx Serialization
- **HTTP**: OkHttp directly (not Retrofit); one interceptor adds Supabase auth headers
- **No offline-first**: Room is a write-through cache, not source of truth
- **Widget cache**: `StatusRepository` writes to `widget_cache` SharedPreferences on every update
