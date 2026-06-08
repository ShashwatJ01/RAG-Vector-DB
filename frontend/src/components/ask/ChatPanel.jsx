import AnswerCard from "./AnswerCard";
import PromptChips from "./PromptChips";

const searchModes = [
  ["semantic", "Semantic"],
  ["keyword", "Keyword"],
  ["hybrid", "Hybrid"],
];

function ChatPanel({
  question,
  setQuestion,
  answer,
  selectedSource,
  setSelectedSource,
  askAi,
  loading,
  copyText,
  searchMode = "semantic",
  setSearchMode,
  semanticWeight = 1,
  setSemanticWeight,
  keywordWeight = 1,
  setKeywordWeight,
}) {
  return (
    <section className="card chat-panel">
      <div className="panel-heading">
        <span>Question console</span>
        <h3>Ask the workspace</h3>
      </div>
      {!answer && !loading && <PromptChips onSelectPrompt={setQuestion} />}
      <div className="retrieval-controls">
        <div className="retrieval-header">
          <span>Retrieval</span>
          <strong>{searchModes.find(([value]) => value === searchMode)?.[1] || "Semantic"}</strong>
        </div>
        <div className="search-mode-segmented" role="group" aria-label="Search mode">
          {searchModes.map(([value, label]) => (
            <button className={searchMode === value ? "active" : ""} key={value} onClick={() => setSearchMode(value)} type="button">
              {label}
            </button>
          ))}
        </div>
        {searchMode === "hybrid" && (
          <div className="weight-grid">
            <label>
              <span>Semantic {semanticWeight}x</span>
              <input
                type="range"
                min="0.25"
                max="2"
                step="0.25"
                value={semanticWeight}
                onChange={(event) => setSemanticWeight(Number(event.target.value))}
              />
            </label>
            <label>
              <span>Keyword {keywordWeight}x</span>
              <input
                type="range"
                min="0.25"
                max="2"
                step="0.25"
                value={keywordWeight}
                onChange={(event) => setKeywordWeight(Number(event.target.value))}
              />
            </label>
          </div>
        )}
      </div>
      <label className="question-box">
        <textarea value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="Ask a question about your documents..." />
      </label>
      <div className="ask-command-row">
        <button className="primary-button" disabled={loading || !question.trim()} onClick={askAi}>
          {loading ? "Generating answer..." : "Ask AI"}
        </button>
        <span>Answers are grounded in the selected source scope.</span>
      </div>
      {loading && <div className="linear-loader" />}
      <AnswerCard
        answer={answer}
        selectedSource={selectedSource}
        setSelectedSource={setSelectedSource}
        askAi={askAi}
        copyText={copyText}
        setQuestion={setQuestion}
      />
    </section>
  );
}

export default ChatPanel;
