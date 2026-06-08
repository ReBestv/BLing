# DI layer

Hilt modules providing app-wide dependencies.

## STRUCTURE

```
di/
└── AppModule.kt        # Room database and ThemeRepository provider
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Add a Room dependency | `AppModule.kt` |
| Add an app-wide provider | `AppModule.kt` |

## CONVENTIONS

- `@Module @InstallIn(SingletonComponent::class)`
- All providers are `@Singleton`
