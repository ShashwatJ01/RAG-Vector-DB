export const CATEGORIES = [
  "General",
  "Legal",
  "Finance",
  "HR",
  "Research",
  "Contracts",
  "Reports",
  "Proposals",
  "Construction",
  "Other",
];

export const WORKSPACE_STATUSES = ["All statuses", "Active", "Archived"];

export const DOCUMENT_FILTERS = ["All", "Indexed", "Queued", "Processing", "Failed", "PDF", "TXT"];

export const WORKSPACE_TABS = ["Ask AI", "Documents", "Insights", "Compare", "Activity"];

export const NAV_ITEMS = ["Dashboard", "Workspaces", "Recent Documents", "Evaluation", "Settings"];

export const KNOWLEDGE_MODELS = [
  {
    label: "Gemini Flash Lite",
    value: "gemini-flash-lite",
    chatModel: "gemini-2.5-flash-lite",
    embeddingModel: "gemini-embedding-001",
    embeddingDimension: 1536,
    description: "Recommended",
  },
  {
    label: "Gemini Flash",
    value: "gemini-flash",
    chatModel: "gemini-2.5-flash",
    embeddingModel: "gemini-embedding-001",
    embeddingDimension: 1536,
    description: "Stronger answers",
  },
];

export const RETRIEVAL_MODES = [
  {
    label: "Fast",
    value: "fast",
    topK: 3,
    description: "Lower latency",
  },
  {
    label: "Balanced",
    value: "balanced",
    topK: 4,
    description: "Best default",
  },
  {
    label: "High Accuracy",
    value: "high",
    topK: 8,
    description: "More evidence",
  },
  {
    label: "Extra High",
    value: "extra-high",
    topK: 10,
    description: "Deep retrieval",
  },
];

export const SPEED_MODES = [
  {
    label: "Standard",
    value: "standard",
    description: "Steady quality",
  },
  {
    label: "Faster",
    value: "faster",
    description: "Quicker responses",
  },
];

export const EMBEDDING_MODELS = [
  {
    label: "Gemini Embedding 001",
    value: "gemini-embedding-001",
    description: "Text retrieval",
  },
  {
    label: "Gemini Embedding 2",
    value: "gemini-embedding-2",
    description: "Multimodal-ready",
  },
];

export function getKnowledgeModelOption(value) {
  return KNOWLEDGE_MODELS.find((option) => option.value === value) || KNOWLEDGE_MODELS[0];
}

export function getRetrievalModeOption(value) {
  return RETRIEVAL_MODES.find((option) => option.value === value) || RETRIEVAL_MODES[1];
}

export function getSpeedModeOption(value) {
  return SPEED_MODES.find((option) => option.value === value) || SPEED_MODES[0];
}

export function getKnowledgeSetupLabel(workspace) {
  const model = getKnowledgeModelOption(workspace?.knowledgeModel);
  const retrieval = getRetrievalModeOption(workspace?.retrievalMode);
  return `${model.label} | ${retrieval.label}`;
}

export const PROMPT_SUGGESTIONS = [
  "Summarize these documents",
  "What are the key obligations?",
  "Find important dates",
  "What information is missing?",
  "Compare selected documents",
];
