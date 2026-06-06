import { NAV_ITEMS } from "../../constants/appConstants";
import { isActiveNav } from "../../utils/navigation";

const icons = {
  Dashboard: (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 13h7V4H4v9Zm0 7h7v-5H4v5Zm9 0h7v-9h-7v9Zm0-16v5h7V4h-7Z" />
    </svg>
  ),
  Workspaces: (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M3 6.5A2.5 2.5 0 0 1 5.5 4h4.7l1.8 2h6.5A2.5 2.5 0 0 1 21 8.5v9A2.5 2.5 0 0 1 18.5 20h-13A2.5 2.5 0 0 1 3 17.5v-11Zm2.5-.7a.7.7 0 0 0-.7.7v11c0 .39.31.7.7.7h13a.7.7 0 0 0 .7-.7v-9a.7.7 0 0 0-.7-.7h-7.3l-1.8-2H5.5Z" />
    </svg>
  ),
  "Recent Documents": (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M7 3h7.2L19 7.8V19a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Zm6.5 1.9V9h4.1l-4.1-4.1ZM7 4.8a.2.2 0 0 0-.2.2v14c0 .11.09.2.2.2h10a.2.2 0 0 0 .2-.2v-8.2h-5.5v-6H7Zm1.8 8.4h6.4V15H8.8v-1.8Zm0 3.4h4.9v1.8H8.8v-1.8Z" />
    </svg>
  ),
  Evaluation: (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M9.4 16.2 5.8 12.6 4.5 14l4.9 4.9 10-10L18 7.5l-8.6 8.7ZM5 5h14v2H5V5Z" />
    </svg>
  ),
  Settings: (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M19.4 13.5c.08-.49.08-1.01 0-1.5l2-1.5-2-3.5-2.4 1a7.2 7.2 0 0 0-1.3-.8L15.4 4h-4l-.4 3.2c-.46.2-.9.47-1.3.8l-2.4-1-2 3.5 2 1.5a6 6 0 0 0 0 1.5l-2 1.5 2 3.5 2.4-1c.4.33.84.6 1.3.8l.4 3.2h4l.4-3.2c.46-.2.9-.47 1.3-.8l2.4 1 2-3.5-2.1-1.5ZM13.4 20h-.8l-.3-2.7-.6-.2a5.1 5.1 0 0 1-1.7-1l-.5-.4-2 .8-.4-.7 1.7-1.3-.1-.7a4.4 4.4 0 0 1 0-2.1l.1-.7-1.7-1.3.4-.7 2 .8.5-.4c.5-.43 1.08-.76 1.7-1l.6-.2.3-2.7h.8l.3 2.7.6.2c.62.24 1.2.57 1.7 1l.5.4 2-.8.4.7-1.7 1.3.1.7a4.4 4.4 0 0 1 0 2.1l-.1.7 1.7 1.3-.4.7-2-.8-.5.4c-.5.43-1.08.76-1.7 1l-.6.2-.3 2.7ZM13 10a3 3 0 1 0 0 6 3 3 0 0 0 0-6Zm0 1.8a1.2 1.2 0 1 1 0 2.4 1.2 1.2 0 0 1 0-2.4Z" />
    </svg>
  ),
};

function Sidebar({ page, onNavigate }) {
  return (
    <aside className="sidebar" aria-label="Main navigation">
      <div className="brand compact-brand" aria-label="AI Document Workspace">
        <span className="brand-mark">AI</span>
      </div>

      <nav className="activity-bar" aria-label="Primary">
        {NAV_ITEMS.map((item) => (
          <button
            aria-label={item}
            className={`nav-item ${isActiveNav(item, page) ? "active" : ""}`}
            data-tooltip={item}
            key={item}
            onClick={() => onNavigate(item)}
            title={item}
          >
            {icons[item]}
          </button>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
