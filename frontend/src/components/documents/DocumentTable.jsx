import Chip from "../common/Chip";
import StatusChip from "../common/StatusChip";

function DocumentTable({
  documents,
  filteredDocuments,
  selectedDocuments,
  toggleDocument,
  setDocumentDrawer,
  askSingleDocument,
  removeDocument,
}) {
  const allSelected =
    filteredDocuments.length > 0 && filteredDocuments.every((document) => selectedDocuments.includes(document.id));

  const toggleAll = () => {
    if (allSelected) {
      filteredDocuments.filter((document) => selectedDocuments.includes(document.id)).forEach((document) => toggleDocument(document.id));
      return;
    }

    filteredDocuments.filter((document) => !selectedDocuments.includes(document.id)).forEach((document) => toggleDocument(document.id));
  };

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>
              <input aria-label="Select all documents" type="checkbox" checked={allSelected} onChange={toggleAll} />
            </th>
            <th>File Name</th>
            <th>Document Type</th>
            <th>Status</th>
            <th>Chunks</th>
            <th>Uploaded</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filteredDocuments.map((document) => (
            <tr key={document.id}>
              <td>
                <input
                  aria-label={`Select ${document.fileName}`}
                  type="checkbox"
                  checked={selectedDocuments.includes(document.id)}
                  onChange={() => toggleDocument(document.id)}
                />
              </td>
              <td>{document.fileName}</td>
              <td>
                <Chip>{document.documentType}</Chip>
              </td>
              <td>
                <StatusChip status={document.status} />
              </td>
              <td>{document.chunks ?? "--"}</td>
              <td>{document.uploaded}</td>
              <td className="table-actions">
                <button onClick={() => setDocumentDrawer(document)}>View</button>
                <button onClick={() => askSingleDocument(document)}>Ask</button>
                <button onClick={() => removeDocument(document.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default DocumentTable;
