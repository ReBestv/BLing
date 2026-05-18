# Graph Report - E:\AndroidProject\StandByUs  (2026-05-13)

## Corpus Check
- 104 files · ~397,670 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 286 nodes · 304 edges · 45 communities (21 shown, 24 thin omitted)
- Extraction: 91% EXTRACTED · 8% INFERRED · 1% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.78)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_NewUI Icons|NewUI Icons]]
- [[_COMMUNITY_Album Screen|Album Screen]]
- [[_COMMUNITY_Statusentity|Statusentity]]
- [[_COMMUNITY_Color System|Color System]]
- [[_COMMUNITY_Architecture Docs|Architecture Docs]]
- [[_COMMUNITY_Repository Layer|Repository Layer]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Supabase Service|Supabase Service]]
- [[_COMMUNITY_Settings Screen|Settings Screen]]
- [[_COMMUNITY_Material Theme|Material Theme]]
- [[_COMMUNITY_NewUI Design System|NewUI Design System]]
- [[_COMMUNITY_Repository Layer|Repository Layer]]
- [[_COMMUNITY_Repository Layer|Repository Layer]]
- [[_COMMUNITY_Album Screen|Album Screen]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Material Theme|Material Theme]]
- [[_COMMUNITY_Status DAO|Status DAO]]
- [[_COMMUNITY_Widget System|Widget System]]
- [[_COMMUNITY_Data Models|Data Models]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Dependency Injection|Dependency Injection]]
- [[_COMMUNITY_History Screen|History Screen]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Home Screen|Home Screen]]
- [[_COMMUNITY_Settings Screen|Settings Screen]]
- [[_COMMUNITY_Architecture Docs|Architecture Docs]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Source Files|Source Files]]
- [[_COMMUNITY_Mainactivity|Mainactivity]]
- [[_COMMUNITY_Doing|Doing]]
- [[_COMMUNITY_Supabasemodule|Supabasemodule]]
- [[_COMMUNITY_Routes|Routes]]
- [[_COMMUNITY_Architecture Docs|Architecture Docs]]

## God Nodes (most connected - your core abstractions)
1. `SupabaseService` - 12 edges
2. `SettingsViewModel` - 10 edges
3. `AlbumViewModel` - 8 edges
4. `StatusRepository` - 8 edges
5. `PairingRepository` - 7 edges
6. `SupabaseService` - 7 edges
7. `UserStatus` - 6 edges
8. `AlbumRepository` - 5 edges
9. `StatusRepository` - 5 edges
10. `StandByWidget` - 5 edges

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

## Communities (45 total, 24 thin omitted)

### Community 0 - "Source Files"
Cohesion: 0.1
Nodes (14): AlbumScreen(), PhotoCard(), BottomNavItem, MainActivity, CelebrationDay, CelebrationOverlay(), Particle, StatusCard() (+6 more)

### Community 1 - "NewUI Icons"
Cohesion: 0.13
Nodes (20): Custom Doing Text Input (max 20 chars), Celebration Overlay System, History Screen (Navigation Destination), Home Screen (Navigation Destination), Post Status Screen (Navigation Destination), Settings Screen (Navigation Destination), ic-motion.svg — Sunburst/Radiate Icon (8-ray starburst pattern), ic-post.svg — Pencil/Edit Icon (angled pencil with rule lines) (+12 more)

### Community 2 - "Album Screen"
Cohesion: 0.14
Nodes (18): AlbumScreen, AlbumViewModel, AvatarWithGlow, CelebrationConfig, CelebrationDay, CelebrationOverlay, Device-UUID Identity, DoingPicker (+10 more)

### Community 3 - "Statusentity"
Cohesion: 0.21
Nodes (17): AlbumPhoto, AlbumRepository, AppDatabase (Room), AppModule (Hilt DI), PairingInfo, PairingRepository, StandByApplication, StatusDao (+9 more)

### Community 4 - "Color System"
Cohesion: 0.19
Nodes (14): Coral Brand Color Palette (珊瑚橙系), Dark Mode Color Scheme, Functional Colors (Error, Success, Warning), Legacy Color Aliases (兼容旧代码), Warm Neutral Color Tokens (暖调), Launcher Icon Background Layer, Launcher Icon Foreground Heart Shape, HDPI Launcher Icon Raster (ic_launcher.webp) (+6 more)

### Community 5 - "Architecture Docs"
Cohesion: 0.15
Nodes (14): StandByUs Architecture Documentation (AGENTS.md), Device-Based Identity (No User Accounts), Room Local Cache (status_cache table), Gradle Build Properties, Graph Analysis Report (2026-05-12), AndroidManifest.xml - App Entry Points, StandBy Us Project README, Tech Stack (README) (+6 more)

### Community 6 - "Repository Layer"
Cohesion: 0.18
Nodes (3): StandByApplication, StatusRepository, StandByWidget

### Community 7 - "Source Files"
Cohesion: 0.18
Nodes (6): StatusEntity, toEntity(), toUserStatus(), fromMap(), UserStatus, PostStatusViewModel

### Community 10 - "Material Theme"
Cohesion: 0.18
Nodes (10): EmojiTheme, MoodColor, StandByUsDarkColors, StandByUsElevation, StandByUsLightColors, StandByUsMoods, StandByUsMotion, StandByUsShapes (+2 more)

### Community 11 - "NewUI Design System"
Cohesion: 0.18
Nodes (10): EmojiTheme, MoodColor, StandByUsDarkColors, StandByUsElevation, StandByUsLightColors, StandByUsMoods, StandByUsMotion, StandByUsShapes (+2 more)

### Community 12 - "Repository Layer"
Cohesion: 0.28
Nodes (3): AlbumPhoto, fromMap(), AlbumRepository

### Community 15 - "Source Files"
Cohesion: 0.33
Nodes (4): DoingPicker(), FeelingPicker(), isEmoji(), PostStatusScreen()

### Community 25 - "Settings Screen"
Cohesion: 0.67
Nodes (3): :app Module Build, StandByUs Root Project, StandByUs Settings

### Community 26 - "Architecture Docs"
Cohesion: 0.67
Nodes (3): 6-Second Status Polling (callbackFlow), Supabase via Raw HTTP (OkHttp, No SDK), Firebase Setup Guide (Superseded)

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
- **55 isolated node(s):** `Doing`, `Feeling`, `SupabaseConfig`, `SupabaseModule`, `Routes` (+50 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **24 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

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
- **Why does `Feeling Enum (9 values: 开心/难过/疲惫/生病/悠闲/想你了/睡觉/奋斗/思考)` connect `Album Screen` to `NewUI Icons`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Why does `ic-sun.svg — Sun Icon (circle with 8 radiating rays)` connect `NewUI Icons` to `Album Screen`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **What connects `Doing`, `Feeling`, `SupabaseConfig` to the rest of the system?**
  _55 weakly-connected nodes found - possible documentation gaps or missing edges._