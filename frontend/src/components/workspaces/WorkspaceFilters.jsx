import { CATEGORIES, WORKSPACE_STATUSES } from "../../constants/appConstants";

function WorkspaceFilters({ search, setSearch, categoryFilter, setCategoryFilter, statusFilter, setStatusFilter }) {
  return (
    <div className="toolbar">
      <label>
        <span>Search workspaces</span>
        <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search workspaces..." />
      </label>
      <label>
        <span>Category filter</span>
        <select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
          <option>All categories</option>
          {CATEGORIES.map((category) => (
            <option key={category}>{category}</option>
          ))}
        </select>
      </label>
      <label>
        <span>Status filter</span>
        <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
          {WORKSPACE_STATUSES.map((status) => (
            <option key={status}>{status}</option>
          ))}
        </select>
      </label>
    </div>
  );
}

export default WorkspaceFilters;
