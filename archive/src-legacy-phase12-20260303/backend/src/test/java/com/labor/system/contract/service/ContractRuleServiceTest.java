package com.labor.system.contract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.labor.system.common.api.AppException;
import com.labor.system.contract.domain.LaborContract;
import com.labor.system.contract.domain.SettlementRule;
import com.labor.system.contract.repository.LaborContractRepository;
import com.labor.system.contract.repository.SettlementRuleRepository;
import com.labor.system.contract.web.dto.CreateSettlementRuleRequest;
import com.labor.system.contract.web.dto.LaborContractResponse;
import com.labor.system.contract.web.dto.RenewLaborContractRequest;
import com.labor.system.contract.web.dto.SettlementRuleResponse;
import com.labor.system.contract.web.dto.TerminateLaborContractRequest;
import com.labor.system.crm.employer.repository.EmployerUnitRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRuleServiceTest {

  @Mock private LaborContractRepository laborContractRepository;
  @Mock private EmployerUnitRepository employerUnitRepository;
  @Mock private SettlementRuleRepository settlementRuleRepository;

  private LaborContractService laborContractService;
  private SettlementRuleService settlementRuleService;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    laborContractService = new LaborContractService(laborContractRepository, employerUnitRepository);
    settlementRuleService =
        new SettlementRuleService(settlementRuleRepository, laborContractRepository, objectMapper);
  }

  @Test
  void signContractShouldUpdateStatusWhenDraft() {
    LaborContract contract =
        buildLaborContract(1L, "DRAFT", LocalDate.parse("2026-03-01"), LocalDate.parse("2027-03-01"));
    when(laborContractRepository.findById(eq(1L))).thenReturn(Optional.of(contract));
    when(laborContractRepository.save(any(LaborContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LaborContractResponse response = laborContractService.signContract(1L);

    assertEquals("SIGNED", response.status());
    verify(laborContractRepository).save(any(LaborContract.class));
  }

  @Test
  void signContractShouldRejectWhenCurrentStatusIsNotDraft() {
    LaborContract contract =
        buildLaborContract(
            1L, "TERMINATED", LocalDate.parse("2026-03-01"), LocalDate.parse("2027-03-01"));
    when(laborContractRepository.findById(eq(1L))).thenReturn(Optional.of(contract));

    AppException exception = assertThrows(AppException.class, () -> laborContractService.signContract(1L));

    assertEquals("REQ-400", exception.getCode());
    verify(laborContractRepository, never()).save(any(LaborContract.class));
  }

  @Test
  void renewContractShouldRejectWhenNewEndDateNotAfterCurrent() {
    LaborContract contract =
        buildLaborContract(1L, "SIGNED", LocalDate.parse("2026-03-01"), LocalDate.parse("2027-03-01"));
    when(laborContractRepository.findById(eq(1L))).thenReturn(Optional.of(contract));
    RenewLaborContractRequest request = new RenewLaborContractRequest();
    request.setNewEndDate(LocalDate.parse("2027-03-01"));

    AppException exception =
        assertThrows(AppException.class, () -> laborContractService.renewContract(1L, request));

    assertEquals("REQ-400", exception.getCode());
    verify(laborContractRepository, never()).save(any(LaborContract.class));
  }

  @Test
  void terminateContractShouldRejectWhenTerminateDateBeforeStartDate() {
    LaborContract contract =
        buildLaborContract(1L, "SIGNED", LocalDate.parse("2026-03-01"), LocalDate.parse("2027-03-01"));
    when(laborContractRepository.findById(eq(1L))).thenReturn(Optional.of(contract));
    TerminateLaborContractRequest request = new TerminateLaborContractRequest();
    request.setTerminateDate(LocalDate.parse("2026-02-28"));

    AppException exception =
        assertThrows(AppException.class, () -> laborContractService.terminateContract(1L, request));

    assertEquals("REQ-400", exception.getCode());
    verify(laborContractRepository, never()).save(any(LaborContract.class));
  }

  @Test
  void createRuleShouldRejectWhenVersionDuplicated() {
    when(laborContractRepository.existsById(eq(11L))).thenReturn(true);
    when(settlementRuleRepository.existsByContractIdAndVersionNo(eq(11L), eq(1))).thenReturn(true);
    CreateSettlementRuleRequest request = new CreateSettlementRuleRequest();
    request.setContractId(11L);
    request.setRuleType("HOURLY");
    request.setVersionNo(1);
    request.setEffectiveFrom(LocalDate.parse("2026-03-01"));
    request.setRulePayload("{\"unitPrice\":35}");

    AppException exception = assertThrows(AppException.class, () -> settlementRuleService.createRule(request));

    assertEquals("REQ-400", exception.getCode());
    verify(settlementRuleRepository, never()).save(any(SettlementRule.class));
  }

  @Test
  void publishRuleShouldRejectWhenSameEffectiveDateHasPublishedRule() {
    SettlementRule current =
        buildSettlementRule(1L, 11L, 1, LocalDate.parse("2026-03-01"), wrappedPayload("DRAFT", 35));
    SettlementRule conflict =
        buildSettlementRule(2L, 11L, 2, LocalDate.parse("2026-03-01"), wrappedPayload("PUBLISHED", 36));
    when(settlementRuleRepository.findById(eq(1L))).thenReturn(Optional.of(current));
    when(settlementRuleRepository.findByContractIdAndEffectiveFrom(
            eq(11L), eq(LocalDate.parse("2026-03-01"))))
        .thenReturn(List.of(current, conflict));

    AppException exception = assertThrows(AppException.class, () -> settlementRuleService.publishRule(1L));

    assertEquals("REQ-400", exception.getCode());
    verify(settlementRuleRepository, never()).save(any(SettlementRule.class));
  }

  @Test
  void deactivateRuleShouldRejectWhenRuleNotPublished() {
    SettlementRule draftRule =
        buildSettlementRule(1L, 11L, 1, LocalDate.parse("2026-03-01"), wrappedPayload("DRAFT", 35));
    when(settlementRuleRepository.findById(eq(1L))).thenReturn(Optional.of(draftRule));

    AppException exception = assertThrows(AppException.class, () -> settlementRuleService.deactivateRule(1L));

    assertEquals("REQ-400", exception.getCode());
    verify(settlementRuleRepository, never()).save(any(SettlementRule.class));
  }

  @Test
  void getActiveRuleShouldReturnLatestPublishedVersion() {
    SettlementRule draftFuture =
        buildSettlementRule(2L, 11L, 2, LocalDate.parse("2026-04-01"), wrappedPayload("DRAFT", 36));
    SettlementRule publishedCurrent =
        buildSettlementRule(
            1L, 11L, 1, LocalDate.parse("2026-03-01"), wrappedPayload("PUBLISHED", 35));
    when(laborContractRepository.existsById(eq(11L))).thenReturn(true);
    when(settlementRuleRepository.findByContractIdOrderByVersionNoDesc(eq(11L)))
        .thenReturn(List.of(draftFuture, publishedCurrent));

    SettlementRuleResponse response =
        settlementRuleService.getActiveRule(11L, LocalDate.parse("2026-03-15"));

    assertEquals(1, response.versionNo());
    assertEquals("PUBLISHED", response.status());
  }

  @Test
  void publishRuleShouldReturnCurrentRuleWhenAlreadyPublished() {
    SettlementRule publishedRule =
        buildSettlementRule(
            1L, 11L, 1, LocalDate.parse("2026-03-01"), wrappedPayload("PUBLISHED", 35));
    when(settlementRuleRepository.findById(eq(1L))).thenReturn(Optional.of(publishedRule));

    SettlementRuleResponse response = settlementRuleService.publishRule(1L);

    assertEquals("PUBLISHED", response.status());
    verify(settlementRuleRepository, never()).save(any(SettlementRule.class));
  }

  private LaborContract buildLaborContract(Long id, String status, LocalDate startDate, LocalDate endDate) {
    LaborContract contract = new LaborContract();
    contract.setId(id);
    contract.setContractNo("LC-001");
    contract.setEmployerUnitId(100L);
    contract.setContractName("酒店劳务外包合同");
    contract.setContractType("A");
    contract.setStartDate(startDate);
    contract.setEndDate(endDate);
    contract.setSettlementCycle("MONTHLY");
    contract.setStatus(status);
    contract.setTaxRate(new BigDecimal("0.0600"));
    return contract;
  }

  private SettlementRule buildSettlementRule(
      Long id, Long contractId, Integer versionNo, LocalDate effectiveFrom, String payload) {
    SettlementRule rule = new SettlementRule();
    rule.setId(id);
    rule.setContractId(contractId);
    rule.setRuleType("HOURLY");
    rule.setVersionNo(versionNo);
    rule.setEffectiveFrom(effectiveFrom);
    rule.setRulePayload(payload);
    return rule;
  }

  private String wrappedPayload(String status, int unitPrice) {
    ObjectNode root = objectMapper.createObjectNode();
    ObjectNode content = root.putObject("content");
    content.put("unitPrice", unitPrice);
    ObjectNode lifecycle = root.putObject("lifecycle");
    lifecycle.put("status", status);
    if ("PUBLISHED".equals(status)) {
      lifecycle.put("publishedAt", "2026-02-27T00:12:00");
      lifecycle.putNull("deactivatedAt");
    } else if ("DISABLED".equals(status)) {
      lifecycle.put("publishedAt", "2026-02-27T00:12:00");
      lifecycle.put("deactivatedAt", "2026-02-27T00:30:00");
    } else {
      lifecycle.putNull("publishedAt");
      lifecycle.putNull("deactivatedAt");
    }
    try {
      return objectMapper.writeValueAsString(root);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
