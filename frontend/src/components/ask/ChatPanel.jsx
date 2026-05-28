import AnswerCard from "./AnswerCard";
import PromptChips from "./PromptChips";

function ChatPanel({ question, setQuestion, answer, selectedSource, setSelectedSource, askAi, loading, copyText }) {
  return (
    <section className="card chat-panel">
      <h3>Ask anything about this workspace</h3>
      {!answer && !loading && <PromptChips onSelectPrompt={setQuestion} />}
      <label className="question-box">
        <span>Question</span>
        <textarea value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="Ask a question about your documents..." />
      </label>
      <button className="primary-button" disabled={loading || !question.trim()} onClick={askAi}>
        {loading ? "Generating answer..." : "Ask AI"}
      </button>
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
