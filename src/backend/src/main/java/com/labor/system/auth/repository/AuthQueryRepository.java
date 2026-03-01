package com.labor.system.auth.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthQueryRepository {

  private static final String ROLE_CODE_SQL =
      """
      SELECT r.code
      FROM role r
      INNER JOIN user_role ur ON ur.role_id = r.id
      WHERE ur.user_id = ?
      ORDER BY r.code
      """;

  private static final String PERMISSION_CODE_SQL =
      """
      SELECT DISTINCT p.code
      FROM permission p
      INNER JOIN role_permission rp ON rp.permission_id = p.id
      INNER JOIN user_role ur ON ur.role_id = rp.role_id
      WHERE ur.user_id = ?
      ORDER BY p.code
      """;

  private final JdbcTemplate jdbcTemplate;

  public AuthQueryRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<String> findRoleCodesByUserId(Long userId) {
    return jdbcTemplate.queryForList(ROLE_CODE_SQL, String.class, userId);
  }

  public List<String> findPermissionCodesByUserId(Long userId) {
    return jdbcTemplate.queryForList(PERMISSION_CODE_SQL, String.class, userId);
  }
}

