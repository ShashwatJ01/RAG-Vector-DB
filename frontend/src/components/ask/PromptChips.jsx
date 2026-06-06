import { PROMPT_SUGGESTIONS } from "../../constants/appConstants";

function PromptChips({ onSelectPrompt }) {
  return (
    <div className="prompt-block">
      <p>Start with a focused question or use a prompt below.</p>
      {PROMPT_SUGGESTIONS.map((prompt) => (
        <button className="prompt-chip" key={prompt} onClick={() => onSelectPrompt(prompt)}>
          {prompt}
        </button>
      ))}
    </div>
  );
}

export default PromptChips;
