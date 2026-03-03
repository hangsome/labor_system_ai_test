package com.labor.system.crm.lead.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UpdateLeadRequest {

  @NotBlank(message = "leadCode 不能为空")
  @Size(max = 64, message = "leadCode 长度不能超过 64")
  private String leadCode;

  @NotBlank(message = "projectName 不能为空")
  @Size(max = 255, message = "projectName 长度不能超过 255")
  private String projectName;

  @Size(max = 64, message = "contactName 长度不能超过 64")
  private String contactName;

  @Size(max = 256, message = "contactPhoneCipher 长度不能超过 256")
  private String contactPhoneCipher;

  @NotBlank(message = "industryType 不能为空")
  @Size(max = 32, message = "industryType 长度不能超过 32")
  private String industryType;

  @NotNull(message = "bizOwnerId 不能为空")
  private Long bizOwnerId;

  @NotBlank(message = "cooperationStatus 不能为空")
  @Size(max = 32, message = "cooperationStatus 长度不能超过 32")
  private String cooperationStatus;

  private LocalDate tenderAt;

  @NotBlank(message = "depositStatus 不能为空")
  @Size(max = 32, message = "depositStatus 长度不能超过 32")
  private String depositStatus;

  public String getLeadCode() {
    return leadCode;
  }

  public void setLeadCode(String leadCode) {
    this.leadCode = leadCode;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getContactPhoneCipher() {
    return contactPhoneCipher;
  }

  public void setContactPhoneCipher(String contactPhoneCipher) {
    this.contactPhoneCipher = contactPhoneCipher;
  }

  public String getIndustryType() {
    return industryType;
  }

  public void setIndustryType(String industryType) {
    this.industryType = industryType;
  }

  public Long getBizOwnerId() {
    return bizOwnerId;
  }

  public void setBizOwnerId(Long bizOwnerId) {
    this.bizOwnerId = bizOwnerId;
  }

  public String getCooperationStatus() {
    return cooperationStatus;
  }

  public void setCooperationStatus(String cooperationStatus) {
    this.cooperationStatus = cooperationStatus;
  }

  public LocalDate getTenderAt() {
    return tenderAt;
  }

  public void setTenderAt(LocalDate tenderAt) {
    this.tenderAt = tenderAt;
  }

  public String getDepositStatus() {
    return depositStatus;
  }

  public void setDepositStatus(String depositStatus) {
    this.depositStatus = depositStatus;
  }
}
