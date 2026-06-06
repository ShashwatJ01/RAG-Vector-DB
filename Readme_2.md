# RAG With Supabase Vector DB

This project is a full-stack RAG application. It lets a user upload PDF or text files, converts the document text into embeddings using Google's Gemini API, stores those embeddings in Supabase Postgres with pgvector, and answers questions by retrieving the most relevant document chunks.

The application has two main parts:

- `backend`: Spring Boot API for file upload, text extraction, embeddings, vector storage, vector search, and answer generation.
- `frontend`: React + Vite UI for uploading documents and asking questions.

## What This Project Does

1. The user selects one or more files in the frontend.
2. The frontend sends those files to the backend at `POST /api/documents/upload`.
3. The backend extracts readable text from PDFs or text files.
4. The text is split into overlapping chunks.
5. Each chunk is sent to Gemini to create a 1536-dimensional embedding.
6. The document metadata is stored in Supabase table `documents`.
7. Each chunk and its vector embedding are stored in Supabase table `document_chunks`.
8. When the user asks a question, the backend embeds the question.
9. Supabase pgvector finds the closest chunks using cosine distance.
10. Gemini receives the retrieved chunks as context and generates a grounded answer.

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

In Supabase, open SQL Editor and run this schema:

```sql
create extension if not exists vector with schema extensions;

create table if not exists documents (
  id text primary key,
  file_name text not null,
  length bigint not null,
  created_at timestamptz not null default now()
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

The project reads these values from `application.properties`, lines 9-12.

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
```

Summary:

- Line 1 runs the backend on port `8080`.
- Lines 3-7 configure Gemini.
- Lines 9-12 configure Supabase Postgres.
- Line 13 limits Hikari connection pool size.
- Lines 15-17 configure backend logging.

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

If this succeeds, your network can reach Supabase. If it fails, the backend will not be able to upload documents because it cannot store vectors.

After uploading a file, verify the data in Supabase:

```sql
select count(*) from documents;
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

The upload controller receives this request in `DocumentController.java`, lines 34-49. It loops through the uploaded files, passes each one to `DocumentService.ingestFile`, and returns the uploaded document details.

### List Documents

```http
GET /api/documents
```

This is handled in `DocumentController.java`, lines 69-74. It asks the service for stored document metadata and returns it to the frontend.

### Ask A Question

```http
POST /api/query
Content-Type: application/json
```

Example body:

```json
{
  "query": "What does this document say about pricing?"
}
```

This is handled in `QueryController.java`, lines 27-39. It validates that the query is not blank, calls `DocumentService.answerQuery`, and returns the generated answer and source document IDs.

## How Upload Works In Code

The main upload flow is in `DocumentService.java`.

- Lines 41-45 validate that the uploaded file is not empty.
- Lines 49-54 extract text and reject unreadable files.
- Lines 56-59 create a new document ID and metadata object.
- Lines 61-63 split the document into chunks.
- Lines 65-69 send chunks to Gemini and receive embeddings.
- Lines 71-80 save the document metadata and chunk embeddings to Supabase.
- Lines 122-138 extract text from PDF files using PDFBox or read text files as UTF-8.
- Lines 140-157 split the text into overlapping chunks with a max chunk size of `900` characters and overlap of `200` characters.

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

- Lines 21-25 inject `JdbcTemplate` and the configured vector dimension.
- Lines 27-31 insert document metadata into the `documents` table.
- Lines 34-39 insert a chunk into `document_chunks`, casting the string vector literal to `vector`.
- Lines 42-51 list stored documents from Supabase.
- Lines 54-68 search nearest chunks using `ORDER BY embedding <=> ?::vector LIMIT ?`.
- Lines 71-80 convert Java `List<Double>` embeddings into pgvector literal format like `[0.1,0.2,0.3]`.

If Supabase reports that type `vector` does not exist, change the casts in `VectorStoreService.java` lines 38 and 59 from:

```sql
?::vector
```

to:

```sql
?::extensions.vector
```

This depends on how the `vector` extension is exposed in your Supabase database.

## How The Frontend Works

The main frontend logic is in `frontend/src/App.jsx`.

- Lines 4-9 define React state for files, documents, query, answer, loading, and sources.
- Lines 15-36 upload selected files to `/api/documents/upload`.
- Lines 38-43 fetch stored document metadata from `/api/documents`.
- Lines 45-67 send the user's question to `/api/query`.
- Lines 76-83 render the upload UI.
- Lines 85-94 render the document list.
- Lines 96-102 render the question box.
- Lines 104-117 render the answer and source IDs.

The frontend dev server is configured in `frontend/vite.config.js`:

- Line 7 runs Vite on port `3000`.
- Lines 8-10 proxy `/api` calls to the backend at `http://localhost:8080`.

## Normal End-To-End Test

1. Start Supabase project.
2. Run the SQL schema in Supabase.
3. Confirm the pooler host works:

```powershell
Test-NetConnection aws-1-us-east-1.pooler.supabase.com -Port 5432
```

4. Start backend with `GOOGLE_API_KEY`, `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, and `SUPABASE_DB_PASSWORD`.
5. Start frontend with `npm run dev`.
6. Open `http://localhost:3000`.
7. Upload a small PDF or text file.
8. Check Supabase tables:

```sql
select * from documents order by created_at desc limit 5;
select id, document_id, vector_dims(embedding) from document_chunks order by created_at desc limit 5;
```

9. Ask a question about the uploaded file.
10. Confirm the answer is grounded in the document content.

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

### Upload succeeds in Gemini but fails in Supabase

Check backend logs:

```powershell
cd "D:\Projects\RAG with Vector DB\backend"
Get-Content .\logs\rag-search.log -Wait
```

If the log reaches `Step 5: Storing document metadata and chunk embeddings to Supabase`, Gemini worked and the issue is on the database side.

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

