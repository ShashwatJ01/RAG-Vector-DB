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

export function mapApiDocument(document) {
  const extension = document.fileName?.split(".").pop()?.toUpperCase() || "FILE";

  return {
    id: document.id,
    fileName: document.fileName,
    documentType: getDocumentType(document.fileName),
    status: "Completed",
    chunks: document.chunks,
    uploaded: formatUploadedDate(document.createdAt),
    embeddingDimension: 1536,
    fileSize: formatFileSize(document.length),
    extension,
  };
}

export function getAnswerScopeLabel(answerScope) {
  if (answerScope === "all") return "All workspace documents";
  if (answerScope === "single") return "Single document";
  return "Selected documents";
}
