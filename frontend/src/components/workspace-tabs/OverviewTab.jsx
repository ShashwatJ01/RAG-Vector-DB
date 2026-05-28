function OverviewTab({ documents, setActiveTab }) {
  const completed = documents.filter((document) => document.status === "Completed").length;
  const processing = documents.filter((document) => document.status === "Processing" || document.status === "Queued").length;
  const chunks = documents.reduce((total, document) => total + (document.chunks || 0), 0);

  return (
    <div className="overview-grid">
      {[
        ["Total Documents", documents.length],
        ["Completed Documents", completed],
        ["Processing Documents", processing],
        ["Total Chunks", chunks],
        ["Questions Asked", 18],
      ].map(([label, value]) => (
        <article className="metric-card" key={label}>
          <span>{label}</span>
          <strong>{value}</strong>
        </article>
      ))}
      <section className="card quick-actions">
        <h3>Quick Actions</h3>
        <div>
          <button className="secondary-button" onClick={() => setActiveTab("Documents")}>
            Upload Documents
          </button>
          <button className="primary-button" onClick={() => setActiveTab("Ask AI")}>
            Ask AI
          </button>
          <button className="ghost-button">Generate Summary</button>
          <button className="ghost-button">Find Key Details</button>
        </div>
      </section>
      <section className="card activity-card">
        <h3>Recent Activity</h3>
        <ul>
          <li>Agreement.pdf uploaded</li>
          <li>User asked: "What are the key obligations?"</li>
          <li>Policy.pdf processed successfully</li>
        </ul>
      </section>
    </div>
  );
}

export default OverviewTab;
