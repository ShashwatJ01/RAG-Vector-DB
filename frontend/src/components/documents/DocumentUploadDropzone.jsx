function DocumentUploadDropzone({ pendingFiles, setPendingFiles, uploadPendingFiles, loading }) {
  return (
    <div className="dropzone">
      <div>
        <strong>Add knowledge files</strong>
        <span>Drop PDFs or text files here, or browse from your computer.</span>
        <small>Supported: PDF and TXT</small>
      </div>
      <div className="upload-actions">
        <label className="file-button">
          Browse Files
          <input type="file" multiple accept=".pdf,.txt" onChange={(event) => setPendingFiles(Array.from(event.target.files))} />
        </label>
        <button className="primary-button" disabled={loading || pendingFiles.length === 0} onClick={uploadPendingFiles}>
          {loading ? "Uploading..." : "Upload"}
        </button>
      </div>
      {pendingFiles.length > 0 && (
        <div className="pending-files">
          <strong>{pendingFiles.length} ready to upload</strong>
          <span>{pendingFiles.map((file) => file.name).join(", ")}</span>
        </div>
      )}
    </div>
  );
}

export default DocumentUploadDropzone;
