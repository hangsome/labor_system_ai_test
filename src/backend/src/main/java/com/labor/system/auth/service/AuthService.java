package com.labor.system.auth.service;

import com.labor.system.auth.domain.UserAccount;
import com.labor.system.auth.model.TokenPair;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.repository.AuthQueryRepository;
import com.labor.system.auth.repository.UserAccountRepository;
import com.labor.system.auth.web.dto.CurrentUserResponse;
import com.labor.system.auth.web.dto.TokenResponse;
import com.labor.system.common.api.AppException;
import java.util.List;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserAccountRepository userAccountRepository;
  private final AuthQueryRepository authQueryRepository;
  private final InMemoryTokenService tokenService;
  private final PasswordEncoder passwordEncoder;

  public AuthService(
      UserAccountRepository userAccountRepository,
      AuthQueryRepository authQueryRepository,
      InMemoryTokenService tokenService,
      PasswordEncoder passwordEncoder) {
    this.userAccountRepository = userAccountRepository;
    this.authQueryRepository = authQueryRepository;
    this.tokenService = tokenService;
    this.passwordEncoder = passwordEncoder;
  }

  public TokenResponse login(String username, String password) {
    UserAccount user =
        userAccountRepository
            .findByUsername(username)
            .filter(account -> "ACTIVE".equalsIgnoreCase(account.getStatus()))
            .orElseThrow(() -> AppException.unauthorized("用户名或密码错误"));

    if (!passwordMatches(password, user.getPasswordHash())) {
      throw AppException.unauthorized("用户名或密码错误");
    }

    TokenPair tokenPair = tokenService.issueTokens(user.getId(), user.getUsername());
    return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken(), tokenPair.expiresIn());
  }

  public TokenResponse refresh(String refreshToken) {
    TokenPair tokenPair = tokenService.refreshAccessToken(refreshToken);
    return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken(), tokenPair.expiresIn());
  }

  public CurrentUserResponse currentUser(String accessToken) {
    TokenPrincipal principal = tokenService.parseAccessToken(accessToken);
    UserAccount user =
        userAccountRepository
            .findById(principal.userId())
            .filter(account -> "ACTIVE".equalsIgnoreCase(account.getStatus()))
            .orElseThrow(() -> AppException.unauthorized("未认证或令牌失效"));

    List<String> roles = authQueryRepository.findRoleCodesByUserId(user.getId());
    List<String> permissions = authQueryRepository.findPermissionCodesByUserId(user.getId());
    return new CurrentUserResponse(
        user.getId(), user.getUsername(), user.getDisplayName(), roles, permissions);
  }

  private boolean passwordMatches(String rawPassword, String storedPasswordHash) {
    if (storedPasswordHash == null) {
      return false;
    }
    if (looksLikeBcryptHash(storedPasswordHash)) {
      return passwordEncoder.matches(rawPassword, storedPasswordHash);
    }
    return Objects.equals(rawPassword, storedPasswordHash);
  }

  private boolean looksLikeBcryptHash(String value) {
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
  }
}

