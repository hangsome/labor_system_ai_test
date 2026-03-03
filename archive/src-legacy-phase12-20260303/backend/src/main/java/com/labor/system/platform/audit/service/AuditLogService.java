package com.labor.system.platform.audit.service;

import com.labor.system.platform.audit.repository.AuditLogRepository;
import com.labor.system.platform.audit.web.dto.AuditLogItemResponse;
import com.labor.system.platform.audit.web.dto.AuditLogPageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  public AuditLogService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  public void record(
      String bizType,
      String bizId,
      String action,
      Long operatorId,
      String traceId,
      String detailJson) {
    if (!hasText(bizType) || !hasText(bizId) || !hasText(action)) {
      return;
    }
    Long safeOperatorId = operatorId == null ? 0L : operatorId;
    String safeTraceId = hasText(traceId) ? traceId : UUID.randomUUID().toString();
    auditLogRepository.insert(
        bizType.trim(), bizId.trim(), action.trim(), safeOperatorId, safeTraceId, detailJson);
  }

  public AuditLogPageResponse query(
      String bizType, String bizId, Long operatorId, Integer pageNo, Integer pageSize) {
    int safePageNo = Math.max(pageNo == null ? 1 : pageNo, 1);
    int safePageSize = Math.max(pageSize == null ? 20 : pageSize, 1);
    int offset = (safePageNo - 1) * safePageSize;

    long total = auditLogRepository.count(bizType, bizId, operatorId);
    List<AuditLogEntry> entries = auditLogRepository.query(bizType, bizId, operatorId, offset, safePageSize);
    List<AuditLogItemResponse> records = new ArrayList<>(entries.size());
    for (AuditLogEntry entry : entries) {
      records.add(
          new AuditLogItemResponse(
              entry.id(),
              entry.bizType(),
              entry.bizId(),
              entry.action(),
              entry.operatorId(),
              entry.traceId(),
              entry.detail(),
              entry.createdAt()));
    }
    return new AuditLogPageResponse(records, safePageNo, safePageSize, total);
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  public record AuditLogEntry(
      Long id,
      String bizType,
      String bizId,
      String action,
      Long operatorId,
      String traceId,
      String detail,
      String createdAt) {}
}
