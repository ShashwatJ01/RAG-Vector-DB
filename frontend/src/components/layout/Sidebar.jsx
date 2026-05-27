import { NAV_ITEMS } from "../../constants/appConstants";
import { isActiveNav, navIcon } from "../../utils/navigation";

function Sidebar({ page, onNavigate }) {
  return (
    <aside className="sidebar" aria-label="Main navigation">
      <div className="brand">
        <span className="brand-mark">AI</span>
        <div>
          <strong>AI Document Workspace</strong>
          <span>Document intelligence</span>
        </div>
      </div>

      {NAV_ITEMS.map((item) => (
        <button
          className={`nav-item ${isActiveNav(item, page) ? "active" : ""}`}
          key={item}
          onClick={() => onNavigate(item)}
        >
          <span>{navIcon(item)}</span>
          {item}
        </button>
      ))}
    </aside>
  );
}

export default Sidebar;
