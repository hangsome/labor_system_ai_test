package com.labor.system.rbac.service;

import com.labor.system.common.api.AppException;
import com.labor.system.rbac.repository.RolePermissionRepository;
import com.labor.system.rbac.repository.RolePermissionRepository.PermissionRecord;
import com.labor.system.rbac.repository.RolePermissionRepository.RoleRecord;
import com.labor.system.rbac.web.dto.DataScopePolicyResponse;
import com.labor.system.rbac.web.dto.RoleSummaryResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolePermissionService {

  private final RolePermissionRepository rolePermissionRepository;

  public RolePermissionService(RolePermissionRepository rolePermissionRepository) {
    this.rolePermissionRepository = rolePermissionRepository;
  }

  public List<RoleSummaryResponse> listRoles() {
    List<RoleRecord> roles = rolePermissionRepository.findRoles();
    List<RoleSummaryResponse> result = new ArrayList<>(roles.size());
    for (RoleRecord role : roles) {
      List<String> permissionCodes = rolePermissionRepository.findPermissionCodesByRoleId(role.id());
      DataScopePolicyResponse dataScopePolicy = rolePermissionRepository.findDataScopeByRoleId(role.id());
      if (dataScopePolicy == null) {
        dataScopePolicy = new DataScopePolicyResponse(role.id(), "NONE", null);
      }
      result.add(
          new RoleSummaryResponse(
              role.id(), role.code(), role.name(), permissionCodes, dataScopePolicy));
    }
    return result;
  }

  @Transactional
  public void updateRolePermissions(Long roleId, List<String> permissionCodes) {
    if (!rolePermissionRepository.roleExists(roleId)) {
      throw AppException.badRequest("角色不存在");
    }
    if (permissionCodes == null || permissionCodes.isEmpty()) {
      throw AppException.badRequest("permissionCodes 不能为空");
    }

    Map<String, Long> permissionCodeIdMap = new LinkedHashMap<>();
    List<String> normalizedCodes = normalizeCodes(permissionCodes);
    List<PermissionRecord> permissionRecords =
        rolePermissionRepository.findPermissionsByCodes(normalizedCodes);
    for (PermissionRecord permissionRecord : permissionRecords) {
      permissionCodeIdMap.put(permissionRecord.code(), permissionRecord.id());
    }
    if (permissionCodeIdMap.size() != normalizedCodes.size()) {
      throw AppException.badRequest("包含无效权限编码");
    }

    rolePermissionRepository.replaceRolePermissions(roleId, new ArrayList<>(permissionCodeIdMap.values()));
  }

  public DataScopePolicyResponse getDataScopePolicy(Long roleId) {
    if (!rolePermissionRepository.roleExists(roleId)) {
      throw AppException.badRequest("角色不存在");
    }
    DataScopePolicyResponse policy = rolePermissionRepository.findDataScopeByRoleId(roleId);
    return policy == null ? new DataScopePolicyResponse(roleId, "NONE", null) : policy;
  }

  private List<String> normalizeCodes(List<String> permissionCodes) {
    List<String> normalized = new ArrayList<>();
    for (String code : permissionCodes) {
      if (code == null) {
        continue;
      }
      String value = code.trim();
      if (value.isEmpty() || normalized.contains(value)) {
        continue;
      }
      normalized.add(value);
    }
    return normalized;
  }
}

