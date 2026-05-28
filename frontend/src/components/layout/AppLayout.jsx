import Sidebar from "./Sidebar";
import Topbar from "./Topbar";

function AppLayout({ page, selectedWorkspace, onNavigate, onCreateWorkspace, children }) {
  return (
    <div className="app-shell">
      <Sidebar page={page} onNavigate={onNavigate} />
      <main className="main-content">
        <Topbar page={page} selectedWorkspace={selectedWorkspace} onCreateWorkspace={onCreateWorkspace} />
        {children}
      </main>
    </div>
  );
}

export default AppLayout;
