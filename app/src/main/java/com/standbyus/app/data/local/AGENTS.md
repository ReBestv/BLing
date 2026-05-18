# Local database

Room persistence layer — write-through cache for status data.

## STRUCTURE

```
local/
├── AppDatabase.kt       # Room database (single table)
├── StatusDao.kt         # get/upsert/clearAll
└── StatusEntity.kt      # Room entity + ↔ UserStatus converters
```

## NOTES

- `fallbackToDestructiveMigration()` — schema changes wipe data
- Not source of truth; used as cache layer
