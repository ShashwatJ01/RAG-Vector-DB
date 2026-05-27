function SourceCitationCard({ source, index, selected, onSelect }) {
  return (
    <button className={`citation-card ${selected ? "active" : ""}`} onClick={() => onSelect(source)}>
      <strong>Source {index + 1}</strong>
      <span>{source.fileName}</span>
      <small>
        Page {source.pageNumber} | Chunk {source.chunkIndex} | Similarity {source.similarityScore}
      </small>
      <p>"{source.content}"</p>
    </button>
  );
}

export default SourceCitationCard;
