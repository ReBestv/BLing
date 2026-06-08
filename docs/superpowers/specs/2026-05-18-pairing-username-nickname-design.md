# 配对功能优化：用户名 + 对方备注

## 概述

为 StandByUs 的配对功能增加两层名字系统：
- **用户名**：用户自己设置的显示名称，公开存储在 Supabase `pairs` 表，对方可见
- **对方备注**：用户给搭档起的备注名称，仅本地存储，对方不可见

## 数据存储

### 远程（Supabase `pairs` 表）

已有 `user1Name`/`user2Name` 列（`PairingInfo` 数据类已包含），需在 Supabase 控制台执行 ALTER TABLE 确保列存在：

```sql
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user1Name" TEXT NOT NULL DEFAULT '';
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user2Name" TEXT NOT NULL DEFAULT '';
```

- `user1Name` — 创建配对码时，创建者填写的用户名
- `user2Name` — 加入配对时，加入者填写的用户名

### 本地（`pairing` SharedPreferences）

已有字段：`is_paired`、`partner_id`
新增字段：

| 字段 | 类型 | 说明 | 写入时机 |
|------|------|------|----------|
| `self_name` | String | 我自己的用户名 | 配对创建/加入时 |
| `partner_name` | String | 对方的用户名 | 配对成功轮询/加入成功时 |
| `partner_nickname` | String | 我给对方的备注 | 设置页编辑时 |

## 配对流程变化

### 创建配对码

```
当前：                   优化后：
[生成配对码]              [输入你的昵称: _____]
                         [生成配对码]
```

- 点击"生成配对码"前必须先填写昵称（非空校验）
- `PairingRepository.createPairingCode()` 新增 `creatorName` 参数
- Supabase 写入 `user1Name` = creatorName
- 本地缓存 `self_name` = creatorName

### 加入配对

```
当前：                   优化后：
[输入6位配对码: _____]    [输入你的昵称: _____]
[连接]                   [输入6位配对码: _____]
                         [连接]
```

- 先填昵称再输配对码
- `PairingRepository.joinPair()` 新增 `joinerName` 参数
- Supabase PATCH 同时更新 `user2Name` = joinerName
- 查询返回后缓存对方用户名 `partner_name` = user1Name
- 本地缓存 `self_name` = joinerName

### 配对成功轮询（创建者侧）

`SettingsViewModel.pollPairingComplete()` 获取到完整 `PairingInfo` 后，从中提取对方用户名并缓存到 `partner_name`。

## 备注编辑（配对成功后）

设置在设置页"配对状态"卡片内，仅已配对状态可见：

```
┌─ 配对状态 ─────────────────────┐
│  💕 已绑定                      │
│                                 │
│  对方昵称                       │
│  [___________]  ← OutlinedTextField │
│                                 │
│  [断开连接]                      │
└─────────────────────────────────┘
```

- 输入即保存到 `partner_nickname`（`onValueChange` 即时写入 SharedPreferences）
- 不设保存按钮，无服务端同步
- 清空则回退显示用户名

## 首页展示

`HomeScreen.PartnerStatusCard` 当前显示"她刚刚更新了这个心情"，改为动态：

```
{备注 || 对方用户名 || "对方"}刚刚更新了这个心情
```

显示优先级：
1. `partner_nickname`（本地备注，非空优先）
2. `partner_name`（对方用户名）
3. 兜底文字："对方"

`HomeViewModel` 新增 `partnerDisplayName: StateFlow<String>`，从 `pairing` SharedPreferences 读取 `partner_nickname` 和 `partner_name`，组合出最终显示名。

`StatusCard` 已有 `userName` 参数，HomeScreen 传入即可。

## 涉及文件变更

| 文件 | 变更 |
|------|------|
| `data/model/PairingInfo.kt` | 无需改动（已有 `user1Name`/`user2Name`） |
| `data/repository/PairingRepository.kt` | `createPairingCode()` 新增 `creatorName` 参数；`joinPair()` 新增 `joinerName` 参数，更新时写入 `user2Name` |
| `ui/settings/SettingsScreen.kt` | 未配对区：创建/加入流程各加一个昵称输入框；已配对区：加对方备注输入框 |
| `ui/settings/SettingsViewModel.kt` | 新增 `nameInput`/`nicknameInput` 状态；createCode/joinPair 传递名字；配对成功后缓存 partner_name；写入/读取 partner_nickname |
| `ui/home/HomeViewModel.kt` | 新增 `partnerDisplayName` 暴露给 UI |
| `ui/home/HomeScreen.kt` | PartnerStatusCard 传入名字替换硬编码"她" |

## Supabase 操作

需在 Supabase 控制台执行：
```sql
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user1Name" TEXT NOT NULL DEFAULT '';
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user2Name" TEXT NOT NULL DEFAULT '';
```

（如果列已存在则跳过）

## 不做的事

- Widget 不显示名字
- 不修改 Room 数据库
- 不做用户名查重/唯一性校验
- 不修改导航结构
