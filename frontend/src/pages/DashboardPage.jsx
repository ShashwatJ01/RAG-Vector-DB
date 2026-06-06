function DashboardPage({ documents, workspaces, onOpenWorkspaces, onOpenRecentDocuments }) {
  const completed = documents.filter((document) => document.status === "Indexed" || document.status === "Completed").length;
  const chunks = documents.reduce((total, document) => total + (document.chunks || 0), 0);
  const activeWorkspaces = workspaces.filter((workspace) => workspace.status === "Active").length;
  const latestDocuments = documents.slice(0, 5);

  return (
    <section className="page-stack">
      <div className="section-heading">
        <div>
          <h2>Dashboard</h2>
          <p>Monitor indexed documents, workspace coverage, and recent knowledge-base activity.</p>
        </div>
        <div className="topbar-actions">
          <button className="secondary-button" onClick={onOpenRecentDocuments}>
            Recent Documents
          </button>
          <button className="primary-button" onClick={onOpenWorkspaces}>
            Workspaces
          </button>
        </div>
      </div>

      <div className="overview-grid">
        {[
          ["Active Workspaces", activeWorkspaces],
          ["Total Documents", documents.length],
          ["Indexed Documents", completed],
          ["Total Chunks", chunks],
          ["Avg Chunks", documents.length ? Math.round(chunks / documents.length) : 0],
        ].map(([label, value]) => (
          <article className="metric-card" key={label}>
            <span>{label}</span>
            <strong>{value}</strong>
          </article>
        ))}
      </div>

      <section className="card">
        <div className="section-heading">
          <div>
            <h3>Latest Documents</h3>
            <p>Most recently uploaded files across all workspaces.</p>
          </div>
        </div>
        {latestDocuments.length === 0 ? (
          <p className="muted">No documents uploaded yet.</p>
        ) : (
          <div className="compact-list">
            {latestDocuments.map((document) => (
              <article key={document.id}>
                <strong>{document.fileName}</strong>
                <span>{document.uploaded}</span>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}

export default DashboardPage;
