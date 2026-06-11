function SourceCitationCard({ source, index, selected, onSelect }) {
  const rerankScore = typeof source.rerankScore === "number" ? source.rerankScore.toFixed(2) : "--";

  return (
    <button className={`citation-card ${selected ? "active" : ""}`} onClick={() => onSelect(source)}>
      <strong>Source {index + 1}</strong>
      <span>{source.fileName}</span>
      <small>
        Chunk {source.chunkIndex + 1} | Relevance {source.similarityScore ?? "--"}
      </small>
      <small className="rank-line">
        Final #{source.finalRank ?? index + 1} | Original #{source.originalRank ?? "--"} | Rerank {rerankScore}
      </small>
      <p>"{source.content}"</p>
    </button>
  );
}

export default SourceCitationCard;
