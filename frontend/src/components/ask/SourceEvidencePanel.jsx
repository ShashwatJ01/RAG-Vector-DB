function SourceEvidencePanel({ selectedSource, copyText }) {
  return (
    <aside className="card evidence-panel">
      <div className="panel-heading">
        <span>Inspector</span>
        <h3>Source Evidence</h3>
      </div>
      {!selectedSource ? (
        <div className="empty-inspector">
          <strong>No citation selected</strong>
          <p className="muted">Select a source card from the answer to inspect the retrieved text.</p>
        </div>
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
              <dt>Relevance Score</dt>
              <dd>{selectedSource.similarityScore}</dd>
            </div>
          </dl>
          <h4>Retrieved Text</h4>
          <p className="source-text">"{selectedSource.content}"</p>
          <button className="secondary-button" onClick={() => copyText(selectedSource.content, "Source text copied.")}>
            Copy Source Text
          </button>
          <button className="ghost-button" disabled>
            Ask Follow-up
          </button>
        </>
      )}
    </aside>
  );
}

export default SourceEvidencePanel;
