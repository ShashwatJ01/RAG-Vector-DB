import StatusChip from "../common/StatusChip";

function DocumentDetailsDrawer({ document, onClose, onAsk, onDelete }) {
  if (!document) return null;

  return (
    <aside className="drawer" aria-label="Document details">
      <button className="text-button" onClick={onClose}>
        Close
      </button>
      <h2>{document.fileName}</h2>
      <dl className="evidence-list">
        <div>
          <dt>Document Type</dt>
          <dd>{document.documentType}</dd>
        </div>
        <div>
          <dt>Status</dt>
          <dd>
            <StatusChip status={document.status} />
          </dd>
        </div>
        <div>
          <dt>Chunk Count</dt>
          <dd>{document.chunks ?? "--"}</dd>
        </div>
        <div>
          <dt>Uploaded Date</dt>
          <dd>{document.uploaded}</dd>
        </div>
        <div>
          <dt>Embedding Dimension</dt>
          <dd>{document.embeddingDimension ?? "--"}</dd>
        </div>
        <div>
          <dt>File Size</dt>
          <dd>{document.fileSize}</dd>
        </div>
        {document.errorMessage && (
          <div>
            <dt>Error</dt>
            <dd>{document.errorMessage}</dd>
          </div>
        )}
      </dl>
      <button className="primary-button full-width" onClick={() => onAsk(document)}>
        Ask this document
      </button>
      <button className="ghost-button full-width" disabled>
        Reprocess
      </button>
      <button className="danger-button full-width" onClick={() => onDelete(document.id)}>
        Delete
      </button>
    </aside>
  );
}

export default DocumentDetailsDrawer;
