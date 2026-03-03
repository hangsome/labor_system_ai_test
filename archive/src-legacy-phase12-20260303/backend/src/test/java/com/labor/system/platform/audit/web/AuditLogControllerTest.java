package com.labor.system.platform.audit.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.platform.audit.service.AuditLogService;
import com.labor.system.platform.audit.web.dto.AuditLogItemResponse;
import com.labor.system.platform.audit.web.dto.AuditLogPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuditLogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuditLogControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuditLogService auditLogService;

  @Test
  void shouldQueryAuditLogsByPagingParameters() throws Exception {
    AuditLogItemResponse item =
        new AuditLogItemResponse(
            1L,
            "ROLE_PERMISSION",
            "1",
            "UPDATE",
            1001L,
            "trace-1",
            null,
            "2026-02-26T19:58:00");
    when(auditLogService.query(any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(new AuditLogPageResponse(List.of(item), 1, 20, 1L));

    mockMvc
        .perform(get("/api/admin/v1/platform/audit-logs").param("bizType", "ROLE_PERMISSION"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].bizType").value("ROLE_PERMISSION"))
        .andExpect(jsonPath("$.data.records[0].action").value("UPDATE"));
  }

  @Test
  void shouldRejectInvalidPageNo() throws Exception {
    mockMvc
        .perform(get("/api/admin/v1/platform/audit-logs").param("pageNo", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
