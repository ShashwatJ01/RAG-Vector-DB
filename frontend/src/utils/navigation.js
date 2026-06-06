export function isActiveNav(item, page) {
  const navPage = item.toLowerCase().replaceAll(" ", "-");
  return (item === "Workspaces" && (page === "workspaces" || page === "workspace")) || navPage === page;
}

export function pageTitle(page) {
  return {
    dashboard: "Dashboard",
    workspaces: "Workspaces",
    "recent-documents": "Recent Documents",
    evaluation: "Evaluation",
    settings: "Settings",
  }[page] || "Workspaces";
}
