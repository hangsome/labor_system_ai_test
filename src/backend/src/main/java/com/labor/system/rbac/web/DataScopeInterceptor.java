package com.labor.system.rbac.web;

import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.common.api.AppException;
import com.labor.system.rbac.datascope.DataScopeContext;
import com.labor.system.rbac.datascope.DataScopePolicy;
import com.labor.system.rbac.service.DataScopeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class DataScopeInterceptor implements HandlerInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String FORBIDDEN_MESSAGE = "无数据访问权限";

  private final JwtTokenService jwtTokenService;
  private final DataScopeService dataScopeService;

  public DataScopeInterceptor(JwtTokenService jwtTokenService, DataScopeService dataScopeService) {
    this.jwtTokenService = jwtTokenService;
    this.dataScopeService = dataScopeService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    DataScopeContext.clear();
    if (jwtTokenService == null || dataScopeService == null) {
      return true;
    }

    String accessToken = resolveBearerToken(request.getHeader("Authorization"));
    TokenPrincipal principal = jwtTokenService.parseAccessToken(accessToken);
    DataScopePolicy policy = dataScopeService.resolveByUserId(principal.userId());
    validateRequestScope(policy, principal.userId(), request);

    DataScopeContext context =
        new DataScopeContext(principal.userId(), policy.scopeType(), policy.scopeRef());
    DataScopeContext.bind(context);
    request.setAttribute(DataScopeContext.REQUEST_ATTRIBUTE, context);
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    DataScopeContext.clear();
  }

  private void validateRequestScope(
      DataScopePolicy policy, Long userId, HttpServletRequest request) {
    String scopeType = policy.scopeType();
    if (DataScopePolicy.NONE.equals(scopeType)) {
      throw AppException.forbidden(FORBIDDEN_MESSAGE);
    }
    if (DataScopePolicy.DEPT.equals(scopeType)) {
      String requestScopeRef = trimToNull(request.getParameter("scopeRef"));
      if (requestScopeRef != null && !requestScopeRef.equals(policy.scopeRef())) {
        throw AppException.forbidden(FORBIDDEN_MESSAGE);
      }
      return;
    }
    if (DataScopePolicy.SELF.equals(scopeType)) {
      String requestUserId = trimToNull(request.getParameter("userId"));
      if (requestUserId != null && !requestUserId.equals(String.valueOf(userId))) {
        throw AppException.forbidden(FORBIDDEN_MESSAGE);
      }
    }
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

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
