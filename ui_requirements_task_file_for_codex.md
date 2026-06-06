# UI Requirements / Task File: Generic AI Document Workspace

## Purpose

Build the frontend UI for a generic AI-powered document intelligence application.

The application should allow users to:

1. Create and manage workspaces.
2. Open a workspace.
3. Upload documents inside a workspace.
4. View all documents related to that workspace.
5. Select all, one, or multiple documents.
6. Ask AI questions using either:
   - all documents in the workspace,
   - selected documents only,
   - or a single/current document.
7. View AI answers with source citations and source evidence.
8. Keep the UI generic, not construction-specific.

This task is only for the UI/frontend for now. Do not implement backend database/schema changes unless explicitly required later.

---

## Current App Context

The existing application is a full-stack RAG app with:

- React + Vite frontend
- Spring Boot backend
- Supabase Postgres with pgvector
- Gemini embeddings and answer generation
- PDF/text upload
- document chunking
- vector search
- answer generation from retrieved chunks

The existing UI is currently simple. This task is to redesign/extend the frontend into a polished, generic document workspace experience.

---

## Important Product Direction

Do not make the product construction-specific.

Use generic terminology:

- Use `Workspace`, not `Project`.
- Use `Documents`, not construction documents.
- Use `Insights`, not construction risks.
- Use `Source Evidence`, not construction source.
- Use `Category`, not department/project type.

The app should support multiple domains, such as:

- legal documents
- finance documents
- HR policies
- research papers
- contracts
- proposals
- reports
- general business documents
- construction documents, if the user chooses that category

Construction can be one optional category, but the UI should not be designed only around construction.

---

## Recommended App Name

Use a generic product title in the UI:

```text
AI Document Workspace
```

Alternative acceptable names:

```text
Document Intelligence Workspace
Workspace AI
AI Knowledge Workspace
```

---

## Navigation Structure

Create a main app shell with a left sidebar and main content area.

### Sidebar Items

```text
Dashboard
Workspaces
Recent Documents
Evaluation
Settings
```

For now, the required screens are:

1. Workspaces Page
2. Workspace View Page
3. Documents Tab inside Workspace View
4. Ask AI Tab inside Workspace View

The following tabs/pages can be placeholder UI for now:

1. Overview Tab
2. Insights Tab
3. Compare Tab
4. Activity Tab
5. Evaluation Page
6. Settings Page

---

## Global Layout Requirements

Use a clean SaaS-style UI.

### Layout

```text
 ---------------------------------------------------------------
| Sidebar              | Top Header                            |
|                      |---------------------------------------|
| Dashboard            | Main Content                          |
| Workspaces           |                                       |
| Recent Documents     |                                       |
| Evaluation           |                                       |
| Settings             |                                       |
 ---------------------------------------------------------------
```

### Visual Style

Use:

- clean card-based layout
- light background
- white cards
- subtle borders
- rounded corners
- clear spacing
- modern SaaS dashboard styling
- status badges/chips
- loading states
- empty states

The UI should look professional enough to include in a portfolio demo.

---

## Suggested Tech

Use the existing React + Vite app.

If Material UI is already used in the project, use MUI components.

Recommended MUI components:

- `Drawer`
- `AppBar`
- `Toolbar`
- `Box`
- `Grid`
- `Card`
- `CardContent`
- `Button`
- `TextField`
- `Chip`
- `Tabs`
- `Tab`
- `Dialog`
- `Table`
- `TableHead`
- `TableBody`
- `TableRow`
- `TableCell`
- `Checkbox`
- `CircularProgress`
- `LinearProgress`
- `Snackbar`
- `Alert`
- `IconButton`
- `Tooltip`

If MUI is not used, use regular CSS or another existing styling approach already present in the app.

---

## Routing Requirements

Use React Router if it is already present or easy to add.

Recommended routes:

```text
/
 /workspaces
 /workspaces/:workspaceId
 /evaluation
 /settings
```

Inside `/workspaces/:workspaceId`, use tabs instead of separate routes initially.

Tabs:

```text
Overview
Documents
Ask AI
Insights
Compare
Activity
```

The selected tab can be stored in local state first.

Optional enhancement:

```text
/workspaces/:workspaceId?tab=documents
/workspaces/:workspaceId?tab=ask
```

---

# Page 1: Workspaces Page

## Purpose

The Workspaces page is the landing area where users create and open document workspaces.

A workspace is a generic container for related documents.

Examples:

```text
Vendor Contracts
HR Policy Review
Research Papers
Costco Proposal Review
Financial Reports
Legal Case Docs
```

---

## Workspaces Page UI

### Header

```text
Workspaces                                      [+ New Workspace]
Create and manage document collections for AI-powered search and analysis.
```

### Search and Filters

Include:

```text
Search workspaces...
Category filter
Status filter
```

Filters can be UI-only for now.

### Workspace Cards or Table

Preferred: card grid for a polished visual layout.

Each workspace card should show:

```text
Workspace Name
Description
Category
Document count
Last activity
Status
Open Workspace button
```

Example card:

```text
Vendor Contracts

Review and search vendor agreements, amendments, and policy documents.

Category: Legal
Documents: 12
Last Activity: Today
Status: Active

[Open Workspace]
```

### Empty State

If no workspaces exist:

```text
No workspaces yet.
Create a workspace to upload documents and start asking questions.

[Create Workspace]
```

---

## Create Workspace Modal

When the user clicks `+ New Workspace`, show a modal.

Fields:

```text
Workspace Name *
Description
Category
Tags
```

Category options:

```text
General
Legal
Finance
HR
Research
Contracts
Reports
Proposals
Construction
Other
```

Buttons:

```text
Cancel
Create Workspace
```

### UI-only behavior for now

If backend endpoints are not available, store created workspaces in local React state or mock data.

When a workspace is created:

1. Close modal.
2. Show success snackbar.
3. Add workspace to workspace list.
4. Allow user to open the workspace.

---

# Page 2: Workspace View Page

## Purpose

The Workspace View is the main area where users manage documents and ask AI questions within a workspace.

---

## Workspace Header

At the top of the page, show:

```text
< Back to Workspaces

Workspace Name
Description

Category: Legal
Documents: 12
Last Activity: Today
Status: Active
```

Actions:

```text
Upload Documents
Ask AI
Workspace Settings
```

---

## Workspace Tabs

Use tabs:

```text
Overview | Documents | Ask AI | Insights | Compare | Activity
```

Required tabs for this task:

1. Overview
2. Documents
3. Ask AI

Placeholder tabs are acceptable for:

1. Insights
2. Compare
3. Activity

---

# Tab 1: Overview

## Purpose

Give the user a quick summary of the workspace.

## UI Sections

### Summary Cards

Show cards like:

```text
Total Documents
Completed Documents
Processing Documents
Total Chunks
Questions Asked
```

Use mock values if backend data is not available.

### Quick Actions

```text
Upload Documents
Ask AI
Generate Summary
Find Key Details
```

Only `Upload Documents` and `Ask AI` need to navigate to tabs for now.

### Recent Activity

Example UI:

```text
Recent Activity

- Agreement.pdf uploaded
- User asked: "What are the key obligations?"
- Policy.pdf processed successfully
```

Use mock activity for now.

---

# Tab 2: Documents

## Purpose

Allow the user to upload, view, select, and manage documents inside the workspace.

---

## Documents Tab Layout

```text
Documents

[Drag and drop files here]
or
[Browse Files]

Supported file types: PDF, TXT

Filters:
[All] [Completed] [Processing] [Failed] [PDF] [TXT]

Document Table
```

---

## Upload Area

Create a clean upload dropzone.

UI text:

```text
Drag and drop files here
or browse files from your computer

Supported: PDF and TXT
```

Buttons:

```text
Browse Files
Upload
```

If existing upload API is available, wire it to the existing endpoint.

If workspace-specific upload API is not available yet, keep the UI ready and add a TODO comment where the API should be integrated.

---

## Document Table

Show a table with columns:

```text
Select
File Name
Document Type
Status
Chunks
Uploaded
Actions
```

Example:

```text
[ ] Agreement.pdf       Contract       Completed       72       Today       View | Ask | Delete
[ ] Policy.pdf          Policy         Completed       31       Today       View | Ask | Delete
[ ] Notes.txt           Notes          Processing      --       Today       View
```

Document type options:

```text
Contract
Policy
Proposal
Report
Invoice
Research Paper
Meeting Notes
Specification
Other
```

Status chips:

```text
Completed
Processing
Failed
Queued
```

### Status Chip Colors

Use standard semantic colors:

```text
Completed = green
Processing = blue
Failed = red
Queued = gray
```

---

## Bulk Actions

When one or more documents are selected, show a bulk action bar:

```text
2 documents selected

[Ask selected] [Summarize] [Compare] [Delete] [Reprocess]
```

For now:

- `Ask selected` should navigate to the Ask AI tab and preserve selected document IDs in state.
- Other buttons can be disabled or placeholder buttons.

---

## Document Details Drawer

When user clicks `View`, open a side drawer.

Show:

```text
File Name
Document Type
Status
Chunk Count
Uploaded Date
Embedding Dimension
File Size
```

Actions:

```text
Ask this document
Reprocess
Delete
```

For now:

- `Ask this document` should navigate to Ask AI tab and set answer scope to `Single Document`.
- `Reprocess` can be placeholder.
- `Delete` can remove from local state if using mock data.

---

# Tab 3: Ask AI

## Purpose

Allow the user to ask questions over either:

1. all documents in the workspace,
2. selected documents,
3. or a single document.

This is the most important screen.

---

## Ask AI Layout

Use a three-column layout on desktop:

```text
 -------------------------------------------------------------------------------
| Document Selector      | AI Conversation / Answer Area       | Source Evidence |
|------------------------|--------------------------------------|-----------------|
| Answer From            | Ask anything about this workspace    | Selected Source |
| Documents              |                                      |                 |
| Filters                | Question input                       | Source preview  |
| Selected Docs          | Answer card                          |                 |
 -------------------------------------------------------------------------------
```

On smaller screens, stack the sections vertically.

---

## Left Panel: Document Selector

### Scope Selector

Use radio buttons or segmented buttons:

```text
Answer From:
(•) All workspace documents
( ) Selected documents
( ) Single document
```

### Documents List

Show all documents in the workspace with checkboxes:

```text
[x] Agreement.pdf
[x] Policy.pdf
[ ] Notes.txt
[ ] Report.pdf
```

If the user selects `All workspace documents`, checkboxes can remain visible but disabled or visually secondary.

If the user selects `Selected documents`, checkboxes should be enabled.

If the user selects `Single document`, allow selecting exactly one document.

### Selected Summary

Show:

```text
Using all 12 workspace documents
```

or:

```text
Using 2 selected documents
```

or:

```text
Using Agreement.pdf only
```

---

## Center Panel: AI Conversation / Answer Area

### Empty State

Before asking a question:

```text
Ask a question about this workspace

Examples:
[Summarize these documents]
[What are the key obligations?]
[Find important dates]
[What information is missing?]
[Compare selected documents]
```

Prompt chips should populate the question input when clicked.

### Question Input

Use a large textarea:

```text
Ask a question about your documents...
```

Button:

```text
Ask AI
```

### Answer Card

After asking, show:

```text
AI Answer

[answer text]

Confidence: High
Sources Used: 3
Scope: Selected documents

[Copy Answer] [Ask Follow-up] [Regenerate]
```

If backend API is not available for new workspace-scoped query shape, mock the response in UI for now.

---

## Source Citations

Below the answer, show citations as clickable cards:

```text
Source 1
Agreement.pdf
Page 4 · Chunk 12 · Similarity 0.89

"The contractor shall submit invoices monthly..."
```

Clicking a citation should update the right Source Evidence panel.

---

## Right Panel: Source Evidence

When no source is selected:

```text
Source Evidence

Select a citation to view the retrieved source text.
```

When a source is selected:

```text
Source Evidence

Document: Agreement.pdf
Page: 4
Chunk: 12
Similarity Score: 0.89

Retrieved Text:
"The contractor shall submit invoices monthly. Payment shall be made within 30 days..."

Actions:
[Copy Source Text]
[Ask Follow-up]
```

---

# Placeholder Tab: Insights

Create placeholder UI for now.

```text
Insights

Generate summaries, extract key details, identify risks, and find missing information across this workspace.

[Generate Workspace Summary]
[Extract Key Details]
[Find Risks]
[Find Missing Info]
```

Use placeholder cards:

```text
Summary
Key Details
Risks
Missing Information
Suggested Questions
```

No real AI integration required for this task unless existing API already supports it.

---

# Placeholder Tab: Compare

Create placeholder UI for now.

```text
Compare Documents

Select two or more documents to compare key differences, overlapping information, and missing details.

[Select Documents]
[Compare]
```

No real comparison logic required yet.

---

# Placeholder Tab: Activity

Create placeholder UI for workspace activity.

```text
Activity

- Workspace created
- Agreement.pdf uploaded
- Question asked
- Answer generated
```

Use mock activity data.

---

# State Management Requirements

For the UI-only version, it is acceptable to use local React state.

Required local state:

```text
workspaces
selectedWorkspace
documents
selectedDocuments
activeWorkspaceTab
answerScope
question
answer
sources
selectedSource
loading
snackbar
```

If Redux or another existing state management approach is already present, use the existing approach.

---

# API Integration Requirements

For now, keep the implementation UI-focused.

Where possible, reuse existing APIs:

```text
POST /api/documents/upload
GET /api/documents
POST /api/query
```

However, because the new UI introduces workspaces, do not force backend changes in this task.

Use TODO comments for future backend integration:

```js
// TODO: Replace mock workspace state with GET /api/workspaces
// TODO: Replace local workspace creation with POST /api/workspaces
// TODO: Update upload API to include workspaceId
// TODO: Update query API to support workspaceId, scope, and selected documentIds
```

Recommended future request shape:

```json
{
  "workspaceId": "workspace-123",
  "query": "What are the key obligations?",
  "scope": "SELECTED_DOCUMENTS",
  "documentIds": ["doc-1", "doc-2"]
}
```

Recommended future response shape:

```json
{
  "answer": "The key obligations are...",
  "confidence": "HIGH",
  "sources": [
    {
      "documentId": "doc-1",
      "fileName": "Agreement.pdf",
      "pageNumber": 4,
      "chunkIndex": 12,
      "similarityScore": 0.89,
      "content": "The contractor shall submit invoices monthly..."
    }
  ]
}
```

---

# Component Structure

Recommended frontend structure:

```text
src/
  components/
    layout/
      AppLayout.jsx
      Sidebar.jsx
      Topbar.jsx

    workspaces/
      WorkspaceList.jsx
      WorkspaceCard.jsx
      CreateWorkspaceModal.jsx
      WorkspaceHeader.jsx
      WorkspaceOverview.jsx

    documents/
      DocumentUploadDropzone.jsx
      DocumentTable.jsx
      DocumentStatusChip.jsx
      DocumentTypeChip.jsx
      DocumentDetailsDrawer.jsx

    ask/
      AskAiWorkspace.jsx
      QuestionScopeSelector.jsx
      DocumentSelectorPanel.jsx
      ChatPanel.jsx
      AnswerCard.jsx
      SourceCitationCard.jsx
      SourceEvidencePanel.jsx
      PromptChips.jsx

    common/
      EmptyState.jsx
      StatusBadge.jsx
      ConfirmDialog.jsx
      LoadingButton.jsx
```

Page components:

```text
src/pages/
  WorkspacesPage.jsx
  WorkspaceViewPage.jsx
  EvaluationPage.jsx
  SettingsPage.jsx
```

---

# Required UI Behavior

## Workspaces Page

- User can view workspace cards.
- User can search/filter workspaces.
- User can create a new workspace from a modal.
- User can open a workspace.

## Workspace View

- User can go back to Workspaces.
- User can switch tabs.
- Header shows selected workspace metadata.

## Documents Tab

- User can see upload area.
- User can see document table.
- User can select documents.
- Selected document count should be visible.
- User can click `Ask selected` and navigate to Ask AI tab.
- User can open document details drawer.

## Ask AI Tab

- User can choose answer scope:
  - All workspace documents
  - Selected documents
  - Single document
- User can select documents from left panel.
- User can type a question.
- User can click Ask AI.
- UI should show loading state.
- UI should show answer card.
- UI should show clickable source citations.
- Clicking a citation should populate source evidence panel.
- User can copy answer.
- User can copy source text.

---

# UX Details

## Empty States

Add helpful empty states.

Examples:

### No workspaces

```text
No workspaces yet.
Create your first workspace to organize documents and ask AI-powered questions.
```

### No documents in workspace

```text
No documents uploaded.
Upload PDFs or text files to start asking questions.
```

### No selected documents

```text
Select one or more documents, or choose "All workspace documents".
```

### No answer yet

```text
Ask a question to generate an answer from your workspace documents.
```

---

## Loading States

Add loading indicators for:

- creating workspace
- uploading documents
- fetching documents
- asking AI question

Use skeletons, spinners, or disabled buttons with loading text.

Example:

```text
Generating answer...
```

---

## Error States

Show user-friendly errors.

Examples:

```text
Failed to upload document. Please try again.
Failed to generate answer. Please check the backend and try again.
No relevant sources found for this question.
```

---

# Styling Requirements

The UI should look clean and recruiter/demo friendly.

Recommended styling:

- sidebar width around 240px
- main content max-width or full-width with comfortable padding
- card spacing around 16px to 24px
- tabs clearly visible
- selected tab highlighted
- source citations visually distinct
- answer card should be prominent
- source evidence panel should feel like an inspector/sidebar

---

# Responsive Behavior

Desktop:

```text
Sidebar + main layout
Ask AI uses 3 columns
```

Tablet:

```text
Sidebar may collapse
Ask AI uses 2 columns
Source panel moves below or becomes drawer
```

Mobile:

```text
Stack all panels vertically
Use tabs/drawers for document selector and source evidence
```

---

# Accessibility Requirements

- Buttons should have clear labels.
- Inputs should have labels or aria-labels.
- Status should not be represented by color only.
- Dialogs should be keyboard accessible.
- Tables should have proper headers.
- Focus states should be visible.

---

# Acceptance Criteria

The UI task is complete when:

1. The app has a professional sidebar-based layout.
2. A user can create a workspace using a modal.
3. Workspaces appear as cards or a table.
4. A user can open a workspace.
5. Workspace View shows header metadata and tabs.
6. Documents tab includes upload UI and document table.
7. Documents can be selected with checkboxes.
8. Ask AI tab supports answer scope:
   - all workspace documents,
   - selected documents,
   - single document.
9. Ask AI tab has document selector, question input, answer area, and source evidence panel.
10. The answer card displays:
    - answer text,
    - confidence,
    - source count,
    - scope used.
11. Source citations are clickable.
12. Clicking a source citation updates the Source Evidence panel.
13. UI has empty, loading, and error states.
14. Placeholder tabs exist for Insights, Compare, and Activity.
15. The UI remains generic and is not construction-specific.
16. No backend schema changes are required for this UI task.
17. Mock data is acceptable where backend endpoints are not ready.
18. TODO comments are added wherever future backend integration is needed.

---

# Suggested Mock Data

Use mock data like this if backend integration is not available.

```js
const mockWorkspaces = [
  {
    id: "ws-1",
    name: "Vendor Contracts",
    description: "Agreements, amendments, and policy documents for vendor review.",
    category: "Legal",
    status: "Active",
    documentCount: 4,
    lastActivity: "Today"
  },
  {
    id: "ws-2",
    name: "Research Papers",
    description: "AI and machine learning papers for study and summarization.",
    category: "Research",
    status: "Active",
    documentCount: 8,
    lastActivity: "Yesterday"
  },
  {
    id: "ws-3",
    name: "Financial Reports",
    description: "Quarterly reports, statements, and financial analysis documents.",
    category: "Finance",
    status: "Active",
    documentCount: 6,
    lastActivity: "May 25"
  }
];

const mockDocuments = [
  {
    id: "doc-1",
    fileName: "Agreement.pdf",
    documentType: "Contract",
    status: "Completed",
    chunks: 72,
    uploaded: "Today",
    embeddingDimension: 1536,
    fileSize: "1.8 MB"
  },
  {
    id: "doc-2",
    fileName: "Policy.pdf",
    documentType: "Policy",
    status: "Completed",
    chunks: 31,
    uploaded: "Today",
    embeddingDimension: 1536,
    fileSize: "900 KB"
  },
  {
    id: "doc-3",
    fileName: "Notes.txt",
    documentType: "Meeting Notes",
    status: "Processing",
    chunks: null,
    uploaded: "Today",
    embeddingDimension: null,
    fileSize: "120 KB"
  }
];

const mockAnswer = {
  answer:
    "The selected documents mention several key obligations, including timely invoice submission, compliance with the attached policy terms, and written approval before making material changes.",
  confidence: "High",
  scope: "Selected documents",
  sources: [
    {
      id: "source-1",
      documentId: "doc-1",
      fileName: "Agreement.pdf",
      pageNumber: 4,
      chunkIndex: 12,
      similarityScore: 0.89,
      content:
        "The contractor shall submit invoices monthly. Payment shall be made within thirty days after receipt of a valid invoice."
    },
    {
      id: "source-2",
      documentId: "doc-2",
      fileName: "Policy.pdf",
      pageNumber: 2,
      chunkIndex: 6,
      similarityScore: 0.83,
      content:
        "All material changes require written approval before implementation. The policy applies to all active agreements."
    }
  ]
};
```

---

# Implementation Notes

- Keep the UI generic.
- Do not hardcode construction-specific labels.
- Prefer `workspace` naming throughout the frontend.
- Keep components reusable and separated.
- Use mock data first if backend endpoints are not ready.
- Do not block UI work on backend changes.
- Add clear TODO comments for future backend integration.
- Make the UI polished enough for a portfolio demo.
