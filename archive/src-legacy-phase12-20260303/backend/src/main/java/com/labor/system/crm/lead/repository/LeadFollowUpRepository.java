package com.labor.system.crm.lead.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LeadFollowUpRepository {

  private static final String BIZ_TYPE = "LEAD_FOLLOW_UP";
  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {};
  private static final String INSERT_SQL =
      """
      INSERT INTO audit_log(biz_type, biz_id, action, operator_id, trace_id, detail)
      VALUES(?, ?, ?, ?, ?, ?)
      """;
  private static final String FIND_BY_LEAD_ID_SQL =
      """
      SELECT id, action, operator_id, detail, created_at
      FROM audit_log
      WHERE biz_type = ?
        AND biz_id = ?
      ORDER BY id DESC
      """;

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public LeadFollowUpRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  public void insertStatusTransition(Long leadId, String statusFrom, String statusTo, String comment) {
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("statusFrom", statusFrom);
    detail.put("statusTo", statusTo);
    if (comment != null && !comment.isBlank()) {
      detail.put("content", comment.trim());
    }
    insert(leadId, "STATUS_TRANSITION", detail);
  }

  public void insertFollowUp(
      Long leadId, String content, LocalDateTime nextContactAt, String cooperationStatus) {
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("content", content);
    detail.put("status", cooperationStatus);
    if (nextContactAt != null) {
      detail.put("nextContactAt", nextContactAt.toString());
    }
    insert(leadId, "FOLLOW_UP", detail);
  }

  public List<LeadFollowUpRecord> findByLeadId(Long leadId) {
    return jdbcTemplate.query(
        FIND_BY_LEAD_ID_SQL,
        (rs, rowNum) -> {
          String detail = rs.getString("detail");
          Map<String, Object> detailMap = parseDetail(detail);
          return new LeadFollowUpRecord(
              rs.getLong("id"),
              rs.getString("action"),
              rs.getLong("operator_id"),
              toText(detailMap.get("content")),
              toText(detailMap.get("status")),
              toText(detailMap.get("statusFrom")),
              toText(detailMap.get("statusTo")),
              toText(detailMap.get("nextContactAt")),
              toDateTimeText(rs.getTimestamp("created_at")));
        },
        BIZ_TYPE,
        String.valueOf(leadId));
  }

  private void insert(Long leadId, String action, Map<String, Object> detail) {
    jdbcTemplate.update(
        INSERT_SQL,
        BIZ_TYPE,
        String.valueOf(leadId),
        action,
        0L,
        UUID.randomUUID().toString(),
        writeDetail(detail));
  }

  private String writeDetail(Map<String, Object> detail) {
    try {
      return objectMapper.writeValueAsString(detail);
    } catch (Exception ex) {
      return null;
    }
  }

  private Map<String, Object> parseDetail(String detail) {
    if (detail == null || detail.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(detail, MAP_TYPE_REFERENCE);
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private String toText(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private String toDateTimeText(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime().toString();
  }

  public record LeadFollowUpRecord(
      Long id,
      String action,
      Long operatorId,
      String content,
      String status,
      String statusFrom,
      String statusTo,
      String nextContactAt,
      String createdAt) {}
}
