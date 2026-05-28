const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, options);

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      message = body.error || message;
    } catch {
      // The backend may return an empty response body for some errors.
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

export function listDocuments() {
  return request("/api/documents");
}

export function uploadDocuments(files) {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));

  return request("/api/documents/upload", {
    method: "POST",
    body: formData,
  });
}

export function deleteDocument(documentId) {
  return request(`/api/documents/${documentId}`, {
    method: "DELETE",
  });
}

export function askQuestion({ query, documentIds = [], topK = 4 }) {
  return request("/api/query", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ query, documentIds, topK }),
  });
}
