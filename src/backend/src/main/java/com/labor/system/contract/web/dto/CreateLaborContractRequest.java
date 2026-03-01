package com.labor.system.contract.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateLaborContractRequest {

  @NotBlank(message = "contractNo 不能为空")
  @Size(max = 128, message = "contractNo 长度不能超过 128")
  private String contractNo;

  @NotNull(message = "employerUnitId 不能为空")
  private Long employerUnitId;

  @NotBlank(message = "contractName 不能为空")
  @Size(max = 255, message = "contractName 长度不能超过 255")
  private String contractName;

  @NotBlank(message = "contractType 不能为空")
  @Size(max = 16, message = "contractType 长度不能超过 16")
  private String contractType;

  @NotNull(message = "startDate 不能为空")
  private LocalDate startDate;

  @NotNull(message = "endDate 不能为空")
  private LocalDate endDate;

  @NotBlank(message = "settlementCycle 不能为空")
  @Size(max = 32, message = "settlementCycle 长度不能超过 32")
  private String settlementCycle;

  @NotNull(message = "taxRate 不能为空")
  @DecimalMin(value = "0.0000", message = "taxRate 不能小于 0")
  @DecimalMax(value = "1.0000", message = "taxRate 不能大于 1")
  private BigDecimal taxRate;

  public String getContractNo() {
    return contractNo;
  }

  public void setContractNo(String contractNo) {
    this.contractNo = contractNo;
  }

  public Long getEmployerUnitId() {
    return employerUnitId;
  }

  public void setEmployerUnitId(Long employerUnitId) {
    this.employerUnitId = employerUnitId;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public String getContractType() {
    return contractType;
  }

  public void setContractType(String contractType) {
    this.contractType = contractType;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getSettlementCycle() {
    return settlementCycle;
  }

  public void setSettlementCycle(String settlementCycle) {
    this.settlementCycle = settlementCycle;
  }

  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }
}
