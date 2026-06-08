alter table public.statuses
  add column if not exists "themeId" text,
  add column if not exists "themeName" text,
  add column if not exists "feelingKey" text,
  add column if not exists "feelingLabel" text,
  add column if not exists "feelingAsset" text,
  add column if not exists "feelingFallbackEmoji" text,
  add column if not exists "stickerId" text,
  add column if not exists "stickerLabel" text,
  add column if not exists "stickerAsset" text;
