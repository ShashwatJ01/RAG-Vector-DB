import EmptyState from "../components/common/EmptyState";
import DocumentTable from "../components/documents/DocumentTable";

function RecentDocumentsPage({ documents, selectedDocuments, toggleDocument, setDocumentDrawer, askSingleDocument, removeDocument }) {
  return (
    <section className="page-stack">
      <div className="section-heading">
        <div>
          <h2>Recent Documents</h2>
          <p>Review the latest files uploaded across your workspaces.</p>
        </div>
      </div>

      {documents.length === 0 ? (
        <EmptyState title="No documents uploaded." text="Upload documents inside a workspace to see them here." />
      ) : (
        <section className="card">
          <DocumentTable
            documents={documents}
            filteredDocuments={documents}
            selectedDocuments={selectedDocuments}
            toggleDocument={toggleDocument}
            setDocumentDrawer={setDocumentDrawer}
            askSingleDocument={askSingleDocument}
            removeDocument={removeDocument}
          />
        </section>
      )}
    </section>
  );
}

export default RecentDocumentsPage;
