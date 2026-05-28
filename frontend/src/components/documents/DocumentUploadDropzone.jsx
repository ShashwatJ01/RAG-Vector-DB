function DocumentUploadDropzone({ pendingFiles, setPendingFiles, uploadPendingFiles, loading }) {
  return (
    <div className="dropzone">
      <strong>Drag and drop files here</strong>
      <span>or browse files from your computer</span>
      <small>Supported: PDF and TXT</small>
      <label className="file-button">
        Browse Files
        <input type="file" multiple accept=".pdf,.txt" onChange={(event) => setPendingFiles(Array.from(event.target.files))} />
      </label>
      <button className="primary-button" disabled={loading || pendingFiles.length === 0} onClick={uploadPendingFiles}>
        {loading ? "Uploading..." : "Upload"}
      </button>
      {pendingFiles.length > 0 && <p className="muted">{pendingFiles.length} file(s) selected</p>}
    </div>
  );
}

export default DocumentUploadDropzone;
