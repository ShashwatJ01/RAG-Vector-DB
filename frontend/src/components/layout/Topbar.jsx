import { pageTitle } from "../../utils/navigation";

const sunIcon = (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path d="M12 17.2a5.2 5.2 0 1 0 0-10.4 5.2 5.2 0 0 0 0 10.4Zm0-1.8a3.4 3.4 0 1 1 0-6.8 3.4 3.4 0 0 1 0 6.8ZM11.1 2h1.8v3.1h-1.8V2Zm0 16.9h1.8V22h-1.8v-3.1ZM2 11.1h3.1v1.8H2v-1.8Zm16.9 0H22v1.8h-3.1v-1.8ZM5 3.7 7.2 6 6 7.2 3.7 5 5 3.7Zm12.9 12.9 2.2 2.2-1.3 1.3-2.2-2.2 1.3-1.3Zm.9-12.9L20.1 5l-2.2 2.2L16.6 6l2.2-2.3ZM6 16.6l1.2 1.3L5 20.1l-1.3-1.3L6 16.6Z" />
  </svg>
);

const moonIcon = (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path d="M20.4 14.6A8.2 8.2 0 0 1 9.4 3.6 8.9 8.9 0 1 0 20.4 14.6ZM12 20.2A7.1 7.1 0 0 1 7.2 7.9a10 10 0 0 0 8.9 8.9A7 7 0 0 1 12 20.2Z" />
  </svg>
);

function Topbar({ page, selectedWorkspace, onCreateWorkspace, theme, onToggleTheme }) {
  const title = page === "workspace" && selectedWorkspace ? "Knowledge workbench" : pageTitle(page);
  const showCreateAction = page !== "workspace" && page !== "workspaces";
  const isDarkTheme = theme === "dark";
  const themeLabel = isDarkTheme ? "Switch to light mode" : "Switch to dark mode";

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">AI Document Workspace</p>
        <h1>{title}</h1>
      </div>
      <div className="topbar-actions">
        <span className="status-dot">Ready</span>
        <button
          className="theme-toggle"
          type="button"
          aria-label={themeLabel}
          aria-pressed={isDarkTheme}
          onClick={onToggleTheme}
          title={themeLabel}
        >
          {isDarkTheme ? sunIcon : moonIcon}
        </button>
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
