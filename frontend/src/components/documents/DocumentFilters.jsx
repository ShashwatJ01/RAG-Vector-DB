import { DOCUMENT_FILTERS } from "../../constants/appConstants";

function DocumentFilters({ documentFilter, setDocumentFilter }) {
  return (
    <div className="filter-row">
      {DOCUMENT_FILTERS.map((filter) => (
        <button className={documentFilter === filter ? "active" : ""} key={filter} onClick={() => setDocumentFilter(filter)}>
          {filter}
        </button>
      ))}
    </div>
  );
}

export default DocumentFilters;
