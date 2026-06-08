import ChatPanel from "./ChatPanel";
import DocumentSelectorPanel from "./DocumentSelectorPanel";
import SourceEvidencePanel from "./SourceEvidencePanel";

function AskAiWorkspace(props) {
  return (
    <div className="ask-grid">
      <DocumentSelectorPanel
        documents={props.documents}
        selectedDocuments={props.selectedDocuments}
        setSelectedDocuments={props.setSelectedDocuments}
        toggleDocument={props.toggleDocument}
        answerScope={props.answerScope}
        setAnswerScope={props.setAnswerScope}
        selectedSingleDocument={props.selectedSingleDocument}
      />
      <ChatPanel
        question={props.question}
        setQuestion={props.setQuestion}
        answer={props.answer}
        selectedSource={props.selectedSource}
        setSelectedSource={props.setSelectedSource}
        askAi={props.askAi}
        loading={props.loading}
        copyText={props.copyText}
        searchMode={props.searchMode}
        setSearchMode={props.setSearchMode}
        semanticWeight={props.semanticWeight}
        setSemanticWeight={props.setSemanticWeight}
        keywordWeight={props.keywordWeight}
        setKeywordWeight={props.setKeywordWeight}
      />
      <SourceEvidencePanel selectedSource={props.selectedSource} copyText={props.copyText} />
    </div>
  );
}

export default AskAiWorkspace;
