package com.labor.system.contract.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateSettlementRuleRequest {

  @NotNull(message = "contractId 不能为空")
  private Long contractId;

  @NotBlank(message = "ruleType 不能为空")
  @Size(max = 16, message = "ruleType 长度不能超过 16")
  private String ruleType;

  @NotNull(message = "versionNo 不能为空")
  @Min(value = 1, message = "versionNo 必须大于等于 1")
  private Integer versionNo;

  @NotNull(message = "effectiveFrom 不能为空")
  private LocalDate effectiveFrom;

  @NotBlank(message = "rulePayload 不能为空")
  private String rulePayload;

  public Long getContractId() {
    return contractId;
  }

  public void setContractId(Long contractId) {
    this.contractId = contractId;
  }

  public String getRuleType() {
    return ruleType;
  }

  public void setRuleType(String ruleType) {
    this.ruleType = ruleType;
  }

  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  public LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(LocalDate effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  public String getRulePayload() {
    return rulePayload;
  }

  public void setRulePayload(String rulePayload) {
    this.rulePayload = rulePayload;
  }
}
