import { WORKSPACE_TABS } from "../../constants/appConstants";

function WorkspaceTabs({ activeTab, onSelectTab }) {
  return (
    <div className="tabs" role="tablist" aria-label="Workspace tabs">
      {WORKSPACE_TABS.map((tab) => (
        <button className={activeTab === tab ? "active" : ""} key={tab} onClick={() => onSelectTab(tab)}>
          {tab}
        </button>
      ))}
    </div>
  );
}

export default WorkspaceTabs;
