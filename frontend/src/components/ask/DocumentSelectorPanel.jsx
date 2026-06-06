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
    if (scope === "single") {
      const firstDocumentId = selectedDocuments[0] || documents[0]?.id;
      setSelectedDocuments(firstDocumentId ? [firstDocumentId] : []);
    }
  };

  return (
    <aside className="card selector-panel">
      <div className="panel-heading">
        <span>Answer scope</span>
        <h3>Choose knowledge base</h3>
      </div>
      <div className="scope-segmented" role="group" aria-label="Answer scope">
        {[
          ["all", "All"],
          ["selected", "Selected"],
          ["single", "One"],
        ].map(([value, label]) => (
          <button className={answerScope === value ? "active" : ""} key={value} onClick={() => updateScope(value)} type="button">
            {label}
          </button>
        ))}
      </div>

      <div className="selected-summary">{usingText}</div>

      {answerScope === "all" ? (
        <div className="scope-note">
          <strong>{documents.length}</strong>
          <span>documents are available to retrieval for this question.</span>
        </div>
      ) : (
        <div className="document-picker">
          <h4>{answerScope === "single" ? "Pick one document" : "Pick documents"}</h4>
          {documents.length === 0 ? (
            <p className="muted">No documents available.</p>
          ) : (
            documents.map((document) => (
              <label className="check-row document-choice" key={document.id}>
                <input
                  type="checkbox"
                  checked={selectedDocuments.includes(document.id)}
                  onChange={() => toggleDocument(document.id)}
                />
                <span>{document.fileName}</span>
                <small>{document.status}</small>
              </label>
            ))
          )}
        </div>
      )}
    </aside>
  );
}

export default DocumentSelectorPanel;
