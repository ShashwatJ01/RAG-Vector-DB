function Snackbar({ message }) {
  if (!message) return null;

  return (
    <div className="snackbar" role="status">
      {message}
    </div>
  );
}

export default Snackbar;
