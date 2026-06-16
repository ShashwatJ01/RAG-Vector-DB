# RAG AI Search

RAG AI Search is a small full-stack application that lets you upload documents, generate embeddings with Google's Gemini API, and ask questions grounded in the uploaded content.

The project uses:

- `Spring Boot` for the backend API
- `React + Vite` for the frontend
- `Apache PDFBox` for PDF text extraction
- `Gemini API` for embeddings and answer generation

## Features

- Upload one or more `.pdf` or text-based files
- Extract text from uploaded documents
- Split document text into chunks for retrieval
- Generate vector embeddings using Gemini
- Retrieve the most relevant chunks for a user query
- Generate grounded answers from retrieved context
- Switch between light and dark mode in the frontend
- Store logs in a file for easier debugging

## How It Works

1. A user uploads one or more documents from the frontend.
2. The backend extracts text from each file.
3. The text is split into overlapping chunks.
4. Gemini embeddings are generated for each chunk.
5. The chunks and embeddings are stored in memory.
6. When the user asks a question, the backend embeds the query.
7. The backend finds the closest matching chunks using cosine similarity.
8. Gemini generates an answer using only the retrieved chunks.

## Project Structure

```text
RAG AI Search/
|-- backend/
|   |-- src/main/java/com/example/ragsearch/
|   |   |-- config/
|   |   |-- controller/
|   |   |-- model/
|   |   `-- service/
|   |-- src/main/resources/
|   `-- pom.xml
|-- frontend/
|   |-- src/
|   |-- package.json
|   `-- vite.config.js
`-- README.md
```

## Prerequisites

- `Java 17`
- `Maven 3.8+`
- `Node.js 18+`
- A valid `GOOGLE_API_KEY`

## Backend Setup

From the `backend` folder:

```powershell
$env:GOOGLE_API_KEY="your-api-key"
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

## Frontend Setup

From the `frontend` folder:

```powershell
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:3000
```

Vite proxies `/api` requests to the backend at `http://localhost:8080`.

## Frontend Theme

The React UI supports light and dark mode. Use the theme toggle in the topbar to switch modes.

Theme behavior:

- The selected theme is saved in `localStorage` as `rag-theme`.
- If no theme was selected yet, the app follows the browser or OS color-scheme preference.
- A small bootstrap script in `frontend/index.html` applies the saved theme before React loads, which avoids a flash of the wrong theme.
- Theme colors are controlled with CSS variables in `frontend/src/styles/base.css`; component styles consume those variables across cards, forms, badges, overlays, and the Ask AI workspace.

## Configuration

Backend settings are in [application.properties](</d:/Projects/RAG AI Search/backend/src/main/resources/application.properties:1>).

Current key settings:

```properties
server.port=8080
google.api.url=https://generativelanguage.googleapis.com
google.chat.model=gemini-2.5-flash-lite
google.embedding.model=gemini-embedding-001
logging.file.name=logs/rag-search.log
logging.level.com.example.ragsearch=DEBUG
```

## API Endpoints

### Upload Documents

```http
POST /api/documents/upload
```

Form field:

- `files`: one or more uploaded files

### List Uploaded Documents

```http
GET /api/documents
```

### Ask a Question

```http
POST /api/query
Content-Type: application/json
```

Example body:

```json
{
  "query": "What does the uploaded document say about the topic?"
}
```

## Logging

The backend is configured to write logs to:

```text
backend/logs/rag-search.log
```

Absolute path on this machine:

```text
D:\Projects\RAG AI Search\backend\logs\rag-search.log
```

To watch logs live in PowerShell:

```powershell
Get-Content .\logs\rag-search.log -Wait
```

Useful log markers:

- `Received upload request`
- `Step 1: Extracting text from file`
- `Step 3: Splitting text into chunks`
- `Step 4: Computing embeddings`
- `Runtime error during file upload`
- `Google API`

## Notes and Limitations

- Document data is stored in memory, so uploads are lost when the backend restarts.
- This project is best suited for demos, learning, and small-scale experimentation.
- Very large files or many uploads may increase latency because embeddings are generated per chunk.

## Troubleshooting

### Upload returns 500

Check:

- `GOOGLE_API_KEY` is set before starting the backend
- The uploaded file is not empty
- The file contains readable text
- The backend log file for the exact exception message

### No answer is generated

Check:

- Documents were uploaded successfully
- The query is not blank
- Gemini API requests are succeeding in the backend logs

### Frontend cannot reach backend

Check:

- Backend is running on port `8080`
- Frontend is running on port `3000`
- Vite proxy is enabled in [vite.config.js](</d:/Projects/RAG AI Search/frontend/vite.config.js:1>)

## Tech Stack

- `Java 17`
- `Spring Boot 3`
- `React 18`
- `Vite`
- `Apache PDFBox`
- `Gemini API`

## Future Improvements

- Persist documents and embeddings in a database or vector store
- Add support for more file types
- Show detailed error messages in the frontend UI
- Add tests for upload, query, and embedding flows
- Add pagination and metadata filtering for uploaded documents
