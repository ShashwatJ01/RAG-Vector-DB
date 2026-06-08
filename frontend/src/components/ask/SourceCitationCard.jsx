function SourceCitationCard({ source, index, selected, onSelect }) {
  return (
    <button className={`citation-card ${selected ? "active" : ""}`} onClick={() => onSelect(source)}>
      <strong>Source {index + 1}</strong>
      <span>{source.fileName}</span>
      <small>
        Chunk {source.chunkIndex + 1} | Relevance {source.similarityScore ?? "--"}
      </small>
      <p>"{source.content}"</p>
    </button>
  );
}

export default SourceCitationCard;
