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

alter table public.document_chunks
add column if not exists content_search tsvector
generated always as (to_tsvector('english', coalesce(content, ''))) stored;

create index if not exists document_chunks_content_search_gin_idx
on public.document_chunks
using gin (content_search);

create or replace function public.hybrid_search_document_chunks(
  query_text text,
  query_embedding public.vector(1536),
  match_count integer,
  workspace_filter text default null,
  document_filter text[] default '{}'::text[],
  semantic_weight double precision default 1,
  keyword_weight double precision default 1,
  rrf_k integer default 50
)
returns table (
  id text,
  document_id text,
  file_name text,
  content text,
  relevance_score double precision,
  semantic_rank bigint,
  keyword_rank bigint
)
language sql
stable
as $$
with query_terms as (
  select websearch_to_tsquery('english', query_text) as query
),
filtered_chunks as (
  select
    dc.id,
    dc.document_id,
    d.file_name,
    dc.content,
    dc.embedding,
    dc.content_search
  from public.document_chunks dc
  join public.documents d on d.id = dc.document_id
  where d.document_status = 'INDEXED'
    and (workspace_filter is null or d.workspace_id = workspace_filter)
    and (cardinality(document_filter) = 0 or dc.document_id = any(document_filter))
),
semantic_search as (
  select
    id,
    row_number() over (order by embedding <=> query_embedding) as rank_ix
  from filtered_chunks
  order by embedding <=> query_embedding
  limit greatest(match_count * 4, 20)
),
keyword_search as (
  select
    fc.id,
    row_number() over (order by ts_rank_cd(fc.content_search, qt.query, 32) desc) as rank_ix
  from filtered_chunks fc
  cross join query_terms qt
  where fc.content_search @@ qt.query
  order by ts_rank_cd(fc.content_search, qt.query, 32) desc
  limit greatest(match_count * 4, 20)
),
fused as (
  select
    fc.id,
    fc.document_id,
    fc.file_name,
    fc.content,
    ss.rank_ix as semantic_rank,
    ks.rank_ix as keyword_rank,
    coalesce(1.0::double precision / (rrf_k + ss.rank_ix), 0.0) * semantic_weight
      + coalesce(1.0::double precision / (rrf_k + ks.rank_ix), 0.0) * keyword_weight as raw_score
  from filtered_chunks fc
  left join semantic_search ss on ss.id = fc.id
  left join keyword_search ks on ks.id = fc.id
  where ss.id is not null or ks.id is not null
)
select
  id,
  document_id,
  file_name,
  content,
  case
    when semantic_weight + keyword_weight <= 0 then 0
    else least(raw_score / ((semantic_weight + keyword_weight) / (rrf_k + 1)), 1.0)
  end as relevance_score,
  semantic_rank,
  keyword_rank
from fused
order by raw_score desc
limit least(match_count, 30);
$$;
