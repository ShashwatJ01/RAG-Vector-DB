create table if not exists public.workspaces (
  id text primary key,
  name text not null,
  description text not null default '',
  category text not null default 'General',
  status text not null default 'Active',
  knowledge_model text not null default 'gemini-flash-lite',
  chat_model text not null default 'gemini-2.5-flash-lite',
  embedding_model text not null default 'gemini-embedding-001',
  embedding_dimension integer not null default 1536,
  retrieval_mode text not null default 'balanced',
  speed_mode text not null default 'standard',
  top_k integer not null default 4,
  created_at timestamp with time zone not null default now(),
  archived_at timestamp with time zone null
);

alter table public.workspaces
add column if not exists knowledge_model text not null default 'gemini-flash-lite';

alter table public.workspaces
add column if not exists chat_model text not null default 'gemini-2.5-flash-lite';

alter table public.workspaces
add column if not exists embedding_model text not null default 'gemini-embedding-001';

alter table public.workspaces
add column if not exists embedding_dimension integer not null default 1536;

alter table public.workspaces
add column if not exists retrieval_mode text not null default 'balanced';

alter table public.workspaces
add column if not exists speed_mode text not null default 'standard';

alter table public.workspaces
add column if not exists top_k integer not null default 4;

alter table public.documents
add column if not exists workspace_id text null;

alter table public.documents
add column if not exists embedding_model text null;

alter table public.documents
add column if not exists embedding_dimension integer null;

alter table public.documents
drop constraint if exists documents_workspace_id_fkey;

alter table public.documents
add constraint documents_workspace_id_fkey
foreign key (workspace_id)
references public.workspaces (id)
on delete set null;

create index if not exists documents_workspace_id_idx
on public.documents (workspace_id);

alter table public.documents
add column if not exists document_status text not null default 'INDEXED';

alter table public.documents
add column if not exists file_hash text null;

alter table public.documents
add column if not exists error_message text null;

alter table public.documents
add column if not exists updated_at timestamp with time zone not null default now();

alter table public.documents
add column if not exists indexed_at timestamp with time zone null;

alter table public.documents
drop constraint if exists documents_document_status_check;

alter table public.documents
add constraint documents_document_status_check
check (document_status in ('QUEUED', 'PROCESSING', 'INDEXED', 'FAILED'));

create unique index if not exists documents_workspace_file_hash_uidx
on public.documents (coalesce(workspace_id, ''), file_hash)
where file_hash is not null;

create table if not exists public.ingestion_jobs (
  id text primary key,
  document_id text not null references public.documents(id) on delete cascade,
  workspace_id text null references public.workspaces(id) on delete set null,
  file_name text not null,
  file_hash text not null,
  content_type text null,
  source_size bigint not null default 0,
  payload bytea not null,
  status text not null default 'QUEUED',
  retry_count integer not null default 0,
  max_retries integer not null default 3,
  error_message text null,
  available_at timestamp with time zone not null default now(),
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
  started_at timestamp with time zone null,
  finished_at timestamp with time zone null,
  constraint ingestion_jobs_status_check check (status in ('QUEUED', 'PROCESSING', 'INDEXED', 'FAILED'))
);

create index if not exists ingestion_jobs_status_available_idx
on public.ingestion_jobs (status, available_at, created_at);

create index if not exists ingestion_jobs_document_id_idx
on public.ingestion_jobs (document_id);

create unique index if not exists ingestion_jobs_active_document_uidx
on public.ingestion_jobs (document_id)
where status in ('QUEUED', 'PROCESSING');

create index if not exists ingestion_jobs_file_hash_idx
on public.ingestion_jobs (file_hash);
