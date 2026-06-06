package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentStatus;
import com.example.ragsearch.model.DocumentStatusResponse;
import com.example.ragsearch.model.IngestionJob;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class IngestionJobRepository {
    private final JdbcTemplate jdbcTemplate;

    public IngestionJobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createJobIfAbsent(IngestionJob job) {
        try {
            return jdbcTemplate.query(
                    "INSERT INTO ingestion_jobs (id, document_id, workspace_id, file_name, file_hash, content_type, source_size, payload, status, retry_count, max_retries, available_at) " +
                            "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
                            "WHERE NOT EXISTS (" +
                            "  SELECT 1 FROM ingestion_jobs WHERE document_id = ? AND status IN ('QUEUED', 'PROCESSING')" +
                            ") " +
                            "RETURNING id",
                    (rs, rowNum) -> rs.getString("id"),
                    job.getId(),
                    job.getDocumentId(),
                    job.getWorkspaceId(),
                    job.getFileName(),
                    job.getFileHash(),
                    job.getContentType(),
                    job.getSourceSize(),
                    job.getPayload(),
                    job.getStatus().name(),
                    job.getRetryCount(),
                    job.getMaxRetries(),
                    toTimestamp(job.getAvailableAt()),
                    job.getDocumentId()
            ).stream().findFirst()
                    .orElseGet(() -> findLatestByDocumentId(job.getDocumentId()).map(IngestionJob::getId).orElse(job.getId()));
        } catch (DuplicateKeyException ex) {
            return findLatestByDocumentId(job.getDocumentId()).map(IngestionJob::getId).orElseThrow(() -> ex);
        }
    }

    public Optional<IngestionJob> claimNextJob() {
        List<IngestionJob> jobs = jdbcTemplate.query(
                "WITH next_job AS (" +
                        "  SELECT id FROM ingestion_jobs " +
                        "  WHERE status = 'QUEUED' AND available_at <= now() " +
                        "  ORDER BY created_at " +
                        "  FOR UPDATE SKIP LOCKED " +
                        "  LIMIT 1" +
                        ") " +
                        "UPDATE ingestion_jobs " +
                        "SET status = 'PROCESSING', started_at = COALESCE(started_at, now()), updated_at = now() " +
                        "WHERE id = (SELECT id FROM next_job) " +
                        "RETURNING id, document_id, workspace_id, file_name, file_hash, content_type, source_size, payload, status, retry_count, max_retries, error_message, available_at, created_at, updated_at, started_at, finished_at",
                (rs, rowNum) -> mapJob(rs)
        );
        return jobs.stream().findFirst();
    }

    public Optional<IngestionJob> findLatestByDocumentId(String documentId) {
        return jdbcTemplate.query(
                "SELECT id, document_id, workspace_id, file_name, file_hash, content_type, source_size, payload, status, retry_count, max_retries, error_message, available_at, created_at, updated_at, started_at, finished_at " +
                        "FROM ingestion_jobs WHERE document_id = ? ORDER BY created_at DESC LIMIT 1",
                (rs, rowNum) -> mapJob(rs),
                documentId
        ).stream().findFirst();
    }

    public DocumentStatusResponse getDocumentStatus(String documentId) {
        return jdbcTemplate.query(
                "SELECT d.id AS document_id, d.file_name, COALESCE(d.document_status, 'INDEXED') AS document_status, " +
                        "COALESCE(j.error_message, d.error_message) AS error_message, d.uploaded_at AS created_at, " +
                        "j.id AS job_id, COALESCE(j.retry_count, 0) AS retry_count, COALESCE(j.max_retries, 0) AS max_retries, " +
                        "j.updated_at, j.started_at, j.finished_at " +
                        "FROM documents d " +
                        "LEFT JOIN LATERAL (" +
                        "  SELECT id, retry_count, max_retries, error_message, updated_at, started_at, finished_at " +
                        "  FROM ingestion_jobs WHERE document_id = d.id ORDER BY created_at DESC LIMIT 1" +
                        ") j ON true " +
                        "WHERE d.id = ?",
                (rs, rowNum) -> new DocumentStatusResponse(
                        rs.getString("document_id"),
                        rs.getString("file_name"),
                        DocumentStatus.valueOf(rs.getString("document_status")),
                        rs.getString("job_id"),
                        rs.getInt("retry_count"),
                        rs.getInt("max_retries"),
                        rs.getString("error_message"),
                        toOffset(rs.getTimestamp("created_at")),
                        toOffset(rs.getTimestamp("updated_at")),
                        toOffset(rs.getTimestamp("started_at")),
                        toOffset(rs.getTimestamp("finished_at"))
                ),
                documentId
        ).stream().findFirst().orElseThrow(() -> new NoSuchElementException("Document not found."));
    }

    public void markIndexed(String jobId) {
        jdbcTemplate.update(
                "UPDATE ingestion_jobs SET status = 'INDEXED', error_message = NULL, updated_at = now(), finished_at = now() WHERE id = ?",
                jobId
        );
    }

    public void markQueuedForRetry(String jobId, int retryCount, String errorMessage, OffsetDateTime availableAt) {
        jdbcTemplate.update(
                "UPDATE ingestion_jobs SET status = 'QUEUED', retry_count = ?, error_message = ?, available_at = ?, updated_at = now() WHERE id = ?",
                retryCount,
                errorMessage,
                toTimestamp(availableAt),
                jobId
        );
    }

    public void markFailed(String jobId, int retryCount, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE ingestion_jobs SET status = 'FAILED', retry_count = ?, error_message = ?, updated_at = now(), finished_at = now() WHERE id = ?",
                retryCount,
                errorMessage,
                jobId
        );
    }

    private IngestionJob mapJob(ResultSet rs) throws SQLException {
        return new IngestionJob(
                rs.getString("id"),
                rs.getString("document_id"),
                rs.getString("workspace_id"),
                rs.getString("file_name"),
                rs.getString("file_hash"),
                rs.getString("content_type"),
                rs.getLong("source_size"),
                rs.getBytes("payload"),
                DocumentStatus.valueOf(rs.getString("status")),
                rs.getInt("retry_count"),
                rs.getInt("max_retries"),
                rs.getString("error_message"),
                toOffset(rs.getTimestamp("available_at")),
                toOffset(rs.getTimestamp("created_at")),
                toOffset(rs.getTimestamp("updated_at")),
                toOffset(rs.getTimestamp("started_at")),
                toOffset(rs.getTimestamp("finished_at"))
        );
    }

    private Timestamp toTimestamp(OffsetDateTime dateTime) {
        return dateTime == null ? Timestamp.from(java.time.Instant.now()) : Timestamp.from(dateTime.toInstant());
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
