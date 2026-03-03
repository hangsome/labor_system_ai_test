package com.labor.system.rbac.datascope;

import java.util.Optional;

public record DataScopeContext(Long userId, String scopeType, String scopeRef) {

  public static final String REQUEST_ATTRIBUTE = "DATA_SCOPE_CONTEXT";
  private static final ThreadLocal<DataScopeContext> HOLDER = new ThreadLocal<>();

  public static void bind(DataScopeContext context) {
    HOLDER.set(context);
  }

  public static Optional<DataScopeContext> current() {
    return Optional.ofNullable(HOLDER.get());
  }

  public static DataScopeContext required() {
    return current().orElseThrow(() -> new IllegalStateException("数据范围上下文缺失"));
  }

  public static void clear() {
    HOLDER.remove();
  }
}
