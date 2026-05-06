import { useState } from "react";

function App() {
  const [files, setFiles] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [query, setQuery] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [sources, setSources] = useState([]);

  const handleFileChange = (event) => {
    setFiles(Array.from(event.target.files));
  };

  const uploadFiles = async () => {
    if (files.length === 0) {
      return;
    }
    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));

    setLoading(true);
    const response = await fetch("/api/documents/upload", {
      method: "POST",
      body: formData,
    });
    setLoading(false);

    if (response.ok) {
      await fetchDocuments();
      setFiles([]);
      setAnswer("Files uploaded successfully. You can now ask a question.");
    } else {
      setAnswer("Upload failed. Check the backend and try again.");
    }
  };

  const fetchDocuments = async () => {
    const response = await fetch("/api/documents");
    if (response.ok) {
      setDocuments(await response.json());
    }
  };

  const askQuery = async () => {
    if (!query.trim()) {
      return;
    }

    setLoading(true);
    const response = await fetch("/api/query", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ query }),
    });
    setLoading(false);

    if (response.ok) {
      const data = await response.json();
      setAnswer(data.answer);
      setSources(data.sources || []);
    } else {
      setAnswer("Query failed. Make sure documents are uploaded and try again.");
    }
  };

  return (
    <div className="app-shell">
      <header>
        <h1>RAG AI Search</h1>
        <p>Upload files and ask questions. Answers are grounded in your document content.</p>
      </header>

      <section className="card">
        <h2>Upload documents</h2>
        <input type="file" multiple onChange={handleFileChange} />
        <button onClick={uploadFiles} disabled={loading || files.length === 0}>
          {loading ? "Uploading..." : "Upload Files"}
        </button>
        <div className="meta-line">{files.length} file(s) selected</div>
      </section>

      <section className="card">
        <h2>Documents</h2>
        <ul>
          {documents.map((doc) => (
            <li key={doc.id}>
              {doc.fileName} ({doc.length} chars)
            </li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h2>Ask a question</h2>
        <textarea value={query} onChange={(event) => setQuery(event.target.value)} rows={4} placeholder="Ask something about the uploaded files" />
        <button onClick={askQuery} disabled={loading || !query.trim()}>
          {loading ? "Thinking..." : "Ask"}
        </button>
      </section>

      <section className="card result-card">
        <h2>Answer</h2>
        <div className="answer-box">{answer}</div>
        {sources.length > 0 && (
          <div className="sources">
            <h3>Sources</h3>
            <ul>
              {sources.map((source) => (
                <li key={source}>{source}</li>
              ))}
            </ul>
          </div>
        )}
      </section>
    </div>
  );
}

export default App;
