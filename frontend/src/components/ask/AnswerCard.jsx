import Chip from "../common/Chip";
import SourceCitationCard from "./SourceCitationCard";

function AnswerCard({ answer, selectedSource, setSelectedSource, askAi, copyText, setQuestion }) {
  if (!answer) return null;
  const sources = answer.sources || [];
  const comparison = answer.rerankComparison;

  return (
    <article className="answer-card">
      <div className="answer-header">
        <div className="panel-heading">
          <span>Grounded answer</span>
          <h3>AI Answer</h3>
        </div>
        <Chip>Confidence: {answer.confidence}</Chip>
      </div>
      <p className="answer-text">{answer.answer}</p>
      <dl className="inline-metadata">
        <div>
          <dt>Sources Used</dt>
          <dd>{sources.length}</dd>
        </div>
        <div>
          <dt>Scope</dt>
          <dd>{answer.scope}</dd>
        </div>
        <div>
          <dt>Retrieval</dt>
          <dd>{answer.retrievalMode || "Semantic"}</dd>
        </div>
        <div>
          <dt>Pool</dt>
          <dd>
            {answer.retrievalTopN && answer.finalTopK ? `${answer.retrievalTopN} -> ${answer.finalTopK}` : "--"}
          </dd>
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
      {comparison && (
        <section className="rerank-comparison">
          <div className="comparison-topline">
            <h4>Without reranking</h4>
            <span>{comparison.sourceOverlap} source overlap</span>
          </div>
          <p>{comparison.baselineAnswer}</p>
        </section>
      )}
      <div className="citations">
        {sources.map((source, index) => (
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
