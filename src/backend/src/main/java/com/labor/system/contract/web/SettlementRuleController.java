package com.labor.system.contract.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.contract.service.SettlementRuleService;
import com.labor.system.contract.web.dto.CreateSettlementRuleRequest;
import com.labor.system.contract.web.dto.SettlementRuleResponse;
import com.labor.system.contract.web.dto.SettlementRuleVersionListResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/contracts")
public class SettlementRuleController {

  private final SettlementRuleService settlementRuleService;

  public SettlementRuleController(SettlementRuleService settlementRuleService) {
    this.settlementRuleService = settlementRuleService;
  }

  @PostMapping("/settlement-rules")
  public ApiResponse<SettlementRuleResponse> createRule(
      @Valid @RequestBody CreateSettlementRuleRequest request) {
    return ApiResponse.success(settlementRuleService.createRule(request));
  }

  @PutMapping("/settlement-rules/{ruleId}/publish")
  public ApiResponse<SettlementRuleResponse> publishRule(@PathVariable("ruleId") Long ruleId) {
    return ApiResponse.success(settlementRuleService.publishRule(ruleId));
  }

  @PutMapping("/settlement-rules/{ruleId}/deactivate")
  public ApiResponse<SettlementRuleResponse> deactivateRule(@PathVariable("ruleId") Long ruleId) {
    return ApiResponse.success(settlementRuleService.deactivateRule(ruleId));
  }

  @GetMapping("/{contractId}/settlement-rules/versions")
  public ApiResponse<SettlementRuleVersionListResponse> listVersions(
      @PathVariable("contractId") Long contractId) {
    return ApiResponse.success(settlementRuleService.listVersions(contractId));
  }

  @GetMapping("/{contractId}/settlement-rules/active")
  public ApiResponse<SettlementRuleResponse> getActiveRule(
      @PathVariable("contractId") Long contractId,
      @RequestParam(value = "onDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate onDate) {
    return ApiResponse.success(settlementRuleService.getActiveRule(contractId, onDate));
  }
}
