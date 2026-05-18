# DI layer

Hilt modules providing app-wide dependencies.

## STRUCTURE

```
di/
├── AppModule.kt        # Room database provider
└── SupabaseModule.kt   # OkHttpClient + SupabaseConfig provider
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Add a Room dependency | `AppModule.kt` |
| Configure HTTP client | `SupabaseModule.kt` |

## CONVENTIONS

- `@Module @InstallIn(SingletonComponent::class)`
- All providers are `@Singleton`
