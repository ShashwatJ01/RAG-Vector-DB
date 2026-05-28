export function isActiveNav(item, page) {
  return (item === "Workspaces" && (page === "workspaces" || page === "workspace")) || item.toLowerCase() === page;
}

export function navIcon(item) {
  return {
    Dashboard: "D",
    Workspaces: "W",
    "Recent Documents": "R",
    Evaluation: "E",
    Settings: "S",
  }[item];
}

export function pageTitle(page) {
  return { workspaces: "Workspaces", evaluation: "Evaluation", settings: "Settings" }[page] || "Workspaces";
}
