import EmptyState from "../components/common/EmptyState";
import WorkspaceCard from "../components/workspaces/WorkspaceCard";
import WorkspaceFilters from "../components/workspaces/WorkspaceFilters";

function WorkspacesPage({
  filteredWorkspaces,
  search,
  setSearch,
  categoryFilter,
  setCategoryFilter,
  statusFilter,
  setStatusFilter,
  openWorkspace,
  archiveWorkspace,
  setWorkspaceModalOpen,
}) {
  return (
    <section className="page-stack">
      <div className="section-heading">
        <div>
          <h2>Workspaces</h2>
          <p>Create and manage document collections for AI-powered search and analysis.</p>
        </div>
        <button className="primary-button" onClick={() => setWorkspaceModalOpen(true)}>
          New Workspace
        </button>
      </div>

      <WorkspaceFilters
        search={search}
        setSearch={setSearch}
        categoryFilter={categoryFilter}
        setCategoryFilter={setCategoryFilter}
        statusFilter={statusFilter}
        setStatusFilter={setStatusFilter}
      />

      {filteredWorkspaces.length === 0 ? (
        <EmptyState
          title="No workspaces yet."
          text="Create your first workspace to organize documents and ask AI-powered questions."
          action="Create Workspace"
          onAction={() => setWorkspaceModalOpen(true)}
        />
      ) : (
        <div className="workspace-grid">
          {filteredWorkspaces.map((workspace) => (
            <WorkspaceCard key={workspace.id} workspace={workspace} onArchive={archiveWorkspace} onOpen={openWorkspace} />
          ))}
        </div>
      )}
    </section>
  );
}

export default WorkspacesPage;
