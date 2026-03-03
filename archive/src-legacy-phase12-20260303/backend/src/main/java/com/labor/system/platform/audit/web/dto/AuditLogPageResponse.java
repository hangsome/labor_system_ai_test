package com.labor.system.platform.audit.web.dto;

import java.util.List;

public record AuditLogPageResponse(
    List<AuditLogItemResponse> records, Integer pageNo, Integer pageSize, Long total) {}
