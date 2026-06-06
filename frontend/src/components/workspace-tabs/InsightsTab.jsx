import PlaceholderCards from "../common/PlaceholderCards";

function InsightsTab() {
  return (
    <PlaceholderCards
      title="Insights"
      text="Generate summaries, extract key details, identify risks, and find missing information across this workspace."
      items={["Summary", "Key Details", "Risks", "Missing Information", "Suggested Questions"]}
      actions={["Generate Workspace Summary", "Extract Key Details", "Find Risks", "Find Missing Info"]}
    />
  );
}

export default InsightsTab;
