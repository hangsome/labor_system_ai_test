package com.labor.system.platform.audit.web.dto;

public record AuditLogItemResponse(
    Long id,
    String bizType,
    String bizId,
    String action,
    Long operatorId,
    String traceId,
    String detail,
    String createdAt) {}
