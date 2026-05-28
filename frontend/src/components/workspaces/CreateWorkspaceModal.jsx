import { CATEGORIES } from "../../constants/appConstants";

function CreateWorkspaceModal({ form, setForm, createWorkspace, loading, onClose }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <form className="modal" onSubmit={createWorkspace} role="dialog" aria-modal="true" aria-labelledby="create-workspace-title">
        <h2 id="create-workspace-title">Create Workspace</h2>
        <label>
          <span>Workspace Name *</span>
          <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label>
          <span>Description</span>
          <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        <label>
          <span>Category</span>
          <select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
            {CATEGORIES.map((category) => (
              <option key={category}>{category}</option>
            ))}
          </select>
        </label>
        <label>
          <span>Tags</span>
          <input value={form.tags} onChange={(event) => setForm({ ...form, tags: event.target.value })} placeholder="review, policy, Q2" />
        </label>
        <div className="modal-actions">
          <button className="ghost-button" type="button" onClick={onClose}>
            Cancel
          </button>
          <button className="primary-button" type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Workspace"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateWorkspaceModal;
