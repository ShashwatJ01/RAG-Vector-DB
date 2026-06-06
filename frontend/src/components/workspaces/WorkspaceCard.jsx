import Chip from "../common/Chip";
import StatusChip from "../common/StatusChip";
import { getKnowledgeSetupLabel } from "../../constants/appConstants";

function WorkspaceCard({ workspace, onArchive, onOpen }) {
  return (
    <article className="workspace-card">
      <div className="card-topline">
        <Chip>{workspace.category}</Chip>
        <StatusChip status={workspace.status} />
      </div>
      <h3>{workspace.name}</h3>
      <p>{workspace.description}</p>
      <Chip>{getKnowledgeSetupLabel(workspace)}</Chip>
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
      <div className="workspace-actions">
        <button className="secondary-button full-width" onClick={() => onOpen(workspace.id)}>
          Open Workspace
        </button>
        <button className="ghost-button full-width" onClick={() => onArchive(workspace.id)}>
          Archive
        </button>
      </div>
    </article>
  );
}

export default WorkspaceCard;
