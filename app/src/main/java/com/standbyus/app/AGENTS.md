# App root (`com.standbyus.app`)

Application entry, MainActivity, and top-level wiring.

## STRUCTURE

```
app/
├── StandByApplication.kt   # @HiltAndroidApp, eager init
├── MainActivity.kt         # NavHost + BottomNavigation + celebration overlay
├── BottomNavItem.kt        # Navigation items enum
├── data/                   # Remote → Repository → Room
├── di/                     # Hilt DI modules
├── navigation/             # Route constants
├── ui/                     # Screens + components + theme
└── widget/                 # Glance AppWidget
```

- 16 feelings (开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考/亲亲/生气/焦虑/看剧/委屈/爱心/无聊)

## CONVENTIONS

- Single Activity, all navigation via Compose NavHost
- Hilt for DI (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- Device UUID from SharedPreferences for identity
- Feeling enum: 9→16 (added 亲亲, 生气, 焦虑, 看剧, 委屈, 爱心, 无聊)
