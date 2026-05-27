import { pageTitle } from "../../utils/navigation";

function Topbar({ page, selectedWorkspace, onCreateWorkspace }) {
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Workspace AI</p>
        <h1>{page === "workspace" && selectedWorkspace ? selectedWorkspace.name : pageTitle(page)}</h1>
      </div>
      <div className="topbar-actions">
        <span className="status-dot">Ready</span>
        <button className="primary-button" onClick={onCreateWorkspace}>
          + New Workspace
        </button>
      </div>
    </header>
  );
}

export default Topbar;
