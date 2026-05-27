function PlaceholderCards({ title, text, items, actions }) {
  return (
    <section className="card">
      <div className="section-heading">
        <div>
          <h3>{title}</h3>
          <p>{text}</p>
        </div>
      </div>
      <div className="action-row">
        {actions.map((action) => (
          <button className="secondary-button" key={action}>
            {action}
          </button>
        ))}
      </div>
      <div className="placeholder-grid">
        {items.map((item) => (
          <article className="placeholder-card" key={item}>
            <h4>{item}</h4>
            <p>Placeholder workspace insight area.</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default PlaceholderCards;
