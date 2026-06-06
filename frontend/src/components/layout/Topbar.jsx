import { pageTitle } from "../../utils/navigation";

function Topbar({ page, selectedWorkspace, onCreateWorkspace }) {
  const title = page === "workspace" && selectedWorkspace ? "Knowledge workbench" : pageTitle(page);
  const showCreateAction = page !== "workspace" && page !== "workspaces";

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">AI Document Workspace</p>
        <h1>{title}</h1>
      </div>
      <div className="topbar-actions">
        <span className="status-dot">Ready</span>
        {showCreateAction && (
          <button className="secondary-button" onClick={onCreateWorkspace}>
            New Workspace
          </button>
        )}
      </div>
    </header>
  );
}

export default Topbar;
