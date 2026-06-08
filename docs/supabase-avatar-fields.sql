alter table public.statuses
add column if not exists "avatarEmoji" text not null default '🙂',
add column if not exists "avatarUrl" text not null default '';
