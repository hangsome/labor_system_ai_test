package com.labor.system.crm.employer.repository;

import com.labor.system.crm.employer.domain.EmployerUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployerUnitRepository extends JpaRepository<EmployerUnit, Long> {

  boolean existsByUnitCode(String unitCode);

  boolean existsByUnitCodeAndIdNot(String unitCode, Long id);

  @Query(
      """
      SELECT unit
      FROM EmployerUnit unit
      WHERE (:keyword IS NULL
              OR LOWER(unit.unitCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(unit.unitName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:customerLevel IS NULL OR unit.customerLevel = :customerLevel)
      """)
  Page<EmployerUnit> search(
      @Param("keyword") String keyword,
      @Param("customerLevel") String customerLevel,
      Pageable pageable);
}
