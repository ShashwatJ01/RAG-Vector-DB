import { useEffect, useMemo, useState } from "react";
import {
  archiveWorkspace,
  askQuestion,
  createWorkspace as createWorkspaceApi,
  deleteDocument,
  listDocuments,
  listWorkspaces,
  uploadDocuments,
} from "./api/ragApi";
import Snackbar from "./components/common/Snackbar";
import PlaceholderPage from "./components/common/PlaceholderPage";
import AppLayout from "./components/layout/AppLayout";
import DocumentDetailsDrawer from "./components/documents/DocumentDetailsDrawer";
import CreateWorkspaceModal from "./components/workspaces/CreateWorkspaceModal";
import { getKnowledgeModelOption, getRetrievalModeOption } from "./constants/appConstants";
import DashboardPage from "./pages/DashboardPage";
import RecentDocumentsPage from "./pages/RecentDocumentsPage";
import WorkspacesPage from "./pages/WorkspacesPage";
import WorkspaceViewPage from "./pages/WorkspaceViewPage";
import { getAnswerScopeLabel, mapApiDocument, mapApiWorkspace } from "./utils/documents";

const emptyWorkspaceForm = {
  name: "",
  description: "",
  category: "General",
  tags: "",
  knowledgeModel: "gemini-flash-lite",
  retrievalMode: "balanced",
  speedMode: "standard",
};

const searchModeLabels = {
  semantic: "Semantic",
  keyword: "Keyword",
  hybrid: "Hybrid",
};

const themeStorageKey = "rag-theme";

const getPreferredTheme = () => (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");

const getInitialTheme = () => {
  try {
    const storedTheme = window.localStorage.getItem(themeStorageKey);
    if (storedTheme === "dark" || storedTheme === "light") return storedTheme;
  } catch {
    return getPreferredTheme();
  }

  return getPreferredTheme();
};

function App() {
  const [theme, setTheme] = useState(getInitialTheme);
  const [page, setPage] = useState("workspaces");
  const [workspaces, setWorkspaces] = useState([]);
  const [documentsByWorkspace, setDocumentsByWorkspace] = useState({});
  const [allDocuments, setAllDocuments] = useState([]);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState(null);
  const [activeTab, setActiveTab] = useState("Ask AI");
  const [selectedDocuments, setSelectedDocuments] = useState([]);
  const [answerScope, setAnswerScope] = useState("all");
  const [searchMode, setSearchMode] = useState("semantic");
  const [semanticWeight, setSemanticWeight] = useState(1);
  const [keywordWeight, setKeywordWeight] = useState(1);
  const [compareReranking, setCompareReranking] = useState(false);
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
  const hasPendingIngestion = allDocuments.some((document) => document.status === "Queued" || document.status === "Processing");

  useEffect(() => {
    refreshAppData();
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
    try {
      window.localStorage.setItem(themeStorageKey, theme);
    } catch {
      // The visual theme should still apply if browser storage is unavailable.
    }
  }, [theme]);

  useEffect(() => {
    if (!hasPendingIngestion) return undefined;

    const intervalId = window.setInterval(() => {
      refreshAppData();
    }, 3000);

    return () => window.clearInterval(intervalId);
  }, [hasPendingIngestion]);

  const refreshAppData = async () => {
    try {
      const workspaceResponse = await listWorkspaces();
      const nextWorkspaces = workspaceResponse.map(mapApiWorkspace);
      setWorkspaces(nextWorkspaces);
    } catch (error) {
      showSnackbar(error.message || "Could not load workspaces.");
    }

    try {
      const documentResponse = await listDocuments();
      const nextDocuments = documentResponse.map(mapApiDocument);
      const groupedDocuments = nextDocuments.reduce((groups, document) => {
        if (!document.workspaceId) return groups;
        return {
          ...groups,
          [document.workspaceId]: [...(groups[document.workspaceId] || []), document],
        };
      }, {});

      setAllDocuments(nextDocuments);
      setDocumentsByWorkspace(groupedDocuments);
    } catch (error) {
      showSnackbar(error.message || "Could not load documents.");
    }
  };

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
    if (item === "Dashboard") setPage("dashboard");
    if (item === "Workspaces") setPage("workspaces");
    if (item === "Recent Documents") setPage("recent-documents");
    if (item === "Evaluation") setPage("evaluation");
    if (item === "Settings") setPage("settings");
  };

  const openWorkspace = (workspaceId) => {
    setSelectedWorkspaceId(workspaceId);
    setActiveTab("Ask AI");
    setPage("workspace");
    setAnswer(null);
    setSelectedSource(null);
    setSelectedDocuments([]);
    setAnswerScope("all");
  };

  const createWorkspace = async (event) => {
    event.preventDefault();
    if (!workspaceForm.name.trim()) return;

    setLoading(true);
    try {
      const selectedModel = getKnowledgeModelOption(workspaceForm.knowledgeModel);
      const selectedRetrieval = getRetrievalModeOption(workspaceForm.retrievalMode);
      const workspace = await createWorkspaceApi({
        name: workspaceForm.name.trim(),
        description: workspaceForm.description.trim() || "A document workspace ready for AI-powered search and analysis.",
        category: workspaceForm.category,
        knowledgeModel: workspaceForm.knowledgeModel,
        chatModel: selectedModel.chatModel,
        embeddingModel: selectedModel.embeddingModel,
        embeddingDimension: selectedModel.embeddingDimension,
        retrievalMode: workspaceForm.retrievalMode,
        speedMode: workspaceForm.speedMode,
        topK: selectedRetrieval.topK,
      });

      const mappedWorkspace = mapApiWorkspace(workspace);
      setWorkspaces((current) => [mappedWorkspace, ...current]);
      setDocumentsByWorkspace((current) => ({ ...current, [mappedWorkspace.id]: [] }));
      setWorkspaceForm(emptyWorkspaceForm);
      setWorkspaceModalOpen(false);
      setSelectedWorkspaceId(mappedWorkspace.id);
      setActiveTab("Ask AI");
      setPage("workspace");
      showSnackbar("Workspace created.");
    } catch (error) {
      showSnackbar(error.message || "Could not create workspace.");
    } finally {
      setLoading(false);
    }
  };

  const handleArchiveWorkspace = async (workspaceId) => {
    setLoading(true);
    try {
      await archiveWorkspace(workspaceId);
      setWorkspaces((current) => current.filter((workspace) => workspace.id !== workspaceId));
      setDocumentsByWorkspace((current) => {
        const next = { ...current };
        delete next[workspaceId];
        return next;
      });
      if (selectedWorkspaceId === workspaceId) {
        setSelectedWorkspaceId(null);
        setPage("workspaces");
      }
      showSnackbar("Workspace archived.");
    } catch (error) {
      showSnackbar(error.message || "Could not archive workspace.");
    } finally {
      setLoading(false);
    }
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
    if (document.workspaceId && document.workspaceId !== selectedWorkspaceId) {
      setSelectedWorkspaceId(document.workspaceId);
      setPage("workspace");
      setActiveTab("Ask AI");
    }
    setSelectedDocuments([document.id]);
    setAnswerScope("single");
    setDocumentDrawer(null);
    setActiveTab("Ask AI");
  };

  const removeDocument = (documentId) => {
    const targetDocument = allDocuments.find((document) => document.id === documentId);
    const workspaceId = selectedWorkspace?.id || targetDocument?.workspaceId;

    setLoading(true);
    deleteDocument(documentId)
      .then(() => {
        setDocumentsByWorkspace((current) => ({
          ...current,
          [workspaceId]: (current[workspaceId] || []).filter((document) => document.id !== documentId),
        }));
        setAllDocuments((current) => current.filter((document) => document.id !== documentId));
        setSelectedDocuments((current) => current.filter((id) => id !== documentId));
        setDocumentDrawer(null);
        showSnackbar("Document removed.");
        return refreshAppData();
      })
      .catch((error) => showSnackbar(error.message || "Could not remove document."))
      .finally(() => setLoading(false));
  };

  const uploadPendingFiles = async () => {
    if (!pendingFiles.length || !selectedWorkspace) return;

    setLoading(true);
    try {
      await uploadDocuments(pendingFiles, selectedWorkspace.id);
      setPendingFiles([]);
      await refreshAppData();
      showSnackbar("Document queued for indexing.");
    } catch (error) {
      showSnackbar(error.message || "Could not upload documents.");
    } finally {
      setLoading(false);
    }
  };

  const askAi = async () => {
    if (!question.trim()) return;
    if (answerScope !== "all" && selectedDocuments.length === 0) {
      showSnackbar("Select one or more documents first.");
      return;
    }

    setLoading(true);
    setAnswer(null);
    setSelectedSource(null);
    try {
      const scopedDocumentIds = answerScope === "all" ? [] : selectedDocuments;
      const response = await askQuestion({
        query: question,
        workspaceId: selectedWorkspace?.id,
        documentIds: scopedDocumentIds,
        topK: selectedWorkspace?.topK || 4,
        searchMode,
        semanticWeight,
        keywordWeight,
        compareReranking,
      });

      const retrievalMode = `${searchModeLabels[searchMode]}${response.reranked ? " + Rerank" : ""}`;
      const nextAnswer = {
        ...response,
        scope: getAnswerScopeLabel(answerScope),
        retrievalMode,
        confidence: response.confidence || "Grounded",
      };

      setAnswer(nextAnswer);
      setSelectedSource(nextAnswer.sources[0] || null);
    } catch (error) {
      showSnackbar(error.message || "Could not answer the question.");
    } finally {
      setLoading(false);
    }
  };

  const copyText = async (text, message) => {
    await navigator.clipboard?.writeText(text);
    showSnackbar(message);
  };

  const toggleTheme = () => {
    setTheme((currentTheme) => (currentTheme === "dark" ? "light" : "dark"));
  };

  return (
    <>
      <AppLayout
        page={page}
        selectedWorkspace={selectedWorkspace}
        onNavigate={handleNavigate}
        onCreateWorkspace={() => setWorkspaceModalOpen(true)}
        theme={theme}
        onToggleTheme={toggleTheme}
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
            archiveWorkspace={handleArchiveWorkspace}
            setWorkspaceModalOpen={setWorkspaceModalOpen}
          />
        )}

        {page === "dashboard" && (
          <DashboardPage
            documents={allDocuments}
            workspaces={workspaces}
            onOpenWorkspaces={() => setPage("workspaces")}
            onOpenRecentDocuments={() => setPage("recent-documents")}
          />
        )}

        {page === "recent-documents" && (
          <RecentDocumentsPage
            documents={allDocuments}
            selectedDocuments={selectedDocuments}
            toggleDocument={toggleDocument}
            setDocumentDrawer={setDocumentDrawer}
            askSingleDocument={askSingleDocument}
            removeDocument={removeDocument}
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
            searchMode={searchMode}
            setSearchMode={setSearchMode}
            semanticWeight={semanticWeight}
            setSemanticWeight={setSemanticWeight}
            keywordWeight={keywordWeight}
            setKeywordWeight={setKeywordWeight}
            compareReranking={compareReranking}
            setCompareReranking={setCompareReranking}
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
