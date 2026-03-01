package com.labor.system.crm.lead.service;

import com.labor.system.common.api.AppException;
import com.labor.system.crm.lead.domain.CustomerLead;
import com.labor.system.crm.lead.repository.CustomerLeadRepository;
import com.labor.system.crm.lead.web.dto.CreateLeadRequest;
import com.labor.system.crm.lead.web.dto.LeadPageResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.UpdateLeadRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {

  private final CustomerLeadRepository customerLeadRepository;

  public LeadService(CustomerLeadRepository customerLeadRepository) {
    this.customerLeadRepository = customerLeadRepository;
  }

  @Transactional
  public LeadResponse createLead(CreateLeadRequest request) {
    String leadCode = normalizeRequiredText(request.getLeadCode(), "leadCode");
    if (customerLeadRepository.existsByLeadCode(leadCode)) {
      throw AppException.badRequest("leadCode 已存在");
    }

    CustomerLead customerLead = new CustomerLead();
    applyCreateRequest(customerLead, request);
    customerLead.setLeadCode(leadCode);
    CustomerLead saved = customerLeadRepository.save(customerLead);
    return toResponse(saved);
  }

  public LeadResponse getLead(Long leadId) {
    return toResponse(findLeadOrThrow(leadId));
  }

  public LeadPageResponse listLeads(String keyword, String status, int page, int pageSize) {
    if (page < 1) {
      throw AppException.badRequest("page 必须大于等于 1");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw AppException.badRequest("pageSize 必须在 1-100 之间");
    }

    String normalizedKeyword = normalizeOptionalText(keyword);
    String normalizedStatus = normalizeOptionalText(status);
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    Page<CustomerLead> result =
        customerLeadRepository.search(normalizedKeyword, normalizedStatus, pageRequest);
    List<LeadResponse> records = result.getContent().stream().map(this::toResponse).toList();
    return new LeadPageResponse(records, result.getTotalElements(), page, pageSize);
  }

  @Transactional
  public LeadResponse updateLead(Long leadId, UpdateLeadRequest request) {
    CustomerLead customerLead = findLeadOrThrow(leadId);
    String leadCode = normalizeRequiredText(request.getLeadCode(), "leadCode");
    if (!leadCode.equals(customerLead.getLeadCode())
        && customerLeadRepository.existsByLeadCodeAndIdNot(leadCode, leadId)) {
      throw AppException.badRequest("leadCode 已存在");
    }

    applyUpdateRequest(customerLead, request);
    customerLead.setLeadCode(leadCode);
    CustomerLead updated = customerLeadRepository.save(customerLead);
    return toResponse(updated);
  }

  @Transactional
  public void deleteLead(Long leadId) {
    if (!customerLeadRepository.existsById(leadId)) {
      throw AppException.badRequest("线索不存在");
    }
    customerLeadRepository.deleteById(leadId);
  }

  private CustomerLead findLeadOrThrow(Long leadId) {
    return customerLeadRepository
        .findById(leadId)
        .orElseThrow(() -> AppException.badRequest("线索不存在"));
  }

  private void applyCreateRequest(CustomerLead customerLead, CreateLeadRequest request) {
    customerLead.setProjectName(normalizeRequiredText(request.getProjectName(), "projectName"));
    customerLead.setContactName(normalizeOptionalText(request.getContactName()));
    customerLead.setContactPhoneCipher(toCipherBytes(request.getContactPhoneCipher()));
    customerLead.setIndustryType(normalizeRequiredText(request.getIndustryType(), "industryType"));
    customerLead.setBizOwnerId(request.getBizOwnerId());
    customerLead.setCooperationStatus(
        normalizeRequiredText(request.getCooperationStatus(), "cooperationStatus"));
    customerLead.setTenderAt(request.getTenderAt());
    customerLead.setDepositStatus(normalizeRequiredText(request.getDepositStatus(), "depositStatus"));
  }

  private void applyUpdateRequest(CustomerLead customerLead, UpdateLeadRequest request) {
    customerLead.setProjectName(normalizeRequiredText(request.getProjectName(), "projectName"));
    customerLead.setContactName(normalizeOptionalText(request.getContactName()));
    customerLead.setContactPhoneCipher(toCipherBytes(request.getContactPhoneCipher()));
    customerLead.setIndustryType(normalizeRequiredText(request.getIndustryType(), "industryType"));
    customerLead.setBizOwnerId(request.getBizOwnerId());
    customerLead.setCooperationStatus(
        normalizeRequiredText(request.getCooperationStatus(), "cooperationStatus"));
    customerLead.setTenderAt(request.getTenderAt());
    customerLead.setDepositStatus(normalizeRequiredText(request.getDepositStatus(), "depositStatus"));
  }

  private LeadResponse toResponse(CustomerLead customerLead) {
    return new LeadResponse(
        customerLead.getId(),
        customerLead.getLeadCode(),
        customerLead.getProjectName(),
        customerLead.getContactName(),
        toCipherText(customerLead.getContactPhoneCipher()),
        customerLead.getIndustryType(),
        customerLead.getBizOwnerId(),
        customerLead.getCooperationStatus(),
        customerLead.getTenderAt(),
        customerLead.getDepositStatus(),
        toDateTimeText(customerLead.getCreatedAt()),
        toDateTimeText(customerLead.getUpdatedAt()));
  }

  private String normalizeRequiredText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw AppException.badRequest(fieldName + " 不能为空");
    }
    return value.trim();
  }

  private String normalizeOptionalText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private byte[] toCipherBytes(String value) {
    String normalized = normalizeOptionalText(value);
    return normalized == null ? null : normalized.getBytes(StandardCharsets.UTF_8);
  }

  private String toCipherText(byte[] value) {
    return value == null ? null : new String(value, StandardCharsets.UTF_8);
  }

  private String toDateTimeText(LocalDateTime value) {
    return value == null ? null : value.toString();
  }
}
