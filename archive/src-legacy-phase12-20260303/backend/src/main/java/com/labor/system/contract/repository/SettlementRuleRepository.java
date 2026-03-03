package com.labor.system.contract.repository;

import com.labor.system.contract.domain.SettlementRule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRuleRepository extends JpaRepository<SettlementRule, Long> {

  boolean existsByContractIdAndVersionNo(Long contractId, Integer versionNo);

  List<SettlementRule> findByContractIdOrderByVersionNoDesc(Long contractId);

  List<SettlementRule> findByContractIdAndEffectiveFrom(Long contractId, LocalDate effectiveFrom);
}
