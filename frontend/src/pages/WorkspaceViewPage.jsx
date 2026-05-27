import AskAiWorkspace from "../components/ask/AskAiWorkspace";
import WorkspaceHeader from "../components/workspaces/WorkspaceHeader";
import WorkspaceTabs from "../components/workspaces/WorkspaceTabs";
import ActivityTab from "../components/workspace-tabs/ActivityTab";
import CompareTab from "../components/workspace-tabs/CompareTab";
import DocumentsTab from "../components/workspace-tabs/DocumentsTab";
import InsightsTab from "../components/workspace-tabs/InsightsTab";
import OverviewTab from "../components/workspace-tabs/OverviewTab";

function WorkspaceViewPage(props) {
  const { workspace, activeTab, setActiveTab, setPage, documents } = props;

  return (
    <section className="page-stack">
      <button className="text-button" onClick={() => setPage("workspaces")}>
        Back to Workspaces
      </button>
      <WorkspaceHeader workspace={workspace} documentsCount={documents.length} onSelectTab={setActiveTab} />
      <WorkspaceTabs activeTab={activeTab} onSelectTab={setActiveTab} />

      {activeTab === "Overview" && <OverviewTab documents={documents} setActiveTab={setActiveTab} />}
      {activeTab === "Documents" && <DocumentsTab {...props} />}
      {activeTab === "Ask AI" && <AskAiWorkspace {...props} />}
      {activeTab === "Insights" && <InsightsTab />}
      {activeTab === "Compare" && <CompareTab />}
      {activeTab === "Activity" && <ActivityTab />}
    </section>
  );
}

export default WorkspaceViewPage;
