function SourceEvidencePanel({ selectedSource, copyText }) {
  return (
    <aside className="card evidence-panel">
      <h3>Source Evidence</h3>
      {!selectedSource ? (
        <p className="muted">Select a citation to view the retrieved source text.</p>
      ) : (
        <>
          <dl className="evidence-list">
            <div>
              <dt>Document</dt>
              <dd>{selectedSource.fileName}</dd>
            </div>
            <div>
              <dt>Page</dt>
              <dd>{selectedSource.pageNumber}</dd>
            </div>
            <div>
              <dt>Chunk</dt>
              <dd>{selectedSource.chunkIndex}</dd>
            </div>
            <div>
              <dt>Similarity Score</dt>
              <dd>{selectedSource.similarityScore}</dd>
            </div>
          </dl>
          <h4>Retrieved Text</h4>
          <p className="source-text">"{selectedSource.content}"</p>
          <button className="secondary-button" onClick={() => copyText(selectedSource.content, "Source text copied.")}>
            Copy Source Text
          </button>
          <button className="ghost-button">Ask Follow-up</button>
        </>
      )}
    </aside>
  );
}

export default SourceEvidencePanel;
