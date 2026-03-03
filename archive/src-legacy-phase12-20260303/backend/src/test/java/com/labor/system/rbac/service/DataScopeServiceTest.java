package com.labor.system.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.labor.system.rbac.datascope.DataScopePolicy;
import com.labor.system.rbac.repository.DataScopeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataScopeServiceTest {

  @Mock private DataScopeRepository dataScopeRepository;

  private DataScopeService dataScopeService;

  @BeforeEach
  void setUp() {
    dataScopeService = new DataScopeService(dataScopeRepository);
  }

  @Test
  void shouldReturnAllWhenUserHasAllPolicy() {
    when(dataScopeRepository.findPoliciesByUserId(eq(1001L)))
        .thenReturn(
            List.of(
                new DataScopePolicy("SELF", null),
                new DataScopePolicy("DEPT", "D001"),
                new DataScopePolicy("ALL", null)));

    DataScopePolicy result = dataScopeService.resolveByUserId(1001L);

    assertEquals(DataScopePolicy.ALL, result.scopeType());
    assertNull(result.scopeRef());
  }

  @Test
  void shouldReturnDeptWhenNoAllPolicy() {
    when(dataScopeRepository.findPoliciesByUserId(eq(1002L)))
        .thenReturn(List.of(new DataScopePolicy("SELF", null), new DataScopePolicy("DEPT", "D002")));

    DataScopePolicy result = dataScopeService.resolveByUserId(1002L);

    assertEquals(DataScopePolicy.DEPT, result.scopeType());
    assertEquals("D002", result.scopeRef());
  }

  @Test
  void shouldReturnSelfWithUserIdAsScopeRef() {
    when(dataScopeRepository.findPoliciesByUserId(eq(1003L)))
        .thenReturn(List.of(new DataScopePolicy("SELF", "ignored")));

    DataScopePolicy result = dataScopeService.resolveByUserId(1003L);

    assertEquals(DataScopePolicy.SELF, result.scopeType());
    assertEquals("1003", result.scopeRef());
  }

  @Test
  void shouldReturnNoneWhenPoliciesEmpty() {
    when(dataScopeRepository.findPoliciesByUserId(eq(1004L))).thenReturn(List.of());

    DataScopePolicy result = dataScopeService.resolveByUserId(1004L);

    assertEquals(DataScopePolicy.NONE, result.scopeType());
    assertNull(result.scopeRef());
  }

  @Test
  void shouldReturnNoneWhenOnlyInvalidPolicies() {
    when(dataScopeRepository.findPoliciesByUserId(eq(1005L)))
        .thenReturn(List.of(new DataScopePolicy("UNKNOWN", null), new DataScopePolicy("DEPT", " ")));

    DataScopePolicy result = dataScopeService.resolveByUserId(1005L);

    assertEquals(DataScopePolicy.NONE, result.scopeType());
    assertNull(result.scopeRef());
  }
}
