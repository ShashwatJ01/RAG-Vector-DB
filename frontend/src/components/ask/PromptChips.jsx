import { PROMPT_SUGGESTIONS } from "../../constants/appConstants";

function PromptChips({ onSelectPrompt }) {
  return (
    <div className="prompt-block">
      <p>Ask a question to generate an answer from your workspace documents.</p>
      {PROMPT_SUGGESTIONS.map((prompt) => (
        <button className="prompt-chip" key={prompt} onClick={() => onSelectPrompt(prompt)}>
          {prompt}
        </button>
      ))}
    </div>
  );
}

export default PromptChips;
