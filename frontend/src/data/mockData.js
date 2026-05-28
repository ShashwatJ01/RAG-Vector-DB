export const mockWorkspaces = [
  {
    id: "ws-1",
    name: "Vendor Contracts",
    description: "Agreements, amendments, and policy documents for vendor review.",
    category: "Legal",
    status: "Active",
    documentCount: 4,
    lastActivity: "Today",
  },
  {
    id: "ws-2",
    name: "Research Papers",
    description: "AI and machine learning papers for study and summarization.",
    category: "Research",
    status: "Active",
    documentCount: 8,
    lastActivity: "Yesterday",
  },
  {
    id: "ws-3",
    name: "Financial Reports",
    description: "Quarterly reports, statements, and financial analysis documents.",
    category: "Finance",
    status: "Active",
    documentCount: 6,
    lastActivity: "May 25",
  },
];

export const mockDocumentsByWorkspace = {
  "ws-1": [
    {
      id: "doc-1",
      fileName: "Agreement.pdf",
      documentType: "Contract",
      status: "Completed",
      chunks: 72,
      uploaded: "Today",
      embeddingDimension: 1536,
      fileSize: "1.8 MB",
      extension: "PDF",
    },
    {
      id: "doc-2",
      fileName: "Policy.pdf",
      documentType: "Policy",
      status: "Completed",
      chunks: 31,
      uploaded: "Today",
      embeddingDimension: 1536,
      fileSize: "900 KB",
      extension: "PDF",
    },
    {
      id: "doc-3",
      fileName: "Notes.txt",
      documentType: "Meeting Notes",
      status: "Processing",
      chunks: null,
      uploaded: "Today",
      embeddingDimension: null,
      fileSize: "120 KB",
      extension: "TXT",
    },
  ],
  "ws-2": [
    {
      id: "doc-4",
      fileName: "Transformer Survey.pdf",
      documentType: "Research Paper",
      status: "Completed",
      chunks: 88,
      uploaded: "Yesterday",
      embeddingDimension: 1536,
      fileSize: "2.4 MB",
      extension: "PDF",
    },
  ],
  "ws-3": [
    {
      id: "doc-5",
      fileName: "Q1 Statement.pdf",
      documentType: "Report",
      status: "Completed",
      chunks: 42,
      uploaded: "May 25",
      embeddingDimension: 1536,
      fileSize: "1.1 MB",
      extension: "PDF",
    },
  ],
};

export const mockAnswer = {
  answer:
    "The selected documents mention several key obligations, including timely invoice submission, compliance with the attached policy terms, and written approval before making material changes.",
  confidence: "High",
  sources: [
    {
      id: "source-1",
      documentId: "doc-1",
      fileName: "Agreement.pdf",
      pageNumber: 4,
      chunkIndex: 12,
      similarityScore: 0.89,
      content:
        "The contractor shall submit invoices monthly. Payment shall be made within thirty days after receipt of a valid invoice.",
    },
    {
      id: "source-2",
      documentId: "doc-2",
      fileName: "Policy.pdf",
      pageNumber: 2,
      chunkIndex: 6,
      similarityScore: 0.83,
      content:
        "All material changes require written approval before implementation. The policy applies to all active agreements.",
    },
  ],
};
