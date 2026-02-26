package com.labor.system.auth.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.auth.service.AuthService;
import com.labor.system.auth.web.dto.CurrentUserResponse;
import com.labor.system.auth.web.dto.TokenResponse;
import com.labor.system.common.api.AppException;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;

  @Test
  void loginShouldReturnTokenPair() throws Exception {
    when(authService.login(eq("admin"), eq("123456")))
        .thenReturn(new TokenResponse("access-token", "refresh-token", 3600));

    mockMvc
        .perform(
            post("/api/admin/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
        .andExpect(jsonPath("$.data.expiresIn").value(3600));
  }

  @Test
  void loginShouldReturnBadRequestWhenRequestInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void loginShouldReturnUnauthorizedWhenCredentialInvalid() throws Exception {
    when(authService.login(eq("admin"), eq("wrong")))
        .thenThrow(AppException.unauthorized("用户名或密码错误"));

    mockMvc
        .perform(
            post("/api/admin/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-401"));
  }

  @Test
  void refreshShouldReturnNewAccessToken() throws Exception {
    when(authService.refresh(eq("refresh-token")))
        .thenReturn(new TokenResponse("new-access-token", "refresh-token", 3600));

    mockMvc
        .perform(
            post("/api/admin/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"refresh-token\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
  }

  @Test
  void meShouldReturnCurrentUser() throws Exception {
    CurrentUserResponse currentUserResponse =
        new CurrentUserResponse(
            1L, "admin", "系统管理员", List.of("ADMIN"), List.of("iam.user.read", "iam.role.read"));
    when(authService.currentUser(eq("access-token"))).thenReturn(currentUserResponse);

    mockMvc
        .perform(get("/api/admin/v1/auth/me").header("Authorization", "Bearer access-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("admin"))
        .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
  }

  @Test
  void meShouldReturnUnauthorizedWhenBearerHeaderMissing() throws Exception {
    mockMvc
        .perform(get("/api/admin/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-401"));
  }
}
