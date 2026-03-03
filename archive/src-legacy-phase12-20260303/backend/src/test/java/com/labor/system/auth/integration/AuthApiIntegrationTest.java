package com.labor.system.auth.integration;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labor.system.auth.domain.UserAccount;
import com.labor.system.auth.repository.AuthQueryRepository;
import com.labor.system.auth.repository.UserAccountRepository;
import com.labor.system.auth.service.AuthService;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.auth.web.AuthController;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = AuthController.class)
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  AuthApiIntegrationTest.IntegrationTestConfig.class
})
class AuthApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserAccountRepository userAccountRepository;
  @MockBean private AuthQueryRepository authQueryRepository;

  @TestConfiguration
  static class IntegrationTestConfig {

    @Bean
    JwtTokenService jwtTokenService(ObjectMapper objectMapper) {
      return new JwtTokenService(
          objectMapper,
          "auth-api-integration-jwt-secret",
          3600,
          604800);
    }

    @Bean
    AuthService authService(
        UserAccountRepository userAccountRepository,
        AuthQueryRepository authQueryRepository,
        JwtTokenService jwtTokenService,
        PasswordEncoder passwordEncoder) {
      return new AuthService(
          userAccountRepository, authQueryRepository, jwtTokenService, passwordEncoder);
    }
  }

  @Test
  void loginRefreshMeShouldWorkAsIntegratedFlow() throws Exception {
    UserAccount user = activeUser(2001L, "integration-admin", "123456", "Integration Admin");
    when(userAccountRepository.findByUsername(eq("integration-admin"))).thenReturn(Optional.of(user));
    when(userAccountRepository.findById(eq(2001L))).thenReturn(Optional.of(user));
    when(authQueryRepository.findRoleCodesByUserId(eq(2001L))).thenReturn(List.of("ADMIN"));
    when(authQueryRepository.findPermissionCodesByUserId(eq(2001L)))
        .thenReturn(List.of("iam.user.read", "iam.role.read"));

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/admin/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"integration-admin\",\"password\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.accessToken").isString())
            .andExpect(jsonPath("$.data.refreshToken").isString())
            .andReturn();

    JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = loginJson.path("data").path("accessToken").asText();
    String refreshToken = loginJson.path("data").path("refreshToken").asText();
    assertNotNull(accessToken);
    assertNotNull(refreshToken);

    MvcResult refreshResult =
        mockMvc
            .perform(
                post("/api/admin/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();

    JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
    String refreshedAccessToken = refreshJson.path("data").path("accessToken").asText();
    assertNotNull(refreshedAccessToken);
    assertNotEquals(accessToken, refreshedAccessToken);

    mockMvc
        .perform(
            get("/api/admin/v1/auth/me")
                .header("Authorization", "Bearer " + refreshedAccessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value(2001))
        .andExpect(jsonPath("$.data.username").value("integration-admin"))
        .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
  }

  @Test
  void refreshShouldRejectInvalidToken() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"invalid-token\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-401"));
  }

  private UserAccount activeUser(Long id, String username, String passwordHash, String displayName) {
    UserAccount user = new UserAccount();
    user.setId(id);
    user.setUsername(username);
    user.setPasswordHash(passwordHash);
    user.setDisplayName(displayName);
    user.setStatus("ACTIVE");
    return user;
  }
}
