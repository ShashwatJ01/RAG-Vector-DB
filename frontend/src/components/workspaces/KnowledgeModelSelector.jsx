import {
  KNOWLEDGE_MODELS,
  RETRIEVAL_MODES,
  SPEED_MODES,
} from "../../constants/appConstants";

function KnowledgeModelSelector({ form, setForm }) {
  return (
    <fieldset className="knowledge-selector">
      <legend>Knowledge Model</legend>
      <label>
        <span>Model</span>
        <select
          value={form.knowledgeModel}
          onChange={(event) => setForm({ ...form, knowledgeModel: event.target.value })}
        >
          {KNOWLEDGE_MODELS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label} - {option.description}
            </option>
          ))}
        </select>
      </label>
      <label>
        <span>Retrieval</span>
        <select
          value={form.retrievalMode}
          onChange={(event) => setForm({ ...form, retrievalMode: event.target.value })}
        >
          {RETRIEVAL_MODES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label} - {option.description}
            </option>
          ))}
        </select>
      </label>
      <label>
        <span>Speed</span>
        <select value={form.speedMode} onChange={(event) => setForm({ ...form, speedMode: event.target.value })}>
          {SPEED_MODES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label} - {option.description}
            </option>
          ))}
        </select>
      </label>
    </fieldset>
  );
}

export default KnowledgeModelSelector;
