package com.labor.system.contract.repository;

import com.labor.system.contract.domain.LaborContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaborContractRepository extends JpaRepository<LaborContract, Long> {

  boolean existsByContractNo(String contractNo);

  boolean existsByContractNoAndIdNot(String contractNo, Long id);
}
