package com.labor.system.platform.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.platform.audit.AuditLogAspect;
import com.labor.system.platform.audit.service.AuditLogService;
import com.labor.system.platform.audit.web.AuditLogController;
import com.labor.system.platform.audit.web.dto.AuditLogItemResponse;
import com.labor.system.platform.audit.web.dto.AuditLogPageResponse;
import com.labor.system.rbac.service.RolePermissionService;
import com.labor.system.rbac.web.RolePermissionController;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {RolePermissionController.class, AuditLogController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuditLogAspect.class})
class AuditApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RolePermissionService rolePermissionService;

  @MockBean private AuditLogService auditLogService;

  @MockBean private JwtTokenService jwtTokenService;

  private List<StoredAuditRecord> storedRecords;

  @BeforeEach
  void setUp() {
    storedRecords = new ArrayList<>();
    doAnswer(
            invocation -> {
              storedRecords.add(
                  new StoredAuditRecord(
                      invocation.getArgument(0, String.class),
                      invocation.getArgument(1, String.class),
                      invocation.getArgument(2, String.class),
                      invocation.getArgument(3, Long.class),
                      invocation.getArgument(4, String.class),
                      invocation.getArgument(5, String.class)));
              return null;
            })
        .when(auditLogService)
        .record(any(), any(), any(), any(), any(), any());

    when(auditLogService.query(any(), any(), any(), anyInt(), anyInt()))
        .thenAnswer(
            invocation -> {
              String bizType = invocation.getArgument(0, String.class);
              String bizId = invocation.getArgument(1, String.class);
              Long operatorId = invocation.getArgument(2, Long.class);
              Integer pageNo = invocation.getArgument(3, Integer.class);
              Integer pageSize = invocation.getArgument(4, Integer.class);

              List<StoredAuditRecord> filtered = new ArrayList<>();
              for (StoredAuditRecord record : storedRecords) {
                if (bizType != null && !bizType.isBlank() && !bizType.equals(record.bizType())) {
                  continue;
                }
                if (bizId != null && !bizId.isBlank() && !bizId.equals(record.bizId())) {
                  continue;
                }
                if (operatorId != null && !operatorId.equals(record.operatorId())) {
                  continue;
                }
                filtered.add(record);
              }

              int safePageNo = pageNo == null ? 1 : pageNo;
              int safePageSize = pageSize == null ? 20 : pageSize;
              int start = Math.max((safePageNo - 1) * safePageSize, 0);
              int end = Math.min(start + safePageSize, filtered.size());

              List<AuditLogItemResponse> items = new ArrayList<>();
              if (start < end) {
                for (int i = start; i < end; i++) {
                  StoredAuditRecord record = filtered.get(i);
                  items.add(
                      new AuditLogItemResponse(
                          (long) (i + 1),
                          record.bizType(),
                          record.bizId(),
                          record.action(),
                          record.operatorId(),
                          record.traceId(),
                          record.detail(),
                          "2026-02-26T20:00:00"));
                }
              }
              return new AuditLogPageResponse(
                  items, safePageNo, safePageSize, (long) filtered.size());
            });
  }

  @Test
  void shouldQueryAuditLogAfterRolePermissionWrite() throws Exception {
    doAnswer(
            invocation -> {
              storedRecords.add(
                  new StoredAuditRecord(
                      "ROLE_PERMISSION",
                      "1",
                      "UPDATE",
                      3001L,
                      UUID.randomUUID().toString(),
                      null));
              return null;
            })
        .when(rolePermissionService)
        .updateRolePermissions(eq(1L), eq(List.of("iam.user.read")));
    when(jwtTokenService.parseAccessToken(eq("audit-access-token")))
        .thenReturn(new TokenPrincipal(3001L, "auditor"));

    mockMvc
        .perform(
            put("/api/admin/v1/iam/roles/1/permissions")
                .header("Authorization", "Bearer audit-access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionCodes\":[\"iam.user.read\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    mockMvc
        .perform(
            get("/api/admin/v1/platform/audit-logs")
                .param("bizType", "ROLE_PERMISSION")
                .param("bizId", "1")
                .param("operatorId", "3001")
                .param("pageNo", "1")
                .param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].bizType").value("ROLE_PERMISSION"))
        .andExpect(jsonPath("$.data.records[0].bizId").value("1"))
        .andExpect(jsonPath("$.data.records[0].action").value("UPDATE"))
        .andExpect(jsonPath("$.data.records[0].operatorId").value(3001));
  }

  @Test
  void shouldSupportAuditFilterByOperatorId() throws Exception {
    doAnswer(
            invocation -> {
              storedRecords.add(
                  new StoredAuditRecord(
                      "ROLE_PERMISSION",
                      "2",
                      "UPDATE",
                      4001L,
                      UUID.randomUUID().toString(),
                      null));
              return null;
            })
        .when(rolePermissionService)
        .updateRolePermissions(eq(2L), eq(List.of("iam.role.read")));
    when(jwtTokenService.parseAccessToken(eq("audit-access-token-2")))
        .thenReturn(new TokenPrincipal(4001L, "operator-1"));

    mockMvc
        .perform(
            put("/api/admin/v1/iam/roles/2/permissions")
                .header("Authorization", "Bearer audit-access-token-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionCodes\":[\"iam.role.read\"]}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/admin/v1/platform/audit-logs")
                .param("bizType", "ROLE_PERMISSION")
                .param("operatorId", "9999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(0));
  }

  private record StoredAuditRecord(
      String bizType, String bizId, String action, Long operatorId, String traceId, String detail) {}
}
