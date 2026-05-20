# Local database

Room persistence layer — write-through cache for status data.

## STRUCTURE

```
local/
├── AppDatabase.kt          # Room database (status_cache + checkin_records)
├── StatusDao.kt            # get/upsert/clearAll
├── StatusEntity.kt         # Room entity + ↔ UserStatus converters
├── CheckinDao.kt           # Check-in CRUD operations
└── CheckinRecordEntity.kt  # Check-in Room entity
```

## NOTES

- `fallbackToDestructiveMigration()` — schema changes wipe data
- Not source of truth; used as cache layer
