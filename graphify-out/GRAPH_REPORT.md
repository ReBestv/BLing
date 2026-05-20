# Graph Report - .  (2026-05-19)

## Corpus Check
- Large corpus: 121 files · ~1,131,717 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder, or use --no-semantic to run AST-only.

## Summary
- 465 nodes · 544 edges · 64 communities (31 shown, 33 thin omitted)
- Extraction: 85% EXTRACTED · 14% INFERRED · 1% AMBIGUOUS · INFERRED: 75 edges (avg confidence: 0.79)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Core Infrastructure + Layers|Core Infrastructure + Layers]]
- [[_COMMUNITY_Screen Composables + Bottom Nav|Screen Composables + Bottom Nav]]
- [[_COMMUNITY_Status Flow Entity → Widget|Status Flow: Entity → Widget]]
- [[_COMMUNITY_Checkin Data Flow Entity → Repository|Checkin Data Flow: Entity → Repository]]
- [[_COMMUNITY_Emoji Theme + Enums + Home Session|Emoji Theme + Enums + Home Session]]
- [[_COMMUNITY_Icons + Feature Navigation|Icons + Feature Navigation]]
- [[_COMMUNITY_PairingAlbumCelebration Features|Pairing/Album/Celebration Features]]
- [[_COMMUNITY_Core Data Layer Repositories + DB + Supabase|Core Data Layer: Repositories + DB + Supabase]]
- [[_COMMUNITY_Theme System Colors + Typography + Modes|Theme System: Colors + Typography + Modes]]
- [[_COMMUNITY_Project Docs + Build Config + Widget|Project Docs + Build Config + Widget]]
- [[_COMMUNITY_Checkin + Album Specs & Plans|Checkin + Album Specs & Plans]]
- [[_COMMUNITY_SupabaseService Methods|SupabaseService Methods]]
- [[_COMMUNITY_SettingsViewModel Methods|SettingsViewModel Methods]]
- [[_COMMUNITY_CheckinDao Methods|CheckinDao Methods]]
- [[_COMMUNITY_StandByUs Design Tokens|StandByUs Design Tokens]]
- [[_COMMUNITY_AlbumRepository + AlbumPhoto Model|AlbumRepository + AlbumPhoto Model]]
- [[_COMMUNITY_PairingRepository Methods|PairingRepository Methods]]
- [[_COMMUNITY_AlbumViewModel Methods|AlbumViewModel Methods]]
- [[_COMMUNITY_CheckinViewModel + PK Stats|CheckinViewModel + PK Stats]]
- [[_COMMUNITY_HomeScreen Composables|HomeScreen Composables]]
- [[_COMMUNITY_EmojiThemeManager|EmojiThemeManager]]
- [[_COMMUNITY_StatusDao Methods|StatusDao Methods]]
- [[_COMMUNITY_AppModule (Hilt DI)|AppModule (Hilt DI)]]
- [[_COMMUNITY_StandByWidgetReceiver|StandByWidgetReceiver]]
- [[_COMMUNITY_Celebration Feature Docs + Code|Celebration Feature Docs + Code]]
- [[_COMMUNITY_Shinchan Theme HappyCryFightKiss|Shinchan Theme: Happy/Cry/Fight/Kiss]]
- [[_COMMUNITY_XiaoXin Theme AnxiousBoredLeisurely|XiaoXin Theme: Anxious/Bored/Leisurely]]
- [[_COMMUNITY_XiaoXin Theme AngrySickTiredSleep|XiaoXin Theme: Angry/Sick/Tired/Sleep]]
- [[_COMMUNITY_AppDatabase (Room)|AppDatabase (Room)]]
- [[_COMMUNITY_PairingInfo Model|PairingInfo Model]]
- [[_COMMUNITY_StickerManager (Upload)|StickerManager (Upload)]]
- [[_COMMUNITY_HistoryViewModel|HistoryViewModel]]
- [[_COMMUNITY_FeelingPicker Component|FeelingPicker Component]]
- [[_COMMUNITY_HomeViewModel|HomeViewModel]]
- [[_COMMUNITY_Build System (Gradle)|Build System (Gradle)]]
- [[_COMMUNITY_Architecture Docs (PollingSupabase)|Architecture Docs (Polling/Supabase)]]
- [[_COMMUNITY_OpenAgent Plugin Sessions|OpenAgent Plugin Sessions]]
- [[_COMMUNITY_Doing Enum|Doing Enum]]
- [[_COMMUNITY_Feeling Enum|Feeling Enum]]
- [[_COMMUNITY_SupabaseConfig|SupabaseConfig]]
- [[_COMMUNITY_SupabaseModule (Hilt DI)|SupabaseModule (Hilt DI)]]
- [[_COMMUNITY_Navigation Routes|Navigation Routes]]
- [[_COMMUNITY_CelebrationConfig|CelebrationConfig]]
- [[_COMMUNITY_NavIcons|NavIcons]]
- [[_COMMUNITY_MainActivity + Celebration Overlay|MainActivity + Celebration Overlay]]
- [[_COMMUNITY_Doing + Feeling (Isolated)|Doing + Feeling (Isolated)]]
- [[_COMMUNITY_Shinchan Theme LoveDrama|Shinchan Theme: Love/Drama]]
- [[_COMMUNITY_SupabaseModule (Isolated)|SupabaseModule (Isolated)]]
- [[_COMMUNITY_Routes (Isolated)|Routes (Isolated)]]
- [[_COMMUNITY_AGENTS.md Conventions|AGENTS.md Conventions]]
- [[_COMMUNITY_BottomNavItem Enum|BottomNavItem Enum]]
- [[_COMMUNITY_System Task Failure Session|System Task Failure Session]]
- [[_COMMUNITY_Commit Code Session|Commit Code Session]]
- [[_COMMUNITY_Graphify Auto Invoke Session|Graphify Auto Invoke Session]]
- [[_COMMUNITY_Celebration Preview HTML|Celebration Preview HTML]]

## God Nodes (most connected - your core abstractions)
1. `CheckinRepository` - 13 edges
2. `SupabaseService` - 12 edges
3. `SettingsViewModel` - 12 edges
4. `CheckinDao` - 11 edges
5. `StatusRepository` - 10 edges
6. `UI Layer` - 9 edges
7. `AlbumViewModel` - 8 edges
8. `CheckinScreen()` - 8 edges
9. `SettingsScreen()` - 8 edges
10. `StatusRepository` - 8 edges

## Surprising Connections (you probably didn't know these)
- `ic-sun.svg — Sun Icon (circle with 8 radiating rays)` --represents--> `Feeling Enum (9 values: 开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考)`  [AMBIGUOUS]
  NewUI/icons/ic-sun.svg → data/model/Feeling.kt
- `ic-ruler.svg — Ruler/Pencil Icon (angled measuring tool)` --represents--> `Custom Doing Text Input (max 20 chars)`  [AMBIGUOUS]
  NewUI/icons/ic-ruler.svg → data/model/Doing.kt
- `StandByUs Architecture Documentation (AGENTS.md)` ----> `widget_cache SharedPreferences`  [EXTRACTED]
  AGENTS.md → app/src/main/java/com/standbyus/app/widget/StandByWidget.kt
- `ic-signal.svg — Signal Bars Icon (4 vertical bars ascending height)` --represents--> `Device Pairing (6-char alphanumeric code exchange)`  [INFERRED]
  NewUI/icons/ic-signal.svg → data/repository/PairingRepository.kt
- `ic-signal.svg — Signal Bars Icon (4 vertical bars ascending height)` --represents--> `Status Polling (6s callbackFlow via Supabase REST)`  [INFERRED]
  NewUI/icons/ic-signal.svg → ui/home/HomeViewModel.kt

## Hyperedges (group relationships)
- **Write-Through Status Sync Pipeline** — StatusRepository_StatusRepository, SupabaseService_SupabaseService, StatusDao_StatusDao, StatusRepository_widget_cache [INFERRED]
- **Hand-Written Map Serialization Convention** — UserStatus_UserStatus, PairingInfo_PairingInfo, AlbumPhoto_AlbumPhoto [INFERRED]
- **Status Preset System** — Feeling_Feeling, Doing_Doing, UserStatus_UserStatus [INFERRED]
- **MVVM+StateFlow Architecture Pattern** —  [INFERRED]
- **Celebration Display Pipeline** —  [INFERRED]
- **Partner Identity Resolution Pattern** —  [INFERRED]
- **Material3 Theme Pipeline** — color_coralpalette, color_warmneutrals, color_darkmode, color_functional, theme_standbyustheme, type_standbytypography [INFERRED]
- **Widget Display Chain** — manifest_androidmanifest, widget_standbywidgetreceiver, widget_standbywidget, widget_widgetcache, userstatus_model [INFERRED]
- **Launcher Icon Composition** — mipmap_launcher, drawable_launcherbackground, drawable_launcherforeground, mipmap_hdpilauncher [INFERRED]
- **Celebration Theme Icons** —  [INFERRED]
- **Navigation Destination Icons** —  [INFERRED]
- **Action Icons** —  [INFERRED]
- **Status/Mood Indicator Icons** —  [INFERRED]
- **Data Pipeline (Remote → Repository → Local)** — remote_layer, repository_layer, local_database [EXTRACTED 1.00]
- **UI Screens (6 screens via NavHost)** — home_screen, post_status_screen, history_screen, album_screen, settings_screen [EXTRACTED 1.00]
- **Design System (Theme + NewUI + Emotion Colors)** — theme_system, emotion_colors, emoji_theme, newui_design_tokens [INFERRED 0.85]
- **Checkin Full Feature Implementation** — CheckinDataModel, CheckinDao, CheckinRepository, CheckinViewModel, CheckinScreen, PKStats, CheckinFeature [EXTRACTED 1.00]
- **Celebration Overlay Feature** — CelebrationDay, CelebrationConfig, CelebrationOverlay, birthdayCelebrationSpec_doc [EXTRACTED 1.00]
- **Pairing Username Nickname Feature** — PairingInfo, PairingRepository, PairingUsernameNicknameFeature [EXTRACTED 1.00]

## Communities (64 total, 33 thin omitted)

### Community 0 - "Core Infrastructure + Layers"
Cohesion: 0.05
Nodes (54): Album Screen, AppDatabase (Room), AppModule, App Root (com.standbyus.app), AvatarWithGlow, callbackFlow Polling, CelebrationConfig, CelebrationDay (+46 more)

### Community 1 - "Screen Composables + Bottom Nav"
Cohesion: 0.08
Nodes (27): AlbumScreen(), PhotoCard(), BottomNavBar(), BottomNavItem, MainActivity, CelebrationDay, CelebrationOverlay(), Particle (+19 more)

### Community 2 - "Status Flow: Entity → Widget"
Cohesion: 0.09
Nodes (10): StandByApplication, StatusEntity, toEntity(), toUserStatus(), fromMap(), UserStatus, PostStatusViewModel, toHex() (+2 more)

### Community 3 - "Checkin Data Flow: Entity → Repository"
Cohesion: 0.12
Nodes (6): CheckinRecordEntity, toData(), toEntity(), CheckinData, fromMap(), CheckinRepository

### Community 4 - "Emoji Theme + Enums + Home Session"
Cohesion: 0.1
Nodes (21): Emoji Text Timeline Album Bug Fix Session, Timeline Display Fix Session, UI Redesign 5 Screens Session, Doing Enum (12 values), Emoji Theme Manager, Feeling Enum (16 values), AlbumPhoto Model, AvatarWithGlow (+13 more)

### Community 5 - "Icons + Feature Navigation"
Cohesion: 0.13
Nodes (19): Custom Doing Text Input (max 20 chars), Celebration Overlay System, History Screen (Navigation Destination), Home Screen (Navigation Destination), Post Status Screen (Navigation Destination), Settings Screen (Navigation Destination), ic-motion.svg — Sunburst/Radiate Icon (8-ray starburst pattern), ic-post.svg — Pencil/Edit Icon (angled pencil with rule lines) (+11 more)

### Community 6 - "Pairing/Album/Celebration Features"
Cohesion: 0.16
Nodes (18): Pairing Username Nickname Session, Pairing Info Data Class, Pairing Repository, Pairing Username Nickname Feature, AlbumScreen, AlbumViewModel, CelebrationConfig, CelebrationDay (+10 more)

### Community 7 - "Core Data Layer: Repositories + DB + Supabase"
Cohesion: 0.21
Nodes (17): AlbumPhoto, AlbumRepository, AppDatabase (Room), AppModule (Hilt DI), PairingInfo, PairingRepository, StandByApplication, StatusDao (+9 more)

### Community 8 - "Theme System: Colors + Typography + Modes"
Cohesion: 0.19
Nodes (14): Coral Brand Color Palette (珊瑚橙系), Dark Mode Color Scheme, Functional Colors (Error, Success, Warning), Legacy Color Aliases (兼容旧代码), Warm Neutral Color Tokens (暖调), Launcher Icon Background Layer, Launcher Icon Foreground Heart Shape, HDPI Launcher Icon Raster (ic_launcher.webp) (+6 more)

### Community 9 - "Project Docs + Build Config + Widget"
Cohesion: 0.15
Nodes (14): StandByUs Architecture Documentation (AGENTS.md), Device-Based Identity (No User Accounts), Room Local Cache (status_cache table), Gradle Build Properties, Graph Analysis Report (2026-05-12), AndroidManifest.xml - App Entry Points, StandBy Us Project README, Tech Stack (README) (+6 more)

### Community 10 - "Checkin + Album Specs & Plans"
Cohesion: 0.2
Nodes (14): Checkin Page UI Replacement Session, Album Feature (Our Story), Checkin DAO, Checkin Data Model, Checkin PK Feature, Checkin Repository, Checkin Screen Composable, Checkin ViewModel (+6 more)

### Community 14 - "StandByUs Design Tokens"
Cohesion: 0.18
Nodes (10): EmojiTheme, MoodColor, StandByUsDarkColors, StandByUsElevation, StandByUsLightColors, StandByUsMoods, StandByUsMotion, StandByUsShapes (+2 more)

### Community 15 - "AlbumRepository + AlbumPhoto Model"
Cohesion: 0.28
Nodes (3): AlbumPhoto, fromMap(), AlbumRepository

### Community 18 - "CheckinViewModel + PK Stats"
Cohesion: 0.22
Nodes (4): CheckinUiState, CheckinViewModel, PKStats, RiskLevel

### Community 19 - "HomeScreen Composables"
Cohesion: 0.46
Nodes (7): cardGradientFor(), EmptyPartnerState(), formatRelativeTime(), HomeScreen(), MyStatusStrip(), PartnerStatusCard(), QuickPostButton()

### Community 24 - "Celebration Feature Docs + Code"
Cohesion: 0.4
Nodes (5): Celebration Config, Celebration Day Data Class, Celebration Overlay, Birthday Celebration Plan, Birthday Celebration Design Spec

### Community 25 - "Shinchan Theme: Happy/Cry/Fight/Kiss"
Cohesion: 1.0
Nodes (5): 哭哭-小新哭哭表情, 奋斗-小新奋斗表情, 开心-小新开心表情, 亲亲-小新亲亲表情, 拉了么打卡页面UI设计

### Community 26 - "XiaoXin Theme: Anxious/Bored/Leisurely"
Cohesion: 0.6
Nodes (5): 焦虑 (Anxious), 无聊 (Bored), 悠闲 (Leisurely), 想你 (Miss You), 思考 (Thinking)

### Community 27 - "XiaoXin Theme: Angry/Sick/Tired/Sleep"
Cohesion: 0.6
Nodes (5): 生气 sticker (Xiao Xin), 生病 sticker (Xiao Xin), 疲惫 sticker (Xiao Xin), 睡觉 sticker (Xiao Xin), 难过 sticker (Xiao Xin)

### Community 34 - "Build System (Gradle)"
Cohesion: 0.67
Nodes (3): :app Module Build, StandByUs Root Project, StandByUs Settings

### Community 35 - "Architecture Docs (Polling/Supabase)"
Cohesion: 0.67
Nodes (3): 6-Second Status Polling (callbackFlow), Supabase via Raw HTTP (OkHttp, No SDK), Firebase Setup Guide (Superseded)

### Community 36 - "OpenAgent Plugin Sessions"
Cohesion: 0.67
Nodes (3): Sisyphus Creative Agent Session, Visual Subagent Flow Plugin Session, Oh My OpenAgent Plugin v4.1.2

## Ambiguous Edges - Review These
- `ic-ruler.svg — Ruler/Pencil Icon (angled measuring tool)` → `Custom Doing Text Input (max 20 chars)`  [AMBIGUOUS]
   · relation: represents
- `ic-ruler.svg — Ruler/Pencil Icon (angled measuring tool)` → `Post Status Screen (Navigation Destination)`  [AMBIGUOUS]
   · relation: part_of
- `ic-sun.svg — Sun Icon (circle with 8 radiating rays)` → `Feeling Enum (9 values: 开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考)`  [AMBIGUOUS]
   · relation: represents
- `ic-trash.svg — Trash/Delete Icon (can with lid and vertical slit lines)` → `History Screen (Navigation Destination)`  [AMBIGUOUS]
   · relation: part_of

## Knowledge Gaps
- **95 isolated node(s):** `Doing`, `Feeling`, `SupabaseConfig`, `SupabaseModule`, `Routes` (+90 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `ic-ruler.svg — Ruler/Pencil Icon (angled measuring tool)` and `Custom Doing Text Input (max 20 chars)`?**
  _Edge tagged AMBIGUOUS (relation: represents) - confidence is low._
- **What is the exact relationship between `ic-ruler.svg — Ruler/Pencil Icon (angled measuring tool)` and `Post Status Screen (Navigation Destination)`?**
  _Edge tagged AMBIGUOUS (relation: part_of) - confidence is low._
- **What is the exact relationship between `ic-sun.svg — Sun Icon (circle with 8 radiating rays)` and `Feeling Enum (9 values: 开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考)`?**
  _Edge tagged AMBIGUOUS (relation: represents) - confidence is low._
- **What is the exact relationship between `ic-trash.svg — Trash/Delete Icon (can with lid and vertical slit lines)` and `History Screen (Navigation Destination)`?**
  _Edge tagged AMBIGUOUS (relation: part_of) - confidence is low._
- **Why does `App Root (com.standbyus.app)` connect `Core Infrastructure + Layers` to `Pairing/Album/Celebration Features`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `Feeling Enum (9 values: 开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考)` connect `Emoji Theme + Enums + Home Session` to `Icons + Feature Navigation`?**
  _High betweenness centrality (0.013) - this node is a cross-community bridge._
- **What connects `Doing`, `Feeling`, `SupabaseConfig` to the rest of the system?**
  _95 weakly-connected nodes found - possible documentation gaps or missing edges._