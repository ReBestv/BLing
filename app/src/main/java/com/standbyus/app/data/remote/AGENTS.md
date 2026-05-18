# Remote layer

Supabase REST API client — raw OkHttp, no SDK.

## STRUCTURE

```
remote/
├── SupabaseConfig.kt     # Base URL + anon key
├── SupabaseService.kt    # CRUD operations + file upload
└── StickerManager.kt     # Feeling sticker image uploads
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Add API endpoint | `SupabaseService.kt` |
| Change auth | `SupabaseConfig.kt` |
| Upload photos | `StickerManager.kt` |

## CONVENTIONS

- OkHttp directly (no Retrofit)
- JSON: `org.json.JSONObject/JSONArray`
- Auth: custom interceptor adds `apikey` header
