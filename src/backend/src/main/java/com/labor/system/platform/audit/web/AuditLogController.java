package com.labor.system.platform.audit.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.platform.audit.service.AuditLogService;
import com.labor.system.platform.audit.web.dto.AuditLogPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/v1/platform/audit-logs")
public class AuditLogController {

  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ApiResponse<AuditLogPageResponse> query(
      @RequestParam(value = "bizType", required = false) String bizType,
      @RequestParam(value = "bizId", required = false) String bizId,
      @RequestParam(value = "operatorId", required = false) Long operatorId,
      @RequestParam(value = "pageNo", defaultValue = "1") @Min(1) Integer pageNo,
      @RequestParam(value = "pageSize", defaultValue = "20") @Min(1) @Max(100) Integer pageSize) {
    AuditLogPageResponse response = auditLogService.query(bizType, bizId, operatorId, pageNo, pageSize);
    return ApiResponse.success(response);
  }
}
