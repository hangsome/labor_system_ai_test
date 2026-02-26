package com.labor.system.rbac.repository;

import com.labor.system.rbac.datascope.DataScopePolicy;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DataScopeRepository {

  private static final String FIND_POLICIES_BY_USER_ID_SQL =
      """
      SELECT dsp.scope_type, dsp.scope_ref
      FROM data_scope_policy dsp
      INNER JOIN user_role ur ON ur.role_id = dsp.role_id
      WHERE ur.user_id = ?
      ORDER BY dsp.id
      """;

  private final JdbcTemplate jdbcTemplate;

  public DataScopeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<DataScopePolicy> findPoliciesByUserId(Long userId) {
    return jdbcTemplate.query(
        FIND_POLICIES_BY_USER_ID_SQL,
        (rs, rowNum) -> new DataScopePolicy(rs.getString("scope_type"), rs.getString("scope_ref")),
        userId);
  }
}
