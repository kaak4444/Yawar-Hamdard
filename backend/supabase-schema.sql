-- Yawar Hamdard Supabase migration
-- Run this in the Supabase SQL editor after enabling Email auth.
-- The Android app uses the publishable key; every table and storage object is
-- protected by RLS. The Hostinger API remains compatible during migration.

create extension if not exists pgcrypto;

create table if not exists public.care_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  role text not null check (role in ('patient','doctor','hospital','yhcs','admin')),
  avatar_path text,
  phone text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.care_conversations (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.care_conversation_members (
  conversation_id uuid not null references public.care_conversations(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  joined_at timestamptz not null default now(),
  primary key (conversation_id, user_id)
);

create table if not exists public.care_messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id uuid not null references public.care_conversations(id) on delete cascade,
  sender_id uuid not null references auth.users(id) on delete cascade,
  kind text not null check (kind in ('text','image','video','audio','file','system')),
  body text not null default '',
  attachment_path text,
  attachment_name text,
  attachment_mime text,
  attachment_bytes bigint,
  created_at timestamptz not null default now(),
  read_at timestamptz
);

create index if not exists care_messages_conversation_created_idx
  on public.care_messages(conversation_id, created_at desc);

alter table public.care_profiles enable row level security;
alter table public.care_conversations enable row level security;
alter table public.care_conversation_members enable row level security;
alter table public.care_messages enable row level security;

drop policy if exists care_profiles_read on public.care_profiles;
create policy care_profiles_read on public.care_profiles for select to authenticated
  using (true);
drop policy if exists care_profiles_self_write on public.care_profiles;
create policy care_profiles_self_write on public.care_profiles for all to authenticated
  using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists care_conversations_member_read on public.care_conversations;
create policy care_conversations_member_read on public.care_conversations for select to authenticated
  using (exists (select 1 from public.care_conversation_members m
                 where m.conversation_id = id and m.user_id = auth.uid()));
drop policy if exists care_conversations_member_insert on public.care_conversations;
create policy care_conversations_member_insert on public.care_conversations for insert to authenticated
  with check (true);

drop policy if exists care_members_read on public.care_conversation_members;
create policy care_members_read on public.care_conversation_members for select to authenticated
  using (user_id = auth.uid() or exists (select 1 from public.care_conversation_members m
                                         where m.conversation_id = conversation_id
                                           and m.user_id = auth.uid()));
drop policy if exists care_members_insert_self on public.care_conversation_members;
create policy care_members_insert_self on public.care_conversation_members for insert to authenticated
  with check (user_id = auth.uid() or exists (select 1 from public.care_conversation_members m
                                               where m.conversation_id = conversation_id
                                                 and m.user_id = auth.uid()));

drop policy if exists care_messages_member_read on public.care_messages;
create policy care_messages_member_read on public.care_messages for select to authenticated
  using (exists (select 1 from public.care_conversation_members m
                 where m.conversation_id = conversation_id and m.user_id = auth.uid()));
drop policy if exists care_messages_member_insert on public.care_messages;
create policy care_messages_member_insert on public.care_messages for insert to authenticated
  with check (sender_id = auth.uid() and exists (select 1 from public.care_conversation_members m
                                                 where m.conversation_id = conversation_id
                                                   and m.user_id = auth.uid()));
drop policy if exists care_messages_member_update on public.care_messages;
create policy care_messages_member_update on public.care_messages for update to authenticated
  using (sender_id = auth.uid() or exists (select 1 from public.care_conversation_members m
                                           where m.conversation_id = conversation_id
                                             and m.user_id = auth.uid()))
  with check (sender_id = auth.uid() or exists (select 1 from public.care_conversation_members m
                                                where m.conversation_id = conversation_id
                                                  and m.user_id = auth.uid()));

insert into storage.buckets (id, name, public)
values ('care-attachments', 'care-attachments', false)
on conflict (id) do nothing;

drop policy if exists care_attachment_read on storage.objects;
create policy care_attachment_read on storage.objects for select to authenticated
  using (bucket_id = 'care-attachments');
drop policy if exists care_attachment_insert on storage.objects;
create policy care_attachment_insert on storage.objects for insert to authenticated
  with check (bucket_id = 'care-attachments' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists care_attachment_update on storage.objects;
create policy care_attachment_update on storage.objects for update to authenticated
  using (bucket_id = 'care-attachments' and owner_id = auth.uid()::text);
drop policy if exists care_attachment_delete on storage.objects;
create policy care_attachment_delete on storage.objects for delete to authenticated
  using (bucket_id = 'care-attachments' and owner_id = auth.uid()::text);

do $$
begin
  if not exists (
    select 1 from pg_publication_tables
    where pubname = 'supabase_realtime'
      and schemaname = 'public'
      and tablename = 'care_messages'
  ) then
    alter publication supabase_realtime add table public.care_messages;
  end if;
end $$;
