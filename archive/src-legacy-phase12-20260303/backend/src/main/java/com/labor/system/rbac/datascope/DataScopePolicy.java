package com.labor.system.rbac.datascope;

public record DataScopePolicy(String scopeType, String scopeRef) {

  public static final String ALL = "ALL";
  public static final String DEPT = "DEPT";
  public static final String SELF = "SELF";
  public static final String NONE = "NONE";

  public static DataScopePolicy none() {
    return new DataScopePolicy(NONE, null);
  }
}
