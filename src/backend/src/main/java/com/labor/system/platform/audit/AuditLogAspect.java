package com.labor.system.platform.audit;

import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.common.api.AppException;
import com.labor.system.platform.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogAspect.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private final AuditLogService auditLogService;
  private final JwtTokenService jwtTokenService;

  public AuditLogAspect(AuditLogService auditLogService, JwtTokenService jwtTokenService) {
    this.auditLogService = auditLogService;
    this.jwtTokenService = jwtTokenService;
  }

  @Around("@annotation(auditAction)")
  public Object around(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
    Object result = joinPoint.proceed();
    try {
      String bizId = resolveBizId(joinPoint, auditAction.bizIdArg());
      auditLogService.record(
          auditAction.bizType(),
          bizId,
          auditAction.action(),
          resolveOperatorId(),
          resolveTraceId(),
          null);
    } catch (Exception ex) {
      LOGGER.warn("Failed to write audit log for {}.{}", auditAction.bizType(), auditAction.action(), ex);
    }
    return result;
  }

  private String resolveBizId(ProceedingJoinPoint joinPoint, String bizIdArg) {
    if (bizIdArg == null || bizIdArg.trim().isEmpty()) {
      return "UNKNOWN";
    }
    if (!(joinPoint.getSignature() instanceof CodeSignature codeSignature)) {
      return "UNKNOWN";
    }
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] arguments = joinPoint.getArgs();
    for (int i = 0; i < parameterNames.length; i++) {
      if (bizIdArg.equals(parameterNames[i])) {
        Object value = arguments[i];
        return value == null ? "UNKNOWN" : String.valueOf(value);
      }
    }
    return "UNKNOWN";
  }

  private Long resolveOperatorId() {
    HttpServletRequest request = currentRequest();
    if (request == null) {
      return 0L;
    }
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
      return 0L;
    }
    String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
    if (accessToken.isEmpty()) {
      return 0L;
    }
    try {
      TokenPrincipal principal = jwtTokenService.parseAccessToken(accessToken);
      return principal.userId();
    } catch (AppException ex) {
      return 0L;
    }
  }

  private String resolveTraceId() {
    String traceId = MDC.get("traceId");
    return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
  }

  private HttpServletRequest currentRequest() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
      return null;
    }
    return attributes.getRequest();
  }
}
