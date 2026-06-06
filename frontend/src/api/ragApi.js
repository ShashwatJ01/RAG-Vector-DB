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

export function listWorkspaces() {
  return request("/api/workspaces");
}

export function createWorkspace(workspace) {
  return request("/api/workspaces", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(workspace),
  });
}

export function archiveWorkspace(workspaceId) {
  return request(`/api/workspaces/${workspaceId}`, {
    method: "DELETE",
  });
}

export function listDocuments(workspaceId) {
  const query = workspaceId ? `?workspaceId=${encodeURIComponent(workspaceId)}` : "";
  return request(`/api/documents${query}`);
}

export function uploadDocuments(files, workspaceId) {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  if (workspaceId) formData.append("workspaceId", workspaceId);

  return request("/api/documents/upload", {
    method: "POST",
    body: formData,
  });
}

export function getDocumentStatus(documentId) {
  return request(`/api/documents/${documentId}/status`);
}

export function deleteDocument(documentId) {
  return request(`/api/documents/${documentId}`, {
    method: "DELETE",
  });
}

export function askQuestion({ query, workspaceId, documentIds = [], topK = 4 }) {
  return request("/api/query", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ query, workspaceId, documentIds, topK }),
  });
}
