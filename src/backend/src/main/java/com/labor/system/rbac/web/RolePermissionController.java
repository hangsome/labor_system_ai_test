package com.labor.system.rbac.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.platform.audit.AuditAction;
import com.labor.system.rbac.service.RolePermissionService;
import com.labor.system.rbac.web.dto.DataScopePolicyResponse;
import com.labor.system.rbac.web.dto.RoleSummaryResponse;
import com.labor.system.rbac.web.dto.UpdateRolePermissionsRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/iam")
public class RolePermissionController {

  private final RolePermissionService rolePermissionService;

  public RolePermissionController(RolePermissionService rolePermissionService) {
    this.rolePermissionService = rolePermissionService;
  }

  @GetMapping("/roles")
  public ApiResponse<List<RoleSummaryResponse>> listRoles() {
    return ApiResponse.success(rolePermissionService.listRoles());
  }

  @PutMapping("/roles/{roleId}/permissions")
  @AuditAction(bizType = "ROLE_PERMISSION", action = "UPDATE", bizIdArg = "roleId")
  public ApiResponse<Void> updateRolePermissions(
      @PathVariable("roleId") Long roleId,
      @Valid @RequestBody UpdateRolePermissionsRequest request) {
    rolePermissionService.updateRolePermissions(roleId, request.getPermissionCodes());
    return ApiResponse.success(null);
  }

  @GetMapping("/roles/{roleId}/data-scope")
  public ApiResponse<DataScopePolicyResponse> getRoleDataScope(@PathVariable("roleId") Long roleId) {
    return ApiResponse.success(rolePermissionService.getDataScopePolicy(roleId));
  }
}
