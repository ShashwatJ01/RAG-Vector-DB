function EmptyState({ title, text, action, onAction }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      <p>{text}</p>
      {action && (
        <button className="primary-button" onClick={onAction}>
          {action}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
