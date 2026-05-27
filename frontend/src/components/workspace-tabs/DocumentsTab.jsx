import EmptyState from "../common/EmptyState";
import BulkActionBar from "../documents/BulkActionBar";
import DocumentFilters from "../documents/DocumentFilters";
import DocumentTable from "../documents/DocumentTable";
import DocumentUploadDropzone from "../documents/DocumentUploadDropzone";

function DocumentsTab(props) {
  return (
    <section className="card">
      <div className="section-heading">
        <div>
          <h3>Documents</h3>
          <p>Upload, select, and manage workspace documents.</p>
        </div>
      </div>

      <DocumentUploadDropzone
        pendingFiles={props.pendingFiles}
        setPendingFiles={props.setPendingFiles}
        uploadPendingFiles={props.uploadPendingFiles}
        loading={props.loading}
      />
      <DocumentFilters documentFilter={props.documentFilter} setDocumentFilter={props.setDocumentFilter} />
      <BulkActionBar selectedCount={props.selectedDocuments.length} onAskSelected={props.askSelected} />

      {props.documents.length === 0 ? (
        <EmptyState title="No documents uploaded." text="Upload PDFs or text files to start asking questions." />
      ) : (
        <DocumentTable
          documents={props.documents}
          filteredDocuments={props.filteredDocuments}
          selectedDocuments={props.selectedDocuments}
          toggleDocument={props.toggleDocument}
          setDocumentDrawer={props.setDocumentDrawer}
          askSingleDocument={props.askSingleDocument}
          removeDocument={props.removeDocument}
        />
      )}
    </section>
  );
}

export default DocumentsTab;
