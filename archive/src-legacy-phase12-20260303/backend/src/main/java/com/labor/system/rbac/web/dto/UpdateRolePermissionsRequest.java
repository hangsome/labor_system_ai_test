package com.labor.system.rbac.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateRolePermissionsRequest {

  @NotNull(message = "permissionCodes 不能为空")
  @NotEmpty(message = "permissionCodes 不能为空")
  private List<String> permissionCodes;

  public List<String> getPermissionCodes() {
    return permissionCodes;
  }

  public void setPermissionCodes(List<String> permissionCodes) {
    this.permissionCodes = permissionCodes;
  }
}

