import AnswerCard from "./AnswerCard";
import PromptChips from "./PromptChips";

function ChatPanel({ question, setQuestion, answer, selectedSource, setSelectedSource, askAi, loading, copyText }) {
  return (
    <section className="card chat-panel">
      <div className="panel-heading">
        <span>Question console</span>
        <h3>Ask the workspace</h3>
      </div>
      {!answer && !loading && <PromptChips onSelectPrompt={setQuestion} />}
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
