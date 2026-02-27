package com.labor.system.crm.employer.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateEmployerUnitRequest {

  @NotBlank(message = "unitCode 不能为空")
  @Size(max = 64, message = "unitCode 长度不能超过 64")
  private String unitCode;

  private Long leadId;

  @NotBlank(message = "unitName 不能为空")
  @Size(max = 255, message = "unitName 长度不能超过 255")
  private String unitName;

  @NotBlank(message = "customerLevel 不能为空")
  @Size(max = 16, message = "customerLevel 长度不能超过 16")
  private String customerLevel;

  @Size(max = 512, message = "address 长度不能超过 512")
  private String address;

  private String invoiceInfo;

  private Boolean outsource;

  public String getUnitCode() {
    return unitCode;
  }

  public void setUnitCode(String unitCode) {
    this.unitCode = unitCode;
  }

  public Long getLeadId() {
    return leadId;
  }

  public void setLeadId(Long leadId) {
    this.leadId = leadId;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  public String getCustomerLevel() {
    return customerLevel;
  }

  public void setCustomerLevel(String customerLevel) {
    this.customerLevel = customerLevel;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getInvoiceInfo() {
    return invoiceInfo;
  }

  public void setInvoiceInfo(String invoiceInfo) {
    this.invoiceInfo = invoiceInfo;
  }

  public Boolean getOutsource() {
    return outsource;
  }

  public void setOutsource(Boolean outsource) {
    this.outsource = outsource;
  }
}
