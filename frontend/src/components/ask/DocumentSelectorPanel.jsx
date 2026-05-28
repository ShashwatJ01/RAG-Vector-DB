function DocumentSelectorPanel({
  documents,
  selectedDocuments,
  setSelectedDocuments,
  toggleDocument,
  answerScope,
  setAnswerScope,
  selectedSingleDocument,
}) {
  const usingText =
    answerScope === "all"
      ? `Using all ${documents.length} workspace documents`
      : answerScope === "single"
        ? `Using ${selectedSingleDocument?.fileName || "one document"} only`
        : `Using ${selectedDocuments.length} selected document${selectedDocuments.length === 1 ? "" : "s"}`;

  const updateScope = (scope) => {
    setAnswerScope(scope);
    if (scope === "single") setSelectedDocuments(selectedDocuments.slice(0, 1));
  };

  return (
    <aside className="card selector-panel">
      <h3>Answer From</h3>
      {[
        ["all", "All workspace documents"],
        ["selected", "Selected documents"],
        ["single", "Single document"],
      ].map(([value, label]) => (
        <label className="radio-row" key={value}>
          <input type="radio" checked={answerScope === value} onChange={() => updateScope(value)} />
          <span>{label}</span>
        </label>
      ))}

      <div className="document-picker">
        <h4>Documents</h4>
        {documents.length === 0 ? (
          <p className="muted">No documents available.</p>
        ) : (
          documents.map((document) => (
            <label className={`check-row ${answerScope === "all" ? "secondary" : ""}`} key={document.id}>
              <input
                type="checkbox"
                disabled={answerScope === "all"}
                checked={answerScope === "all" || selectedDocuments.includes(document.id)}
                onChange={() => toggleDocument(document.id)}
              />
              <span>{document.fileName}</span>
            </label>
          ))
        )}
      </div>

      <div className="selected-summary">{usingText}</div>
    </aside>
  );
}

export default DocumentSelectorPanel;
