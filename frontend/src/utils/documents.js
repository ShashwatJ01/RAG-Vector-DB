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

export function getAnswerScopeLabel(answerScope) {
  if (answerScope === "all") return "All workspace documents";
  if (answerScope === "single") return "Single document";
  return "Selected documents";
}
