package com.labor.system.rbac.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.rbac.datascope.DataScopePolicy;
import com.labor.system.rbac.service.DataScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DataScopeTestController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, DataScopeWebConfig.class})
class DataScopeInterceptorTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JwtTokenService jwtTokenService;

  @MockBean private DataScopeService dataScopeService;

  @Test
  void shouldInjectDeptScopeContextWhenRequestInScope() throws Exception {
    when(jwtTokenService.parseAccessToken(eq("scope-token")))
        .thenReturn(new TokenPrincipal(1001L, "u1001"));
    when(dataScopeService.resolveByUserId(eq(1001L)))
        .thenReturn(new DataScopePolicy(DataScopePolicy.DEPT, "D001"));

    mockMvc
        .perform(
            get("/api/admin/v1/system/data-scope/query")
                .header("Authorization", "Bearer scope-token")
                .param("scopeRef", "D001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.userId").value(1001))
        .andExpect(jsonPath("$.data.scopeType").value("DEPT"))
        .andExpect(jsonPath("$.data.scopeRef").value("D001"));
  }

  @Test
  void shouldRejectDeptScopeMismatch() throws Exception {
    when(jwtTokenService.parseAccessToken(eq("scope-token")))
        .thenReturn(new TokenPrincipal(1001L, "u1001"));
    when(dataScopeService.resolveByUserId(eq(1001L)))
        .thenReturn(new DataScopePolicy(DataScopePolicy.DEPT, "D001"));

    mockMvc
        .perform(
            get("/api/admin/v1/system/data-scope/query")
                .header("Authorization", "Bearer scope-token")
                .param("scopeRef", "D002"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTH-403"));
  }

  @Test
  void shouldRejectWhenPolicyIsNone() throws Exception {
    when(jwtTokenService.parseAccessToken(eq("scope-token")))
        .thenReturn(new TokenPrincipal(1001L, "u1001"));
    when(dataScopeService.resolveByUserId(eq(1001L))).thenReturn(DataScopePolicy.none());

    mockMvc
        .perform(get("/api/admin/v1/system/data-scope/query").header("Authorization", "Bearer scope-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTH-403"));
  }

  @Test
  void shouldRejectWhenAuthorizationHeaderMissing() throws Exception {
    mockMvc
        .perform(get("/api/admin/v1/system/data-scope/query"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-401"));
  }
}
