package com.labor.system.platform.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.common.api.AppException;
import com.labor.system.platform.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private static final String MASK = "******";

  private final AuditLogService auditLogService;
  private final JwtTokenService jwtTokenService;
  private final ObjectMapper objectMapper;

  public AuditLogAspect(
      AuditLogService auditLogService, JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
    this.auditLogService = auditLogService;
    this.jwtTokenService = jwtTokenService;
    this.objectMapper = objectMapper;
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
          buildSanitizedDetail(joinPoint));
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

  private String buildSanitizedDetail(ProceedingJoinPoint joinPoint) {
    if (!(joinPoint.getSignature() instanceof CodeSignature codeSignature)) {
      return null;
    }
    Map<String, Object> detail = new LinkedHashMap<>();
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] arguments = joinPoint.getArgs();
    for (int i = 0; i < parameterNames.length; i++) {
      detail.put(parameterNames[i], sanitizeValue(parameterNames[i], arguments[i]));
    }
    try {
      return objectMapper.writeValueAsString(detail);
    } catch (Exception ex) {
      return null;
    }
  }

  private Object sanitizeValue(String fieldName, Object value) {
    if (isSensitiveField(fieldName)) {
      return MASK;
    }
    if (value == null) {
      return null;
    }
    if (value instanceof Map<?, ?> map) {
      Map<String, Object> sanitized = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String key = entry.getKey() == null ? "null" : String.valueOf(entry.getKey());
        sanitized.put(key, sanitizeValue(key, entry.getValue()));
      }
      return sanitized;
    }
    if (value instanceof Iterable<?> iterable) {
      List<Object> items = new ArrayList<>();
      for (Object item : iterable) {
        items.add(sanitizeValue(fieldName, item));
      }
      return items;
    }
    return value;
  }

  private boolean isSensitiveField(String fieldName) {
    if (fieldName == null) {
      return false;
    }
    String normalized = fieldName.toLowerCase();
    return normalized.contains("password")
        || normalized.contains("token")
        || normalized.contains("secret")
        || normalized.contains("credential");
  }

  private HttpServletRequest currentRequest() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
      return null;
    }
    return attributes.getRequest();
  }
}
