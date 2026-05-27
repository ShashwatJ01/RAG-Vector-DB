import { useMemo, useState } from "react";
import Snackbar from "./components/common/Snackbar";
import PlaceholderPage from "./components/common/PlaceholderPage";
import AppLayout from "./components/layout/AppLayout";
import DocumentDetailsDrawer from "./components/documents/DocumentDetailsDrawer";
import CreateWorkspaceModal from "./components/workspaces/CreateWorkspaceModal";
import { mockAnswer, mockDocumentsByWorkspace, mockWorkspaces } from "./data/mockData";
import WorkspacesPage from "./pages/WorkspacesPage";
import WorkspaceViewPage from "./pages/WorkspaceViewPage";
import { createQueuedDocuments, getAnswerScopeLabel } from "./utils/documents";

const emptyWorkspaceForm = { name: "", description: "", category: "General", tags: "" };

function App() {
  const [page, setPage] = useState("workspaces");
  const [workspaces, setWorkspaces] = useState(mockWorkspaces);
  const [documentsByWorkspace, setDocumentsByWorkspace] = useState(mockDocumentsByWorkspace);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState(null);
  const [activeTab, setActiveTab] = useState("Overview");
  const [selectedDocuments, setSelectedDocuments] = useState([]);
  const [answerScope, setAnswerScope] = useState("all");
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState(null);
  const [selectedSource, setSelectedSource] = useState(null);
  const [loading, setLoading] = useState(false);
  const [workspaceModalOpen, setWorkspaceModalOpen] = useState(false);
  const [documentDrawer, setDocumentDrawer] = useState(null);
  const [snackbar, setSnackbar] = useState("");
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("All categories");
  const [statusFilter, setStatusFilter] = useState("All statuses");
  const [documentFilter, setDocumentFilter] = useState("All");
  const [pendingFiles, setPendingFiles] = useState([]);
  const [workspaceForm, setWorkspaceForm] = useState(emptyWorkspaceForm);

  const selectedWorkspace = workspaces.find((workspace) => workspace.id === selectedWorkspaceId);
  const workspaceDocuments = selectedWorkspace ? documentsByWorkspace[selectedWorkspace.id] || [] : [];

  const filteredWorkspaces = useMemo(() => {
    return workspaces.filter((workspace) => {
      const matchesSearch = `${workspace.name} ${workspace.description}`.toLowerCase().includes(search.toLowerCase());
      const matchesCategory = categoryFilter === "All categories" || workspace.category === categoryFilter;
      const matchesStatus = statusFilter === "All statuses" || workspace.status === statusFilter;

      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [workspaces, search, categoryFilter, statusFilter]);

  const filteredDocuments = useMemo(() => {
    return workspaceDocuments.filter((document) => {
      if (documentFilter === "All") return true;
      if (documentFilter === "PDF" || documentFilter === "TXT") return document.extension === documentFilter;
      return document.status === documentFilter;
    });
  }, [workspaceDocuments, documentFilter]);

  const selectedDocumentRecords = workspaceDocuments.filter((document) => selectedDocuments.includes(document.id));
  const selectedSingleDocument = selectedDocumentRecords[0] || workspaceDocuments[0];

  const showSnackbar = (message) => {
    setSnackbar(message);
    window.setTimeout(() => setSnackbar(""), 3000);
  };

  const handleNavigate = (item) => {
    if (item === "Workspaces" || item === "Dashboard" || item === "Recent Documents") setPage("workspaces");
    if (item === "Evaluation") setPage("evaluation");
    if (item === "Settings") setPage("settings");
  };

  const openWorkspace = (workspaceId) => {
    setSelectedWorkspaceId(workspaceId);
    setActiveTab("Overview");
    setPage("workspace");
    setAnswer(null);
    setSelectedSource(null);
    setSelectedDocuments([]);
    setAnswerScope("all");
  };

  const createWorkspace = (event) => {
    event.preventDefault();
    if (!workspaceForm.name.trim()) return;

    setLoading(true);
    window.setTimeout(() => {
      const workspace = {
        id: `ws-${Date.now()}`,
        name: workspaceForm.name.trim(),
        description: workspaceForm.description.trim() || "A document workspace ready for AI-powered search and analysis.",
        category: workspaceForm.category,
        status: "Active",
        documentCount: 0,
        lastActivity: "Just now",
      };

      // TODO: Replace local workspace creation with POST /api/workspaces.
      setWorkspaces((current) => [workspace, ...current]);
      setDocumentsByWorkspace((current) => ({ ...current, [workspace.id]: [] }));
      setWorkspaceForm(emptyWorkspaceForm);
      setWorkspaceModalOpen(false);
      setLoading(false);
      showSnackbar("Workspace created.");
    }, 450);
  };

  const toggleDocument = (documentId) => {
    if (answerScope === "single") {
      setSelectedDocuments([documentId]);
      return;
    }

    setSelectedDocuments((current) =>
      current.includes(documentId) ? current.filter((id) => id !== documentId) : [...current, documentId],
    );
  };

  const askSelected = () => {
    setAnswerScope("selected");
    setActiveTab("Ask AI");
  };

  const askSingleDocument = (document) => {
    setSelectedDocuments([document.id]);
    setAnswerScope("single");
    setDocumentDrawer(null);
    setActiveTab("Ask AI");
  };

  const removeDocument = (documentId) => {
    setDocumentsByWorkspace((current) => ({
      ...current,
      [selectedWorkspace.id]: current[selectedWorkspace.id].filter((document) => document.id !== documentId),
    }));
    setSelectedDocuments((current) => current.filter((id) => id !== documentId));
    setDocumentDrawer(null);
    showSnackbar("Document removed.");
  };

  const uploadPendingFiles = () => {
    if (!pendingFiles.length || !selectedWorkspace) return;

    setLoading(true);
    window.setTimeout(() => {
      const newDocuments = createQueuedDocuments(pendingFiles);

      // TODO: Update upload API to include workspaceId when the backend supports workspaces.
      setDocumentsByWorkspace((current) => ({
        ...current,
        [selectedWorkspace.id]: [...newDocuments, ...(current[selectedWorkspace.id] || [])],
      }));
      setWorkspaces((current) =>
        current.map((workspace) =>
          workspace.id === selectedWorkspace.id
            ? { ...workspace, documentCount: workspace.documentCount + newDocuments.length, lastActivity: "Just now" }
            : workspace,
        ),
      );
      setPendingFiles([]);
      setLoading(false);
      showSnackbar("Document upload queued.");
    }, 600);
  };

  const askAi = () => {
    if (!question.trim()) return;
    if (answerScope !== "all" && selectedDocuments.length === 0) {
      showSnackbar("Select one or more documents first.");
      return;
    }

    setLoading(true);
    setAnswer(null);
    setSelectedSource(null);
    window.setTimeout(() => {
      // TODO: Update query API to support workspaceId, scope, and selected documentIds.
      setAnswer({
        ...mockAnswer,
        scope: getAnswerScopeLabel(answerScope),
      });
      setSelectedSource(mockAnswer.sources[0]);
      setLoading(false);
    }, 800);
  };

  const copyText = async (text, message) => {
    await navigator.clipboard?.writeText(text);
    showSnackbar(message);
  };

  return (
    <>
      <AppLayout
        page={page}
        selectedWorkspace={selectedWorkspace}
        onNavigate={handleNavigate}
        onCreateWorkspace={() => setWorkspaceModalOpen(true)}
      >
        {page === "workspaces" && (
          <WorkspacesPage
            filteredWorkspaces={filteredWorkspaces}
            search={search}
            setSearch={setSearch}
            categoryFilter={categoryFilter}
            setCategoryFilter={setCategoryFilter}
            statusFilter={statusFilter}
            setStatusFilter={setStatusFilter}
            openWorkspace={openWorkspace}
            setWorkspaceModalOpen={setWorkspaceModalOpen}
          />
        )}

        {page === "workspace" && selectedWorkspace && (
          <WorkspaceViewPage
            workspace={selectedWorkspace}
            documents={workspaceDocuments}
            filteredDocuments={filteredDocuments}
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            setPage={setPage}
            selectedDocuments={selectedDocuments}
            setSelectedDocuments={setSelectedDocuments}
            toggleDocument={toggleDocument}
            askSelected={askSelected}
            askSingleDocument={askSingleDocument}
            documentFilter={documentFilter}
            setDocumentFilter={setDocumentFilter}
            pendingFiles={pendingFiles}
            setPendingFiles={setPendingFiles}
            uploadPendingFiles={uploadPendingFiles}
            loading={loading}
            setDocumentDrawer={setDocumentDrawer}
            removeDocument={removeDocument}
            answerScope={answerScope}
            setAnswerScope={setAnswerScope}
            selectedDocumentRecords={selectedDocumentRecords}
            selectedSingleDocument={selectedSingleDocument}
            question={question}
            setQuestion={setQuestion}
            answer={answer}
            selectedSource={selectedSource}
            setSelectedSource={setSelectedSource}
            askAi={askAi}
            copyText={copyText}
          />
        )}

        {page === "evaluation" && (
          <PlaceholderPage title="Evaluation" text="Review answer quality, retrieval coverage, and source usefulness for workspace questions." />
        )}
        {page === "settings" && <PlaceholderPage title="Settings" text="Manage preferences, categories, integrations, and workspace defaults." />}
      </AppLayout>

      {workspaceModalOpen && (
        <CreateWorkspaceModal
          form={workspaceForm}
          setForm={setWorkspaceForm}
          createWorkspace={createWorkspace}
          loading={loading}
          onClose={() => setWorkspaceModalOpen(false)}
        />
      )}

      <DocumentDetailsDrawer
        document={documentDrawer}
        onClose={() => setDocumentDrawer(null)}
        onAsk={askSingleDocument}
        onDelete={removeDocument}
      />
      <Snackbar message={snackbar} />
    </>
  );
}

export default App;
