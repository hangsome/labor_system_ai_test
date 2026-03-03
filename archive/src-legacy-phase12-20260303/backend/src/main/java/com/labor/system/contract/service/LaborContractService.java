package com.labor.system.contract.service;

import com.labor.system.common.api.AppException;
import com.labor.system.contract.domain.LaborContract;
import com.labor.system.contract.repository.LaborContractRepository;
import com.labor.system.contract.web.dto.CreateLaborContractRequest;
import com.labor.system.contract.web.dto.LaborContractResponse;
import com.labor.system.contract.web.dto.RenewLaborContractRequest;
import com.labor.system.contract.web.dto.TerminateLaborContractRequest;
import com.labor.system.crm.employer.repository.EmployerUnitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaborContractService {

  private static final String STATUS_DRAFT = "DRAFT";
  private static final String STATUS_SIGNED = "SIGNED";
  private static final String STATUS_TERMINATED = "TERMINATED";
  private static final Set<String> ALLOWED_CONTRACT_TYPES = Set.of("A", "B");

  private final LaborContractRepository laborContractRepository;
  private final EmployerUnitRepository employerUnitRepository;

  public LaborContractService(
      LaborContractRepository laborContractRepository, EmployerUnitRepository employerUnitRepository) {
    this.laborContractRepository = laborContractRepository;
    this.employerUnitRepository = employerUnitRepository;
  }

  @Transactional
  public LaborContractResponse createContract(CreateLaborContractRequest request) {
    String contractNo = normalizeRequiredText(request.getContractNo(), "contractNo");
    if (laborContractRepository.existsByContractNo(contractNo)) {
      throw AppException.badRequest("contractNo 已存在");
    }
    if (!employerUnitRepository.existsById(request.getEmployerUnitId())) {
      throw AppException.badRequest("用工单位不存在");
    }
    validateContractDateRange(request.getStartDate(), request.getEndDate());
    validateContractType(request.getContractType());

    LaborContract contract = new LaborContract();
    contract.setContractNo(contractNo);
    contract.setEmployerUnitId(request.getEmployerUnitId());
    contract.setContractName(normalizeRequiredText(request.getContractName(), "contractName"));
    contract.setContractType(request.getContractType().trim().toUpperCase(Locale.ROOT));
    contract.setStartDate(request.getStartDate());
    contract.setEndDate(request.getEndDate());
    contract.setSettlementCycle(normalizeRequiredText(request.getSettlementCycle(), "settlementCycle"));
    contract.setTaxRate(request.getTaxRate());
    contract.setStatus(STATUS_DRAFT);
    LaborContract saved = laborContractRepository.save(contract);
    return toResponse(saved);
  }

  public LaborContractResponse getContract(Long contractId) {
    return toResponse(findContractOrThrow(contractId));
  }

  @Transactional
  public LaborContractResponse signContract(Long contractId) {
    LaborContract contract = findContractOrThrow(contractId);
    if (!STATUS_DRAFT.equals(contract.getStatus())) {
      throw AppException.badRequest("当前状态不允许签署");
    }
    contract.setStatus(STATUS_SIGNED);
    LaborContract updated = laborContractRepository.save(contract);
    return toResponse(updated);
  }

  @Transactional
  public LaborContractResponse renewContract(Long contractId, RenewLaborContractRequest request) {
    LaborContract contract = findContractOrThrow(contractId);
    if (!STATUS_SIGNED.equals(contract.getStatus())) {
      throw AppException.badRequest("当前状态不允许续签");
    }
    if (request.getNewEndDate() == null || !request.getNewEndDate().isAfter(contract.getEndDate())) {
      throw AppException.badRequest("newEndDate 必须晚于当前 endDate");
    }
    contract.setEndDate(request.getNewEndDate());
    LaborContract updated = laborContractRepository.save(contract);
    return toResponse(updated);
  }

  @Transactional
  public LaborContractResponse terminateContract(Long contractId, TerminateLaborContractRequest request) {
    LaborContract contract = findContractOrThrow(contractId);
    if (!STATUS_SIGNED.equals(contract.getStatus())) {
      throw AppException.badRequest("当前状态不允许终止");
    }
    if (request.getTerminateDate() == null) {
      throw AppException.badRequest("terminateDate 不能为空");
    }
    if (request.getTerminateDate().isBefore(contract.getStartDate())) {
      throw AppException.badRequest("terminateDate 不能早于 startDate");
    }
    contract.setEndDate(request.getTerminateDate());
    contract.setStatus(STATUS_TERMINATED);
    LaborContract updated = laborContractRepository.save(contract);
    return toResponse(updated);
  }

  private LaborContract findContractOrThrow(Long contractId) {
    return laborContractRepository
        .findById(contractId)
        .orElseThrow(() -> AppException.badRequest("合同不存在"));
  }

  private void validateContractType(String contractType) {
    String value = normalizeRequiredText(contractType, "contractType").toUpperCase(Locale.ROOT);
    if (!ALLOWED_CONTRACT_TYPES.contains(value)) {
      throw AppException.badRequest("contractType 仅支持 A/B");
    }
  }

  private void validateContractDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw AppException.badRequest("startDate/endDate 不能为空");
    }
    if (!endDate.isAfter(startDate)) {
      throw AppException.badRequest("endDate 必须晚于 startDate");
    }
  }

  private LaborContractResponse toResponse(LaborContract contract) {
    return new LaborContractResponse(
        contract.getId(),
        contract.getContractNo(),
        contract.getEmployerUnitId(),
        contract.getContractName(),
        contract.getContractType(),
        contract.getStartDate(),
        contract.getEndDate(),
        contract.getSettlementCycle(),
        contract.getStatus(),
        contract.getTaxRate(),
        toDateTimeText(contract.getCreatedAt()),
        toDateTimeText(contract.getUpdatedAt()));
  }

  private String normalizeRequiredText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw AppException.badRequest(fieldName + " 不能为空");
    }
    return value.trim();
  }

  private String toDateTimeText(LocalDateTime value) {
    return value == null ? null : value.toString();
  }
}
