# 我们的故事 - 共同相册功能设计

## 概述

新增「我们的故事」共同相册功能，配对双方可以上传照片并配文字小记，在 App 内以 2 列网格展示。

## 新增资源

### Supabase Storage
- Bucket 名称：`our-story`
- 文件路径：`{deviceId}/{timestamp}.jpg`
- 权限：公开可读（通过 URL）

### Supabase 数据表

```sql
CREATE TABLE photos (
  id BIGSERIAL PRIMARY KEY,
  "deviceId" TEXT NOT NULL,
  "url" TEXT NOT NULL,
  "caption" TEXT NOT NULL DEFAULT '',
  "createdAt" BIGINT NOT NULL DEFAULT 0
);
```

## 新增文件

| 文件 | 用途 |
|------|------|
| `data/model/AlbumPhoto.kt` | 照片数据类 |
| `data/repository/AlbumRepository.kt` | 照片上传/查询 |
| `ui/album/AlbumScreen.kt` | 相册页面 + 上传弹窗 |
| `ui/album/AlbumViewModel.kt` | 相册逻辑 |

## 修改文件

| 文件 | 改动 |
|------|------|
| `MainActivity.kt` | 底部导航移到 Activity 层，新增相册 tab |
| `HomeScreen.kt` | 移除底部导航（移至 Activity） |
| `SupabaseService.kt` | 新增文件上传方法 |
| `navigation/Routes.kt` | 新增相册路由 |

## 功能流程

### 上传照片
1. 点击 ＋ 按钮 → `PhotoPicker` 选照片
2. 压缩照片至最大 1920px
3. 上传至 Supabase Storage → 获取 URL
4. 填写可选小记 → 写入 `photos` 表
5. 刷新相册列表

### 浏览照片
- 2 列网格，按时间倒序
- 每张显示照片缩略图 + 小记 + 时间
- 空状态显示引导文字

## 导航结构

底部导航 3 项：首页 | 我们的故事 | 历史

## 命名

「我们的故事」— 温馨浪漫，契合情侣 App 调性
