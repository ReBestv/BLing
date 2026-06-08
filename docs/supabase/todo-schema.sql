create table if not exists public.todo_lists (
  id bigserial primary key,
  pair_id text not null default '',
  name text not null default '',
  owner_id text not null default '',
  is_shared boolean not null default false,
  sort_order bigint not null default 0,
  created_at bigint not null default 0
);

create index if not exists todo_lists_pair_id_idx
  on public.todo_lists (pair_id, sort_order desc, created_at desc);

create table if not exists public.todo_items (
  id bigserial primary key,
  list_id bigint not null references public.todo_lists(id) on delete cascade,
  title text not null default '',
  is_done boolean not null default false,
  note text not null default '',
  owner_id text not null default '',
  done_by text,
  sort_order bigint not null default 0,
  created_at bigint not null default 0,
  done_at bigint
);

create index if not exists todo_items_list_id_idx
  on public.todo_items (list_id, sort_order desc, created_at desc);

grant usage on schema public to anon, authenticated;
grant all on public.todo_lists to anon, authenticated;
grant all on public.todo_items to anon, authenticated;
grant usage, select on sequence public.todo_lists_id_seq to anon, authenticated;
grant usage, select on sequence public.todo_items_id_seq to anon, authenticated;

alter table public.todo_lists disable row level security;
alter table public.todo_items disable row level security;
