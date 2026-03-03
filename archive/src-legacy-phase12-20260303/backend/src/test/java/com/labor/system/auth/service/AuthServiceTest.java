package com.labor.system.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.labor.system.auth.domain.UserAccount;
import com.labor.system.auth.model.TokenPair;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.repository.AuthQueryRepository;
import com.labor.system.auth.repository.UserAccountRepository;
import com.labor.system.auth.web.dto.CurrentUserResponse;
import com.labor.system.auth.web.dto.TokenResponse;
import com.labor.system.common.api.AppException;
import com.labor.system.common.api.ErrorCodes;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserAccountRepository userAccountRepository;
  @Mock private AuthQueryRepository authQueryRepository;
  @Mock private JwtTokenService tokenService;
  @Mock private PasswordEncoder passwordEncoder;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService =
        new AuthService(userAccountRepository, authQueryRepository, tokenService, passwordEncoder);
  }

  @Test
  void loginShouldReturnTokenWhenCredentialValid() {
    UserAccount user = buildActiveUser(1001L, "admin", "123456", "系统管理员");
    when(userAccountRepository.findByUsername(eq("admin"))).thenReturn(Optional.of(user));
    when(tokenService.issueTokens(eq(1001L), eq("admin")))
        .thenReturn(new TokenPair("access-token", "refresh-token", 3600));

    TokenResponse response = authService.login("admin", "123456");

    assertEquals("access-token", response.accessToken());
    assertEquals("refresh-token", response.refreshToken());
    assertEquals(3600L, response.expiresIn());
  }

  @Test
  void loginShouldUsePasswordEncoderWhenHashLooksLikeBcrypt() {
    UserAccount user =
        buildActiveUser(
            1002L,
            "manager",
            "$2a$10$123456789012345678901uuN4MN7zEW9h7Qf2aE2VG6I7aTXPdY1S",
            "业务经理");
    when(userAccountRepository.findByUsername(eq("manager"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(
            eq("plain-password"),
            eq("$2a$10$123456789012345678901uuN4MN7zEW9h7Qf2aE2VG6I7aTXPdY1S")))
        .thenReturn(true);
    when(tokenService.issueTokens(eq(1002L), eq("manager")))
        .thenReturn(new TokenPair("new-access", "new-refresh", 3600));

    TokenResponse response = authService.login("manager", "plain-password");

    verify(passwordEncoder)
        .matches(
            eq("plain-password"),
            eq("$2a$10$123456789012345678901uuN4MN7zEW9h7Qf2aE2VG6I7aTXPdY1S"));
    assertEquals("new-access", response.accessToken());
  }

  @Test
  void loginShouldThrowUnauthorizedWhenUserMissing() {
    when(userAccountRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> authService.login("ghost", "123456"));

    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  @Test
  void loginShouldThrowUnauthorizedWhenPasswordMismatch() {
    UserAccount user = buildActiveUser(1003L, "auditor", "expected-password", "审计员");
    when(userAccountRepository.findByUsername(eq("auditor"))).thenReturn(Optional.of(user));

    AppException exception =
        assertThrows(AppException.class, () -> authService.login("auditor", "wrong-password"));

    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  @Test
  void refreshShouldReturnNewTokenPair() {
    when(tokenService.refreshAccessToken(eq("refresh-token")))
        .thenReturn(new TokenPair("access-2", "refresh-token", 3600));

    TokenResponse response = authService.refresh("refresh-token");

    assertEquals("access-2", response.accessToken());
    assertEquals("refresh-token", response.refreshToken());
  }

  @Test
  void refreshShouldThrowUnauthorizedWhenTokenInvalid() {
    when(tokenService.refreshAccessToken(eq("bad-refresh")))
        .thenThrow(AppException.unauthorized("未认证或令牌失效"));

    AppException exception =
        assertThrows(AppException.class, () -> authService.refresh("bad-refresh"));

    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  @Test
  void currentUserShouldReturnUserWithRolesAndPermissions() {
    UserAccount user = buildActiveUser(1004L, "operator", "pwd", "操作员");
    when(tokenService.parseAccessToken(eq("access-ok")))
        .thenReturn(new TokenPrincipal(1004L, "operator"));
    when(userAccountRepository.findById(eq(1004L))).thenReturn(Optional.of(user));
    when(authQueryRepository.findRoleCodesByUserId(eq(1004L))).thenReturn(List.of("ADMIN"));
    when(authQueryRepository.findPermissionCodesByUserId(eq(1004L)))
        .thenReturn(List.of("iam.user.read", "iam.role.read"));

    CurrentUserResponse response = authService.currentUser("access-ok");

    assertEquals(1004L, response.id());
    assertEquals("operator", response.username());
    assertEquals("操作员", response.displayName());
    assertEquals(1, response.roles().size());
    assertEquals(2, response.permissions().size());
  }

  @Test
  void currentUserShouldThrowUnauthorizedWhenUserInactive() {
    UserAccount user = buildActiveUser(1005L, "blocked", "pwd", "已禁用用户");
    user.setStatus("DISABLED");
    when(tokenService.parseAccessToken(eq("access-disabled")))
        .thenReturn(new TokenPrincipal(1005L, "blocked"));
    when(userAccountRepository.findById(eq(1005L))).thenReturn(Optional.of(user));

    AppException exception =
        assertThrows(AppException.class, () -> authService.currentUser("access-disabled"));

    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  private UserAccount buildActiveUser(Long id, String username, String passwordHash, String displayName) {
    UserAccount user = new UserAccount();
    user.setId(id);
    user.setUsername(username);
    user.setPasswordHash(passwordHash);
    user.setDisplayName(displayName);
    user.setStatus("ACTIVE");
    return user;
  }
}

