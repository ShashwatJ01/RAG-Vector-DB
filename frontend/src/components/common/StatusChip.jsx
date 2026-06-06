function StatusChip({ status }) {
  return <span className={`status-chip ${status.toLowerCase()}`}>{status}</span>;
}

export default StatusChip;
