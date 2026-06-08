# 拉屎打卡通知设计

## 背景

StandByUs 当前使用设备 ID 作为用户身份，两台设备通过 `pairs` 表配对。打卡记录写入 Supabase `checkins` 表，并通过 6 秒轮询同步。首页已有 `interactions` 表用于伴侣互动，也通过 6 秒轮询读取对方发来的最新互动。

用户希望在“拉屎打卡”时通知对方，通知文案为“TA正在拉屎”。由于主要使用国内网络，本版本不接入 Firebase/FCM，也不接入各手机厂商推送，先做不依赖 VPN 的 Supabase 轮询通知方案。

## 目标

- 点击拉屎打卡后，继续保存一次打卡记录。
- 如果当前设备已配对，同时向对方写入一条 `poop_checkin` 互动事件。
- 对方 App 进程存活并具备通知权限时，轮询到新的 `poop_checkin` 事件后弹出系统通知。
- 通知文案使用简体中文：“TA正在拉屎”。
- 通知点击后打开 App。

## 非目标

- 不保证 App 被系统杀死后仍能收到通知。
- 不接入 Firebase、FCM、Supabase Edge Function 或国内厂商推送。
- 不新增用户账号体系。
- 不新增独立通知表，除非后续需要更复杂的通知中心。

## 推荐方案

复用现有 `interactions` 数据通道，新增一种互动类型：

- key: `poop_checkin`
- send text: `正在拉屎`
- received text: `TA正在拉屎`

打卡成功后由 `CheckinRepository` 或 `CheckinViewModel` 在已配对时调用 `InteractionRepository.sendInteraction(...)`，向对方写入 `poop_checkin`。对方端新增一个小的通知监听器，复用 `InteractionRepository.observeLatestReceivedInteraction(myUserId)`，当检测到最新互动是新的、未通知过的 `poop_checkin` 时发送本地系统通知。

这个方案不需要新增 Supabase 表，和现有亲密互动数据模型一致，失败时也不会影响打卡本身。

## 数据流

1. 用户点击打卡按钮。
2. App 写入 `checkins` 表并缓存到 Room。
3. 如果本机有 `partner_id`，App 写入一条 `interactions` 记录：
   - `fromUserId`: 当前设备 ID
   - `toUserId`: 对方设备 ID
   - `type`: `poop_checkin`
   - `text`: `正在拉屎`
   - `targetStatusTime`: 当前打卡时间或 0
   - `createdAt`: 当前时间
4. 对方 App 通过现有轮询拿到最新互动。
5. 本地通知监听器检查：
   - 类型是 `poop_checkin`
   - 事件 ID 或创建时间尚未通知过
   - 通知权限已授予
6. App 发送系统通知：“TA正在拉屎”。
7. 记录已通知的 interaction id，避免每轮轮询重复弹通知。

## Android 通知行为

App 已声明 `POST_NOTIFICATIONS` 权限。实现时需要：

- 在 Android 13+ 上请求通知权限，建议在首次需要通知能力时触发。
- 创建一个通知渠道，例如 `partner_events`。
- 通知标题可使用 App 名或“StandByUs”，正文为“TA正在拉屎”。
- 通知点击后打开 `MainActivity`。
- 如果权限未授予，不弹系统通知；功能仍会写入互动事件。

## 错误处理

- 打卡成功、互动发送失败：打卡仍算成功，只记录日志，不阻塞用户。
- 未配对：只保存打卡，不发送互动通知。
- 轮询失败：沿用现有轮询失败策略，下次轮询再试。
- 通知权限未开启：不弹通知，不重复请求打扰用户；后续可在设置页补一个开关或说明。

## 测试策略

- 为 `InteractionType` 增加单元测试，确认 `poop_checkin` 的 key 和展示文案正确。
- 为通知去重逻辑增加单元测试，确认同一条互动不会重复通知。
- 构建验证：运行 `./gradlew testDebugUnitTest` 和 `./gradlew assembleDebug`。
- 手动验证：两台设备或两个模拟器配对后，一端打卡，另一端在 App 进程存活时收到通知。

## 已知限制

这是轮询版本地通知，不是真正远程推送。它适合先验证功能和体验，但后台可靠性受 Android 系统限制。如果后续需要稳定离线推送，应单独设计国内厂商推送方案。
