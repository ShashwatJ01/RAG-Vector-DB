import Chip from "../common/Chip";
import StatusChip from "../common/StatusChip";
import { getKnowledgeSetupLabel } from "../../constants/appConstants";

function WorkspaceHeader({ workspace, documents = [], onSelectTab }) {
  const completed = documents.filter((document) => document.status === "Indexed" || document.status === "Completed").length;
  const processing = documents.filter((document) => document.status === "Processing" || document.status === "Queued").length;
  const chunks = documents.reduce((total, document) => total + (document.chunks || 0), 0);

  return (
    <div className="workspace-header card">
      <div className="workspace-identity">
        <div className="card-topline">
          <Chip>{workspace.category}</Chip>
          <Chip>{getKnowledgeSetupLabel(workspace)}</Chip>
          <StatusChip status={workspace.status} />
        </div>
        <h2>{workspace.name}</h2>
        <p>{workspace.description}</p>
        <div className="workspace-stat-strip" aria-label="Workspace summary">
          {[
            ["Documents", documents.length],
            ["Indexed", completed],
            ["Processing", processing],
            ["Chunks", chunks],
            ["Last Activity", workspace.lastActivity],
          ].map(([label, value]) => (
            <dl key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </dl>
          ))}
        </div>
      </div>
      <aside className="workspace-command-panel" aria-label="Workspace actions">
        <span>Primary workflow</span>
        <button className="primary-button" onClick={() => onSelectTab("Ask AI")}>
          Ask AI
        </button>
        <button className="secondary-button" onClick={() => onSelectTab("Documents")}>
          Upload Documents
        </button>
        <button className="ghost-button">Workspace Settings</button>
      </aside>
    </div>
  );
}

export default WorkspaceHeader;
