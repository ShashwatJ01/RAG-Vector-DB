import Chip from "../common/Chip";
import SourceCitationCard from "./SourceCitationCard";

function AnswerCard({ answer, selectedSource, setSelectedSource, askAi, copyText, setQuestion }) {
  if (!answer) return null;

  return (
    <article className="answer-card">
      <div className="card-topline">
        <h3>AI Answer</h3>
        <Chip>Confidence: {answer.confidence}</Chip>
      </div>
      <p>{answer.answer}</p>
      <dl className="inline-metadata">
        <div>
          <dt>Sources Used</dt>
          <dd>{answer.sources.length}</dd>
        </div>
        <div>
          <dt>Scope</dt>
          <dd>{answer.scope}</dd>
        </div>
      </dl>
      <div className="answer-actions">
        <button className="secondary-button" onClick={() => copyText(answer.answer, "Answer copied.")}>
          Copy Answer
        </button>
        <button className="ghost-button" onClick={() => setQuestion("")}>
          Ask Follow-up
        </button>
        <button className="ghost-button" onClick={askAi}>
          Regenerate
        </button>
      </div>
      <div className="citations">
        {answer.sources.map((source, index) => (
          <SourceCitationCard
            index={index}
            key={source.id}
            source={source}
            selected={selectedSource?.id === source.id}
            onSelect={setSelectedSource}
          />
        ))}
      </div>
    </article>
  );
}

export default AnswerCard;
