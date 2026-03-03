package com.labor.system.rbac.service;

import com.labor.system.rbac.datascope.DataScopePolicy;
import com.labor.system.rbac.repository.DataScopeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DataScopeService {

  private final DataScopeRepository dataScopeRepository;

  public DataScopeService(DataScopeRepository dataScopeRepository) {
    this.dataScopeRepository = dataScopeRepository;
  }

  public DataScopePolicy resolveByUserId(Long userId) {
    List<DataScopePolicy> policies = dataScopeRepository.findPoliciesByUserId(userId);
    DataScopePolicy selected = null;
    int selectedPriority = Integer.MAX_VALUE;
    for (DataScopePolicy policy : policies) {
      DataScopePolicy normalized = normalizePolicy(policy, userId);
      int priority = priority(normalized.scopeType());
      if (priority < selectedPriority) {
        selected = normalized;
        selectedPriority = priority;
      }
    }
    return selected == null ? DataScopePolicy.none() : selected;
  }

  private DataScopePolicy normalizePolicy(DataScopePolicy policy, Long userId) {
    String scopeType = toUpper(policy.scopeType());
    String scopeRef = trimToNull(policy.scopeRef());
    return switch (scopeType) {
      case DataScopePolicy.ALL -> new DataScopePolicy(DataScopePolicy.ALL, null);
      case DataScopePolicy.DEPT ->
          scopeRef == null
              ? DataScopePolicy.none()
              : new DataScopePolicy(DataScopePolicy.DEPT, scopeRef);
      case DataScopePolicy.SELF ->
          new DataScopePolicy(DataScopePolicy.SELF, String.valueOf(userId));
      default -> DataScopePolicy.none();
    };
  }

  private int priority(String scopeType) {
    return switch (scopeType) {
      case DataScopePolicy.ALL -> 0;
      case DataScopePolicy.DEPT -> 1;
      case DataScopePolicy.SELF -> 2;
      case DataScopePolicy.NONE -> 3;
      default -> 4;
    };
  }

  private String toUpper(String value) {
    if (value == null) {
      return DataScopePolicy.NONE;
    }
    String normalized = value.trim().toUpperCase();
    return normalized.isEmpty() ? DataScopePolicy.NONE : normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
