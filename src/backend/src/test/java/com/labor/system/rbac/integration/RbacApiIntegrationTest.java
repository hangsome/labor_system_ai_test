package com.labor.system.rbac.integration;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.rbac.service.RolePermissionService;
import com.labor.system.rbac.web.RolePermissionController;
import com.labor.system.rbac.web.dto.DataScopePolicyResponse;
import com.labor.system.rbac.web.dto.RoleSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RolePermissionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RbacApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RolePermissionService rolePermissionService;

  @Test
  void rolePermissionAndDataScopeApisShouldWorkAsIntegratedFlow() throws Exception {
    RoleSummaryResponse role =
        new RoleSummaryResponse(
            1L,
            "ADMIN",
            "Administrator",
            List.of("iam.user.read", "iam.role.read"),
            new DataScopePolicyResponse(1L, "DEPT", "D001"));
    when(rolePermissionService.listRoles()).thenReturn(List.of(role));
    doNothing().when(rolePermissionService).updateRolePermissions(eq(1L), eq(List.of("iam.user.read")));
    when(rolePermissionService.getDataScopePolicy(eq(1L)))
        .thenReturn(new DataScopePolicyResponse(1L, "DEPT", "D001"));

    mockMvc
        .perform(get("/api/admin/v1/iam/roles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].permissionCodes[0]").value("iam.user.read"));

    mockMvc
        .perform(
            put("/api/admin/v1/iam/roles/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionCodes\":[\"iam.user.read\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    mockMvc
        .perform(get("/api/admin/v1/iam/roles/1/data-scope"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.scopeType").value("DEPT"))
        .andExpect(jsonPath("$.data.scopeRef").value("D001"));
  }

  @Test
  void shouldRejectInvalidPermissionPayload() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/v1/iam/roles/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionCodes\":[]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
