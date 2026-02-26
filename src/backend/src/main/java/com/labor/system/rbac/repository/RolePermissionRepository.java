package com.labor.system.rbac.repository;

import com.labor.system.rbac.web.dto.DataScopePolicyResponse;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RolePermissionRepository {

  private static final RowMapper<RoleRecord> ROLE_ROW_MAPPER =
      (rs, rowNum) -> new RoleRecord(rs.getLong("id"), rs.getString("code"), rs.getString("name"));

  private static final RowMapper<PermissionRecord> PERMISSION_ROW_MAPPER =
      (rs, rowNum) -> new PermissionRecord(rs.getLong("id"), rs.getString("code"));

  private static final String FIND_ROLES_SQL = "SELECT id, code, name FROM role ORDER BY id";
  private static final String FIND_PERMISSION_CODES_BY_ROLE_ID_SQL =
      """
      SELECT p.code
      FROM permission p
      INNER JOIN role_permission rp ON rp.permission_id = p.id
      WHERE rp.role_id = ?
      ORDER BY p.code
      """;
  private static final String FIND_DATA_SCOPE_BY_ROLE_ID_SQL =
      """
      SELECT role_id, scope_type, scope_ref
      FROM data_scope_policy
      WHERE role_id = ?
      ORDER BY id
      LIMIT 1
      """;
  private static final String COUNT_ROLE_BY_ID_SQL = "SELECT COUNT(1) FROM role WHERE id = ?";
  private static final String FIND_PERMISSION_BY_CODES_SQL =
      """
      SELECT id, code
      FROM permission
      WHERE code IN (:codes)
      ORDER BY id
      """;
  private static final String DELETE_ROLE_PERMISSIONS_SQL =
      "DELETE FROM role_permission WHERE role_id = ?";
  private static final String INSERT_ROLE_PERMISSION_SQL =
      "INSERT INTO role_permission(role_id, permission_id) VALUES(?, ?)";

  private final JdbcTemplate jdbcTemplate;

  public RolePermissionRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<RoleRecord> findRoles() {
    return jdbcTemplate.query(FIND_ROLES_SQL, ROLE_ROW_MAPPER);
  }

  public List<String> findPermissionCodesByRoleId(Long roleId) {
    return jdbcTemplate.queryForList(FIND_PERMISSION_CODES_BY_ROLE_ID_SQL, String.class, roleId);
  }

  public DataScopePolicyResponse findDataScopeByRoleId(Long roleId) {
    List<DataScopePolicyResponse> policies =
        jdbcTemplate.query(
            FIND_DATA_SCOPE_BY_ROLE_ID_SQL,
            (rs, rowNum) ->
                new DataScopePolicyResponse(
                    rs.getLong("role_id"), rs.getString("scope_type"), rs.getString("scope_ref")),
            roleId);
    return policies.isEmpty() ? null : policies.get(0);
  }

  public boolean roleExists(Long roleId) {
    Integer count = jdbcTemplate.queryForObject(COUNT_ROLE_BY_ID_SQL, Integer.class, roleId);
    return count != null && count > 0;
  }

  public List<PermissionRecord> findPermissionsByCodes(List<String> codes) {
    if (codes.isEmpty()) {
      return Collections.emptyList();
    }
    String placeholders = String.join(",", Collections.nCopies(codes.size(), "?"));
    String sql = FIND_PERMISSION_BY_CODES_SQL.replace(":codes", placeholders);
    return jdbcTemplate.query(sql, PERMISSION_ROW_MAPPER, codes.toArray());
  }

  public void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
    jdbcTemplate.update(DELETE_ROLE_PERMISSIONS_SQL, roleId);
    if (permissionIds.isEmpty()) {
      return;
    }
    jdbcTemplate.batchUpdate(
        INSERT_ROLE_PERMISSION_SQL,
        permissionIds,
        permissionIds.size(),
        (PreparedStatement ps, Long permissionId) -> {
          ps.setLong(1, roleId);
          ps.setLong(2, permissionId);
        });
  }

  public record RoleRecord(Long id, String code, String name) {}

  public record PermissionRecord(Long id, String code) {}
}

