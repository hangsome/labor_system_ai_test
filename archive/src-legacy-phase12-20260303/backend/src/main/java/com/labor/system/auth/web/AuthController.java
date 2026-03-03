package com.labor.system.auth.web;

import com.labor.system.auth.service.AuthService;
import com.labor.system.auth.web.dto.CurrentUserResponse;
import com.labor.system.auth.web.dto.LoginRequest;
import com.labor.system.auth.web.dto.RefreshRequest;
import com.labor.system.auth.web.dto.TokenResponse;
import com.labor.system.common.api.ApiResponse;
import com.labor.system.common.api.AppException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/auth")
public class AuthController {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse response = authService.login(request.getUsername(), request.getPassword());
    return ApiResponse.success(response);
  }

  @PostMapping("/refresh")
  public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    TokenResponse response = authService.refresh(request.getRefreshToken());
    return ApiResponse.success(response);
  }

  @GetMapping("/me")
  public ApiResponse<CurrentUserResponse> me(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    String accessToken = resolveBearerToken(authorization);
    CurrentUserResponse response = authService.currentUser(accessToken);
    return ApiResponse.success(response);
  }

  private String resolveBearerToken(String authorization) {
    if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
      throw AppException.unauthorized("未认证或令牌失效");
    }
    String token = authorization.substring(BEARER_PREFIX.length()).trim();
    if (token.isEmpty()) {
      throw AppException.unauthorized("未认证或令牌失效");
    }
    return token;
  }
}

