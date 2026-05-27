import PlaceholderCards from "../common/PlaceholderCards";

function CompareTab() {
  return (
    <PlaceholderCards
      title="Compare Documents"
      text="Select two or more documents to compare key differences, overlapping information, and missing details."
      items={["Differences", "Overlap", "Missing Details"]}
      actions={["Select Documents", "Compare"]}
    />
  );
}

export default CompareTab;
