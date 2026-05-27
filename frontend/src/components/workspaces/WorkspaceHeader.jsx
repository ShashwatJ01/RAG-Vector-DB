import Chip from "../common/Chip";
import StatusChip from "../common/StatusChip";

function WorkspaceHeader({ workspace, documentsCount, onSelectTab }) {
  return (
    <div className="workspace-header card">
      <div>
        <div className="card-topline">
          <Chip>{workspace.category}</Chip>
          <StatusChip status={workspace.status} />
        </div>
        <h2>{workspace.name}</h2>
        <p>{workspace.description}</p>
        <dl className="inline-metadata">
          <div>
            <dt>Documents</dt>
            <dd>{documentsCount}</dd>
          </div>
          <div>
            <dt>Last Activity</dt>
            <dd>{workspace.lastActivity}</dd>
          </div>
        </dl>
      </div>
      <div className="header-actions">
        <button className="secondary-button" onClick={() => onSelectTab("Documents")}>
          Upload Documents
        </button>
        <button className="primary-button" onClick={() => onSelectTab("Ask AI")}>
          Ask AI
        </button>
        <button className="ghost-button">Workspace Settings</button>
      </div>
    </div>
  );
}

export default WorkspaceHeader;
