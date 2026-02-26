package com.labor.system.platform.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.platform.audit.service.AuditLogService;
import java.lang.reflect.Method;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

  @Mock private AuditLogService auditLogService;

  @Mock private JwtTokenService jwtTokenService;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private CodeSignature codeSignature;

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
    MDC.clear();
  }

  @Test
  void shouldRecordAuditWithSanitizedFields() throws Throwable {
    AuditLogAspect aspect = new AuditLogAspect(auditLogService, jwtTokenService, new ObjectMapper());
    AuditAction auditAction = getAuditAction("updateRolePermissions");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer access-1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    MDC.put("traceId", "trace-001");

    when(joinPoint.proceed()).thenReturn("OK");
    when(joinPoint.getSignature()).thenReturn(codeSignature);
    when(codeSignature.getParameterNames())
        .thenReturn(new String[] {"roleId", "password", "accessToken", "payload"});
    when(joinPoint.getArgs())
        .thenReturn(new Object[] {1L, "plain-pass", "token-abc", Map.of("refreshToken", "r1", "safe", "ok")});
    when(jwtTokenService.parseAccessToken(eq("access-1"))).thenReturn(new TokenPrincipal(9001L, "admin"));

    Object result = aspect.around(joinPoint, auditAction);

    assertEquals("OK", result);
    ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
    verify(auditLogService)
        .record(
            eq("ROLE_PERMISSION"),
            eq("1"),
            eq("UPDATE"),
            eq(9001L),
            eq("trace-001"),
            detailCaptor.capture());
    String detail = detailCaptor.getValue();
    assertNotNull(detail);
    assertTrue(detail.contains("\"password\":\"******\""));
    assertTrue(detail.contains("\"accessToken\":\"******\""));
    assertTrue(detail.contains("\"refreshToken\":\"******\""));
    assertTrue(detail.contains("\"safe\":\"ok\""));
  }

  @Test
  void shouldNotBreakBusinessFlowWhenAuditWriteFails() throws Throwable {
    AuditLogAspect aspect = new AuditLogAspect(auditLogService, jwtTokenService, new ObjectMapper());
    AuditAction auditAction = getAuditAction("updateRolePermissions");

    when(joinPoint.proceed()).thenReturn("OK");
    when(joinPoint.getSignature()).thenReturn(codeSignature);
    when(codeSignature.getParameterNames()).thenReturn(new String[] {"roleId"});
    when(joinPoint.getArgs()).thenReturn(new Object[] {2L});
    doThrow(new RuntimeException("db down"))
        .when(auditLogService)
        .record(any(), any(), any(), any(), any(), any());

    Object result = aspect.around(joinPoint, auditAction);

    assertEquals("OK", result);
    ArgumentCaptor<Long> operatorIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<String> traceIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(auditLogService)
        .record(
            eq("ROLE_PERMISSION"),
            eq("2"),
            eq("UPDATE"),
            operatorIdCaptor.capture(),
            traceIdCaptor.capture(),
            any());
    assertEquals(0L, operatorIdCaptor.getValue());
    assertNotNull(traceIdCaptor.getValue());
  }

  private AuditAction getAuditAction(String methodName) throws NoSuchMethodException {
    Method method =
        DummyTarget.class.getDeclaredMethod(
            methodName, Long.class, String.class, String.class, Map.class);
    return method.getAnnotation(AuditAction.class);
  }

  private static class DummyTarget {

    @AuditAction(bizType = "ROLE_PERMISSION", action = "UPDATE", bizIdArg = "roleId")
    public String updateRolePermissions(
        Long roleId, String password, String accessToken, Map<String, Object> payload) {
      return "OK";
    }
  }
}
