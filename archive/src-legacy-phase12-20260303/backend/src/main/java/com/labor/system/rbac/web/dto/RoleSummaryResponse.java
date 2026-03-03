package com.labor.system.rbac.web.dto;

import java.util.List;

public record RoleSummaryResponse(
    Long id,
    String code,
    String name,
    List<String> permissionCodes,
    DataScopePolicyResponse dataScopePolicy) {}

