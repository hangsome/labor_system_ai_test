package com.labor.system.crm.lead.repository;

import com.labor.system.crm.lead.domain.CustomerLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerLeadRepository extends JpaRepository<CustomerLead, Long> {

  boolean existsByLeadCode(String leadCode);

  boolean existsByLeadCodeAndIdNot(String leadCode, Long id);

  @Query(
      """
      SELECT lead
      FROM CustomerLead lead
      WHERE (:status IS NULL OR lead.cooperationStatus = :status)
        AND (
          :keyword IS NULL
          OR LOWER(lead.leadCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(lead.projectName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(COALESCE(lead.contactName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
      """)
  Page<CustomerLead> search(
      @Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}
