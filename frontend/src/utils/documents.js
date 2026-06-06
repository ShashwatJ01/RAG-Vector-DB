export function createQueuedDocuments(files) {
  return files.map((file, index) => {
    const extension = file.name.split(".").pop().toUpperCase();

    return {
      id: `doc-${Date.now()}-${index}`,
      fileName: file.name,
      documentType: extension === "TXT" ? "Meeting Notes" : "Other",
      status: "Queued",
      chunks: null,
      uploaded: "Just now",
      embeddingDimension: null,
      fileSize: `${Math.max(file.size / 1024, 1).toFixed(0)} KB`,
      extension,
    };
  });
}

export function formatFileSize(bytes) {
  if (!bytes) return "--";
  if (bytes < 1024 * 1024) return `${Math.max(bytes / 1024, 1).toFixed(0)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function formatUploadedDate(value) {
  if (!value) return "--";

  const uploadedAt = new Date(value);
  if (Number.isNaN(uploadedAt.getTime())) return "--";

  return uploadedAt.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export function getDocumentType(fileName = "") {
  const extension = fileName.split(".").pop()?.toUpperCase();
  if (extension === "PDF") return "PDF";
  if (extension === "TXT") return "Text";
  return "Document";
}

export function mapDocumentStatus(status) {
  if (status === "QUEUED") return "Queued";
  if (status === "PROCESSING") return "Processing";
  if (status === "INDEXED") return "Indexed";
  if (status === "FAILED") return "Failed";
  return status || "Queued";
}

export function mapApiDocument(document) {
  const extension = document.fileName?.split(".").pop()?.toUpperCase() || "FILE";

  return {
    id: document.id,
    workspaceId: document.workspaceId,
    fileName: document.fileName,
    documentType: getDocumentType(document.fileName),
    status: mapDocumentStatus(document.status),
    chunks: document.chunks,
    uploaded: formatUploadedDate(document.createdAt),
    embeddingDimension: document.embeddingDimension || 1536,
    embeddingModel: document.embeddingModel,
    fileHash: document.fileHash,
    errorMessage: document.errorMessage,
    fileSize: formatFileSize(document.length),
    extension,
  };
}

export function mapApiWorkspace(workspace) {
  return {
    id: workspace.id,
    name: workspace.name,
    description: workspace.description,
    category: workspace.category,
    status: workspace.status,
    knowledgeModel: workspace.knowledgeModel || "gemini-flash-lite",
    chatModel: workspace.chatModel || "gemini-2.5-flash-lite",
    embeddingModel: workspace.embeddingModel || "gemini-embedding-001",
    embeddingDimension: workspace.embeddingDimension || 1536,
    retrievalMode: workspace.retrievalMode || "balanced",
    speedMode: workspace.speedMode || "standard",
    topK: workspace.topK || 4,
    documentCount: workspace.documentCount || 0,
    lastActivity: formatUploadedDate(workspace.lastActivityAt || workspace.createdAt),
  };
}

export function getAnswerScopeLabel(answerScope) {
  if (answerScope === "all") return "All workspace documents";
  if (answerScope === "single") return "Single document";
  return "Selected documents";
}
