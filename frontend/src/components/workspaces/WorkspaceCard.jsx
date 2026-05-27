import Chip from "../common/Chip";
import StatusChip from "../common/StatusChip";

function WorkspaceCard({ workspace, onOpen }) {
  return (
    <article className="workspace-card">
      <div className="card-topline">
        <Chip>{workspace.category}</Chip>
        <StatusChip status={workspace.status} />
      </div>
      <h3>{workspace.name}</h3>
      <p>{workspace.description}</p>
      <dl className="metadata-grid">
        <div>
          <dt>Documents</dt>
          <dd>{workspace.documentCount}</dd>
        </div>
        <div>
          <dt>Last Activity</dt>
          <dd>{workspace.lastActivity}</dd>
        </div>
      </dl>
      <button className="secondary-button full-width" onClick={() => onOpen(workspace.id)}>
        Open Workspace
      </button>
    </article>
  );
}

export default WorkspaceCard;
