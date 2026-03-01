package com.labor.system.crm.lead.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "customer_lead")
public class CustomerLead {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "lead_code", nullable = false, length = 64)
  private String leadCode;

  @Column(name = "project_name", nullable = false, length = 255)
  private String projectName;

  @Column(name = "contact_name", length = 64)
  private String contactName;

  @Column(name = "contact_phone_cipher")
  private byte[] contactPhoneCipher;

  @Column(name = "industry_type", nullable = false, length = 32)
  private String industryType;

  @Column(name = "biz_owner_id", nullable = false)
  private Long bizOwnerId;

  @Column(name = "cooperation_status", nullable = false, length = 32)
  private String cooperationStatus;

  @Column(name = "tender_at")
  private LocalDate tenderAt;

  @Column(name = "deposit_status", nullable = false, length = 32)
  private String depositStatus;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public byte[] getContactPhoneCipher() {
    return contactPhoneCipher;
  }

  public void setContactPhoneCipher(byte[] contactPhoneCipher) {
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
