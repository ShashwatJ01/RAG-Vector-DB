function BulkActionBar({ selectedCount, onAskSelected }) {
  if (selectedCount === 0) return null;

  return (
    <div className="bulk-bar">
      <strong>
        {selectedCount} document{selectedCount === 1 ? "" : "s"} selected
      </strong>
      <button className="primary-button" onClick={onAskSelected}>
        Ask selected
      </button>
      <button className="ghost-button" disabled>
        Summarize
      </button>
      <button className="ghost-button" disabled>
        Compare
      </button>
      <button className="ghost-button" disabled>
        Delete
      </button>
      <button className="ghost-button" disabled>
        Reprocess
      </button>
    </div>
  );
}

export default BulkActionBar;
