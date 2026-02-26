package com.labor.system.platform.audit.repository;

import com.labor.system.platform.audit.service.AuditLogService.AuditLogEntry;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {

  private static final String INSERT_SQL =
      """
      INSERT INTO audit_log(biz_type, biz_id, action, operator_id, trace_id, detail)
      VALUES(?, ?, ?, ?, ?, ?)
      """;

  private static final String BASE_QUERY_SQL =
      """
      FROM audit_log
      WHERE 1 = 1
      """;

  private final JdbcTemplate jdbcTemplate;

  public AuditLogRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String bizType,
      String bizId,
      String action,
      Long operatorId,
      String traceId,
      String detail) {
    jdbcTemplate.update(INSERT_SQL, bizType, bizId, action, operatorId, traceId, detail);
  }

  public long count(String bizType, String bizId, Long operatorId) {
    QuerySql querySql = buildQuerySql(bizType, bizId, operatorId);
    String sql = "SELECT COUNT(1) " + querySql.whereClause();
    Long total = jdbcTemplate.queryForObject(sql, Long.class, querySql.parameters().toArray());
    return total == null ? 0L : total;
  }

  public List<AuditLogEntry> query(
      String bizType, String bizId, Long operatorId, int offset, int pageSize) {
    QuerySql querySql = buildQuerySql(bizType, bizId, operatorId);
    String sql =
        """
        SELECT id, biz_type, biz_id, action, operator_id, trace_id, detail, created_at
        """
            + querySql.whereClause()
            + " ORDER BY id DESC LIMIT ? OFFSET ?";

    List<Object> params = new ArrayList<>(querySql.parameters());
    params.add(pageSize);
    params.add(offset);
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new AuditLogEntry(
                rs.getLong("id"),
                rs.getString("biz_type"),
                rs.getString("biz_id"),
                rs.getString("action"),
                rs.getLong("operator_id"),
                rs.getString("trace_id"),
                rs.getString("detail"),
                toTimestampString(rs.getTimestamp("created_at"))),
        params.toArray());
  }

  private QuerySql buildQuerySql(String bizType, String bizId, Long operatorId) {
    StringBuilder whereClause = new StringBuilder(BASE_QUERY_SQL);
    List<Object> parameters = new ArrayList<>();
    if (hasText(bizType)) {
      whereClause.append(" AND biz_type = ?");
      parameters.add(bizType.trim());
    }
    if (hasText(bizId)) {
      whereClause.append(" AND biz_id = ?");
      parameters.add(bizId.trim());
    }
    if (operatorId != null) {
      whereClause.append(" AND operator_id = ?");
      parameters.add(operatorId);
    }
    return new QuerySql(whereClause.toString(), parameters);
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private String toTimestampString(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime().toString();
  }

  private record QuerySql(String whereClause, List<Object> parameters) {}
}
