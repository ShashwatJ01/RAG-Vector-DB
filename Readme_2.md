# RAG With Supabase Vector DB

## Test upload

This project is a full-stack RAG application. It lets a user upload PDF or text files, queues those files for asynchronous ingestion, converts document text into embeddings using Google's Gemini API, stores those embeddings in Supabase Postgres with pgvector, and answers questions by retrieving the most relevant indexed document chunks.

The application has two main parts:

- `backend`: Spring Boot API for file upload, async ingestion jobs, background text extraction, embeddings, vector storage, vector search, and answer generation.
- `frontend`: React + Vite UI for uploading documents, watching ingestion status, asking questions, and switching between light and dark mode.

## What This Project Does

1. The user selects one or more files in the frontend.
2. The frontend sends those files to the backend at `POST /api/documents/upload`.
3. The backend reads the file bytes, computes a SHA-256 file hash, and checks for duplicate uploads in the same workspace.
4. The backend stores document metadata in Supabase table `documents` with `document_status = QUEUED`.
5. The backend stores an ingestion job in Supabase table `ingestion_jobs`.
6. The upload request returns quickly, before extraction and embedding are complete.
7. A scheduled background worker claims queued jobs and marks them `PROCESSING`.
8. The worker extracts readable text from PDFs or text files.
9. The worker splits text into overlapping chunks.
10. Each chunk is sent to Gemini to create a 1536-dimensional embedding.
11. Each chunk and its vector embedding are stored in Supabase table `document_chunks`.
12. The document and job are marked `INDEXED`, or marked `FAILED` after retries are exhausted.
13. When the user asks a question, the frontend sends the query, source scope, retrieval mode, and optional hybrid weights.
14. Semantic mode uses Supabase pgvector cosine search over `INDEXED` chunks.
15. Keyword mode uses Postgres full-text search over a generated `tsvector` chunk column.
16. Hybrid mode combines keyword rank and vector rank with reciprocal rank fusion, then returns the fused top chunks.
17. Gemini receives the retrieved chunks as context and generates a grounded answer.

## Tech Stack

- Java 17
- Spring Boot 3.2.12
- Spring Web
- Spring JDBC
- PostgreSQL JDBC driver
- Supabase Postgres
- pgvector
- Gemini API
- Apache PDFBox
- React 18
- Vite

The backend dependencies are defined in `backend/pom.xml`. Important dependencies:

- `spring-boot-starter-web`: REST API support, lines 24-27.
- `pdfbox`: PDF text extraction, lines 40-43.
- `spring-boot-starter-jdbc`: database access through `JdbcTemplate`, lines 45-48.
- `postgresql`: JDBC driver for Supabase Postgres, lines 49-52.

## Project Structure

```text
RAG with Vector DB/
|-- backend/
|   |-- sql/
|   |-- src/main/java/com/example/ragsearch/
|   |   |-- controller/
|   |   |-- model/
|   |   `-- service/
|   |-- src/main/resources/application.properties
|   `-- pom.xml
|-- frontend/
|   |-- src/App.jsx
|   |-- package.json
|   `-- vite.config.js
|-- README.md
`-- Readme_2.md
```

## Required Accounts And Keys

You need:

- A Google Gemini API key.
- A Supabase project.
- The Supabase database password.
- A Supabase direct database connection or session pooler connection.

Do not use the Supabase anon key or service role key as the database password. The JDBC connection needs the actual database password from Supabase.

## Supabase Setup

In Supabase, open SQL Editor and run this base schema:

```sql
create extension if not exists vector with schema extensions;

create table if not exists documents (
  id text primary key,
  workspace_id text null,
  file_name text not null,
  length bigint not null default 0,
  embedding_model text null,
  embedding_dimension integer null,
  document_status text not null default 'INDEXED',
  file_hash text null,
  error_message text null,
  uploaded_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  indexed_at timestamptz null,
  constraint documents_document_status_check
    check (document_status in ('QUEUED', 'PROCESSING', 'INDEXED', 'FAILED'))
);

create table if not exists document_chunks (
  id text primary key,
  document_id text not null references documents(id) on delete cascade,
  content text not null,
  embedding extensions.vector(1536),
  created_at timestamptz not null default now()
);

create index if not exists document_chunks_embedding_hnsw_idx
on document_chunks
using hnsw (embedding vector_cosine_ops);

create unique index if not exists documents_workspace_file_hash_uidx
on documents (coalesce(workspace_id, ''), file_hash)
where file_hash is not null;

create table if not exists ingestion_jobs (
  id text primary key,
  document_id text not null references documents(id) on delete cascade,
  workspace_id text null,
  file_name text not null,
  file_hash text not null,
  content_type text null,
  source_size bigint not null default 0,
  payload bytea not null,
  status text not null default 'QUEUED',
  retry_count integer not null default 0,
  max_retries integer not null default 3,
  error_message text null,
  available_at timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  started_at timestamptz null,
  finished_at timestamptz null,
  constraint ingestion_jobs_status_check
    check (status in ('QUEUED', 'PROCESSING', 'INDEXED', 'FAILED'))
);

create index if not exists ingestion_jobs_status_available_idx
on ingestion_jobs (status, available_at, created_at);

create index if not exists ingestion_jobs_document_id_idx
on ingestion_jobs (document_id);

create unique index if not exists ingestion_jobs_active_document_uidx
on ingestion_jobs (document_id)
where status in ('QUEUED', 'PROCESSING');

create index if not exists ingestion_jobs_file_hash_idx
on ingestion_jobs (file_hash);
```

The repository also includes `backend/sql/workspaces_migration.sql`. Run that file too. It creates workspace support, applies the async ingestion columns/indexes, and adds the hybrid search database objects in an idempotent way, so it is safe to run after the base schema.

Hybrid search adds these Supabase objects:

```sql
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
```

The `1536` dimension must match the backend property:

```properties
google.embedding.output-dimensionality=1536
```

That property is in `backend/src/main/resources/application.properties`, line 7.

## Supabase Connection Values

For this Spring Boot project, the database URL must be a JDBC URL.

If using Supabase session pooler, and Supabase gives you this:

```text
postgresql://postgres.epynhaubdajbrtiavzrc:[YOUR-PASSWORD]@aws-1-us-east-1.pooler.supabase.com:5432/postgres
```

Use these PowerShell environment variables:

```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USER="postgres.epynhaubdajbrtiavzrc"
$env:SUPABASE_DB_PASSWORD="your-supabase-db-password"
```

If using direct connection, the values usually look like this:

```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USER="postgres"
$env:SUPABASE_DB_PASSWORD="your-supabase-db-password"
```

The project reads these values from `backend/src/main/resources/application.properties`.

## Backend Configuration

The important backend config lives in `backend/src/main/resources/application.properties`:

```properties
server.port=8080
google.api.key=${GOOGLE_API_KEY:}
google.api.url=https://generativelanguage.googleapis.com
google.chat.model=gemini-2.5-flash-lite
google.embedding.model=gemini-embedding-001
google.embedding.output-dimensionality=1536
spring.datasource.url=${SUPABASE_DB_URL:}
spring.datasource.username=${SUPABASE_DB_USER:}
spring.datasource.password=${SUPABASE_DB_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

ingestion.worker.enabled=true
ingestion.worker.poll-ms=2000
ingestion.worker.max-jobs-per-poll=1
ingestion.worker.max-retries=3
ingestion.worker.retry-backoff-seconds=15
```

Summary:

- Line 1 runs the backend on port `8080`.
- Lines 3-7 configure Gemini.
- Lines 9-12 configure Supabase Postgres.
- Line 13 limits Hikari connection pool size.
- The `ingestion.worker.*` settings control the background worker that processes queued uploads.
- The logging settings write backend logs to `backend/logs/rag-search.log`.

## Running The Project

Open PowerShell for the backend:

```powershell
cd "D:\Projects\RAG with Vector DB\backend"

$env:GOOGLE_API_KEY="your-google-api-key"
$env:SUPABASE_DB_URL="jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USER="postgres.epynhaubdajbrtiavzrc"
$env:SUPABASE_DB_PASSWORD="your-supabase-db-password"

mvn spring-boot:run
```

Open another PowerShell window for the frontend:

```powershell
cd "D:\Projects\RAG with Vector DB\frontend"

npm install
npm run dev
```

Then open:

```text
http://localhost:3000
```

The backend runs at:

```text
http://localhost:8080
```

## Verifying Supabase Connectivity

Before running the backend, check that your machine can reach the Supabase pooler:

```powershell
Test-NetConnection aws-1-us-east-1.pooler.supabase.com -Port 5432
```

If this succeeds, your network can reach Supabase. If it fails, the backend may accept an upload but the ingestion worker will not be able to persist document status, jobs, chunks, or vectors.

After uploading a file, verify the data in Supabase:

```sql
select count(*) from documents;
select document_status, count(*) from documents group by document_status;
select status, retry_count, error_message from ingestion_jobs order by created_at desc limit 5;
select count(*) from document_chunks;
select id, vector_dims(embedding) from document_chunks limit 5;
```

The vector dimension should be:

```text
1536
```

## API Endpoints

### Upload Documents

```http
POST /api/documents/upload
```

Expected form field:

```text
files
```

The upload controller receives this request in `DocumentController.java`. It loops through the uploaded files, passes each one to `DocumentService.ingestFile`, and returns queued document details.

Example response:

```json
[
  {
    "documentId": "4bb8e2d5-9f12-3c4d-a9ef-5a2f9c8e50a1",
    "fileName": "contract.pdf",
    "size": 123456,
    "status": "QUEUED",
    "jobId": "a6b3c8c2-7dd6-4fc0-8f4d-73b8b719c7ad",
    "duplicate": false
  }
]
```

Important: this endpoint no longer means the document is already searchable. It means the upload was accepted and queued for indexing.

### List Documents

```http
GET /api/documents
```

This is handled in `DocumentController.java`. It asks the service for stored document metadata and returns it to the frontend. Each document includes `status`, which maps to the database `document_status`.

### Updated Check Document Status

```http
GET /api/documents/{documentId}/status
```

Example response:

```json
{
  "documentId": "4bb8e2d5-9f12-3c4d-a9ef-5a2f9c8e50a1",
  "fileName": "contract.pdf",
  "status": "PROCESSING",
  "jobId": "a6b3c8c2-7dd6-4fc0-8f4d-73b8b719c7ad",
  "retryCount": 1,
  "maxRetries": 3,
  "errorMessage": null,
  "createdAt": "2026-06-06T10:15:30Z",
  "updatedAt": "2026-06-06T10:15:40Z",
  "startedAt": "2026-06-06T10:15:38Z",
  "finishedAt": null
}
```

Use this endpoint when you want exact job progress for one document.

### Ask A Question

```http
POST /api/query
Content-Type: application/json
```

Example body:

```json
{
  "query": "What does this document say about pricing?",
  "searchMode": "hybrid",
  "semanticWeight": 1,
  "keywordWeight": 1
}
```

This is handled in `QueryController.java`. It validates that the query is not blank, parses `searchMode`, calls `DocumentService.answerQuery`, and returns the generated answer and source document IDs. Search only uses documents where `document_status = 'INDEXED'`.

Supported `searchMode` values:

```text
semantic
keyword
hybrid
```

## How Upload Works In Code

The upload entry point is `DocumentService.ingestFile`.

The upload request now does lightweight intake work:

1. Validate that the file is not empty.
2. Read the uploaded bytes.
3. Compute a SHA-256 hash from the bytes.
4. Look for an existing document with the same workspace and hash.
5. If the file already exists and is not failed, return the existing document instead of creating another embedding job.
6. If the file is new, create or update a row in `documents` with `document_status = QUEUED`.
7. Create a row in `ingestion_jobs` with `status = QUEUED`.
8. Return the queued document response to the frontend.

The upload request intentionally does not extract text, chunk text, call Gemini embeddings, or write vectors. That heavier work happens in the background worker.

The document ID is deterministic for new files. It is derived from `workspaceId + fileHash`, which helps make repeated uploads idempotent.

## How Async Ingestion Works In Code

The background worker is `DocumentIngestionWorker.java`.

The worker runs on a schedule controlled by `ingestion.worker.poll-ms`.

The worker flow is:

1. Call `IngestionJobRepository.claimNextJob`.
2. Claim one queued job where `status = QUEUED` and `available_at <= now()`.
3. Mark the job and document as `PROCESSING`.
4. Extract text from the job payload.
5. Split text into overlapping chunks with a max chunk size of `900` characters and overlap of `200` characters.
6. Send chunks to Gemini for embeddings.
7. Delete any old chunks for the document.
8. Store the new chunks and vectors in `document_chunks`.
9. Mark the document and job as `INDEXED`.

The claim query uses Postgres `FOR UPDATE SKIP LOCKED`. That matters when more than one backend instance is running, because it prevents two workers from processing the same job at the same time.

If processing fails, the worker increments `retry_count`, stores `error_message`, and requeues the job by setting a future `available_at`. If the retry limit is reached, the job and document are marked `FAILED`.

## How Embeddings And Answers Work

The Gemini integration is in `GoogleGenerativeAiService.java`.

- Lines 31-45 read API settings from Spring config, including the embedding dimensionality.
- Lines 47-60 embed a list of chunks by calling `embedText` for each chunk.
- Lines 63-76 call Gemini `embedContent` for one text block.
- Lines 65-69 build the embedding request and explicitly request `output_dimensionality`.
- Lines 79-108 build a prompt from retrieved document chunks and call Gemini `generateContent`.
- Lines 111-127 parse the embedding values from the Gemini response.
- Lines 129-149 parse the generated answer from the Gemini response.
- Lines 152-196 perform the HTTP POST to Google and handle API errors.

## How Vector Storage And Search Work

The Supabase/pgvector logic is in `VectorStoreService.java`.

- `saveDocumentMetadata` inserts or updates the `documents` row, including `document_status`, `file_hash`, embedding model, and embedding dimension.
- `findDocumentByFileHash` supports idempotency by checking whether a workspace already has the same uploaded file.
- `updateDocumentStatus` changes document state between `QUEUED`, `PROCESSING`, `INDEXED`, and `FAILED`.
- `markDocumentIndexed` records successful indexing and clears any previous error message.
- `deleteDocumentChunks` removes stale chunks before a retry or reprocess writes fresh vectors.
- `saveChunk` inserts a chunk into `document_chunks`, casting the string vector literal to `vector`.
- `searchNearest` searches chunks using `ORDER BY embedding <=> ?::vector LIMIT ?`.
- `searchKeyword` searches the generated `document_chunks.content_search` column with `websearch_to_tsquery`.
- `searchHybrid` calls `public.hybrid_search_document_chunks`, which uses reciprocal rank fusion over semantic and keyword rank lists.
- `semanticWeight` and `keywordWeight` let the caller bias hybrid retrieval toward meaning or exact terms.
- Search filters to `document_status = 'INDEXED'`, so queued, processing, or failed documents are not used for answers.
- `toVectorLiteral` converts Java `List<Double>` embeddings into pgvector literal format like `[0.1,0.2,0.3]`.

If Supabase reports that type `vector` does not exist, change the casts in `VectorStoreService.java` from:

```sql
?::vector
```

to:

```sql
?::extensions.vector
```

This depends on how the `vector` extension is exposed in your Supabase database.

## How Hybrid Search Was Added

The implementation follows the Supabase hybrid search pattern: run semantic search and full-text search separately, assign each result a rank, then combine those ranks with reciprocal rank fusion.

Database changes:

1. `document_chunks.content_search` is a generated `tsvector` column built from chunk `content`.
2. `document_chunks_content_search_gin_idx` is a GIN index so keyword lookup can stay fast as chunks grow.
3. `public.hybrid_search_document_chunks` accepts `query_text`, `query_embedding`, `match_count`, optional workspace/document filters, `semantic_weight`, `keyword_weight`, and `rrf_k`.
4. The SQL function builds a semantic rank list with `embedding <=> query_embedding`.
5. The SQL function builds a keyword rank list with `content_search @@ websearch_to_tsquery('english', query_text)`.
6. The final query scores each chunk with weighted reciprocal rank fusion:

```text
semantic_weight / (rrf_k + semantic_rank)
+ keyword_weight / (rrf_k + keyword_rank)
```

Backend changes:

1. `QueryRequest` now accepts `searchMode`, `semanticWeight`, and `keywordWeight`.
2. `SearchMode` validates `semantic`, `keyword`, and `hybrid`.
3. `DocumentService.answerQuery` chooses the retrieval path:
   - `semantic`: embed the question and call `searchNearest`.
   - `keyword`: skip embeddings and call `searchKeyword`.
   - `hybrid`: embed the question and call `searchHybrid`.
4. `VectorStoreService.searchKeyword` runs Postgres full-text search directly.
5. `VectorStoreService.searchHybrid` calls the SQL function and maps fused relevance back into the source citation score.

Frontend changes:

1. `App.jsx` stores `searchMode`, `semanticWeight`, and `keywordWeight`.
2. `ChatPanel.jsx` renders a segmented control for `Semantic`, `Keyword`, and `Hybrid`.
3. Hybrid mode shows two sliders for semantic and keyword weighting.
4. `ragApi.askQuestion` includes those fields in the `/api/query` request body.
5. Source cards now label the score as `Relevance`, because the same field can represent cosine similarity, keyword rank, or fused hybrid relevance.

To add this to an existing Supabase-backed copy of the project:

1. Pull the code changes.
2. Run `backend/sql/workspaces_migration.sql` in the Supabase SQL Editor.
3. Confirm the generated column and GIN index exist:

```sql
select column_name, data_type
from information_schema.columns
where table_schema = 'public'
  and table_name = 'document_chunks'
  and column_name = 'content_search';

select indexname
from pg_indexes
where schemaname = 'public'
  and tablename = 'document_chunks'
  and indexname = 'document_chunks_content_search_gin_idx';
```

4. Confirm the hybrid function exists:

```sql
select proname
from pg_proc
where proname = 'hybrid_search_document_chunks';
```

5. Restart the Spring backend.
6. Restart the Vite frontend and use the Ask AI retrieval toggle.

## How The Frontend Works

The main frontend logic is in `frontend/src/App.jsx`.

- It uploads selected files to `/api/documents/upload`.
- It fetches stored document metadata from `/api/documents`.
- It maps backend document statuses into UI labels through `frontend/src/utils/documents.js`.
- It watches for documents with `Queued` or `Processing` status.
- While any document is queued or processing, it periodically refreshes document metadata so the UI moves from `Queued` to `Processing` to `Indexed`.
- It sends the user's question, retrieval mode, and hybrid weights to `/api/query`.
- It renders source citations returned by the backend.
- In the Ask AI panel, the retrieval toggle supports `Semantic`, `Keyword`, and `Hybrid`.
- In `Hybrid`, the semantic and keyword sliders are sent as `semanticWeight` and `keywordWeight`.
- It stores the selected visual theme as `rag-theme` in `localStorage` and applies it through `data-theme` on the document root.
- If no theme has been selected, it uses the browser or OS `prefers-color-scheme` setting.
- A small bootstrap script in `frontend/index.html` applies the saved theme before React mounts to avoid a flash of the wrong theme.
- Theme colors are defined as CSS variables in `frontend/src/styles/base.css`; the layout, component, document, Ask AI, and overlay styles consume those variables.

Backend status values:

```text
QUEUED
PROCESSING
INDEXED
FAILED
```

Frontend labels:

```text
Queued
Processing
Indexed
Failed
```

The frontend dev server is configured in `frontend/vite.config.js`:

- Line 7 runs Vite on port `3000`.
- Lines 8-10 proxy `/api` calls to the backend at `http://localhost:8080`.

## Normal End-To-End Test

1. Start Supabase project.
2. Run the SQL schema in Supabase, then run `backend/sql/workspaces_migration.sql`.
3. Confirm the pooler host works:

```powershell
Test-NetConnection aws-1-us-east-1.pooler.supabase.com -Port 5432
```

4. Start backend with `GOOGLE_API_KEY`, `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, and `SUPABASE_DB_PASSWORD`.
5. Start frontend with `npm run dev`.
6. Open `http://localhost:3000`.
7. Upload a small PDF or text file.
8. Watch the document move through `Queued`, `Processing`, and `Indexed`.
9. Check Supabase tables:

```sql
select id, file_name, document_status, error_message
from documents
order by uploaded_at desc
limit 5;

select document_id, status, retry_count, error_message
from ingestion_jobs
order by created_at desc
limit 5;

select id, document_id, vector_dims(embedding)
from document_chunks
order by created_at desc
limit 5;
```

10. Ask a question about the uploaded file after it is `Indexed`.
11. Confirm the answer is grounded in the document content.

## Common Problems

### Failed to obtain JDBC Connection

This means the backend could not connect to Supabase. Check:

- `SUPABASE_DB_URL` starts with `jdbc:postgresql://`.
- The host is the pooler host if direct DB DNS does not work.
- `SUPABASE_DB_USER` is `postgres.PROJECT_REF` for pooler.
- `SUPABASE_DB_PASSWORD` is the database password.
- You restarted the backend after setting environment variables.

### Name resolution failed

Run:

```powershell
Test-NetConnection your-host -Port 5432
```

If direct host `db.PROJECT_REF.supabase.co` fails, use the Supabase session pooler host.

### Type extensions.vector does not exist

If the hybrid migration fails with:

```text
ERROR: 42704: type extensions.vector does not exist
```

Use `query_embedding public.vector(1536)` in `public.hybrid_search_document_chunks` when your schema shows `embedding public.vector`. This project's Java queries already cast query vectors as `?::vector`, which resolves correctly when `public` is on the database search path.

### Type vector does not exist

Run the extension SQL:

```sql
create extension if not exists vector with schema extensions;
```

Then either expose the type on your search path or change Java casts from `?::vector` to `?::extensions.vector`.

### Vector dimensions do not match

Make sure all three values are aligned:

- Supabase column: `embedding extensions.vector(1536)`
- Backend config: `google.embedding.output-dimensionality=1536`
- Gemini response: logs should say `Received embedding with dimension: 1536`

### Document stays Queued

Check backend logs:

```powershell
cd "D:\Projects\RAG with Vector DB\backend"
Get-Content .\logs\rag-search.log -Wait
```

Then check the job row:

```sql
select id, document_id, status, retry_count, error_message, available_at, started_at, finished_at
from ingestion_jobs
order by created_at desc
limit 5;
```

Common causes:

- The backend is not running, so the scheduled worker is not polling.
- `ingestion.worker.enabled=false`.
- `available_at` is in the future because the job is waiting for a retry.
- The `ingestion_jobs` table or indexes were not created.

### Document becomes Failed

Check the stored error:

```sql
select id, file_name, document_status, error_message
from documents
where document_status = 'FAILED'
order by uploaded_at desc;
```

Then check the latest job:

```sql
select id, document_id, status, retry_count, max_retries, error_message
from ingestion_jobs
where document_id = 'YOUR_DOCUMENT_ID'
order by created_at desc
limit 1;
```

Common causes:

- The file has no readable text.
- The Google API key is missing or invalid.
- The Gemini embedding API returned an error.
- The Supabase vector type is not available as `vector`.
- The embedding dimension does not match the `document_chunks.embedding` column.

## Build And Test Commands

Backend package build:

```powershell
cd "D:\Projects\RAG with Vector DB\backend"
mvn -DskipTests package
```

Backend tests:

```powershell
cd "D:\Projects\RAG with Vector DB\backend"
mvn test
```

The test suite may require real Supabase environment variables because `SupabaseConnectionTest` starts the Spring context and creates a datasource.

Frontend build:

```powershell
cd "D:\Projects\RAG with Vector DB\frontend"
npm run build
```

## Security Notes

Do not commit real secrets:

- Google API key
- Supabase database password
- Supabase service role key

Use environment variables for local development. If a real database password was committed or pasted into a file, rotate the password in Supabase.
