package com.labor.system.crm.lead.service;

import com.labor.system.common.api.AppException;
import com.labor.system.crm.lead.domain.CustomerLead;
import com.labor.system.crm.lead.repository.CustomerLeadRepository;
import com.labor.system.crm.lead.repository.LeadFollowUpRepository;
import com.labor.system.crm.lead.repository.LeadFollowUpRepository.LeadFollowUpRecord;
import com.labor.system.crm.lead.web.dto.CreateLeadFollowUpRequest;
import com.labor.system.crm.lead.web.dto.LeadFollowUpResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.LeadStatusTransitionRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadFlowService {

  private static final Map<String, Set<String>> STATUS_TRANSITION_MAP =
      Map.of(
          "NEW", Set.of("FOLLOWING", "WON", "LOST"),
          "FOLLOWING", Set.of("WON", "LOST"),
          "WON", Set.of(),
          "LOST", Set.of());

  private final CustomerLeadRepository customerLeadRepository;
  private final LeadFollowUpRepository leadFollowUpRepository;

  public LeadFlowService(
      CustomerLeadRepository customerLeadRepository, LeadFollowUpRepository leadFollowUpRepository) {
    this.customerLeadRepository = customerLeadRepository;
    this.leadFollowUpRepository = leadFollowUpRepository;
  }

  @Transactional
  public LeadResponse transitionStatus(Long leadId, LeadStatusTransitionRequest request) {
    CustomerLead lead = findLeadOrThrow(leadId);
    String statusFrom = normalizeStatus(lead.getCooperationStatus(), "cooperationStatus");
    String statusTo = normalizeStatus(request.getToStatus(), "toStatus");
    validateTransition(statusFrom, statusTo);

    lead.setCooperationStatus(statusTo);
    CustomerLead updatedLead = customerLeadRepository.save(lead);
    leadFollowUpRepository.insertStatusTransition(
        leadId, statusFrom, statusTo, normalizeOptionalText(request.getComment()));
    return toLeadResponse(updatedLead);
  }

  @Transactional
  public void createFollowUp(Long leadId, CreateLeadFollowUpRequest request) {
    CustomerLead lead = findLeadOrThrow(leadId);
    String content = normalizeRequiredText(request.getContent(), "content");
    leadFollowUpRepository.insertFollowUp(
        leadId, content, request.getNextContactAt(), lead.getCooperationStatus());
  }

  public List<LeadFollowUpResponse> listFollowUps(Long leadId) {
    findLeadOrThrow(leadId);
    return leadFollowUpRepository.findByLeadId(leadId).stream().map(this::toFollowUpResponse).toList();
  }

  private LeadFollowUpResponse toFollowUpResponse(LeadFollowUpRecord record) {
    return new LeadFollowUpResponse(
        record.id(),
        record.action(),
        record.content(),
        record.status(),
        record.statusFrom(),
        record.statusTo(),
        record.nextContactAt(),
        record.operatorId(),
        record.createdAt());
  }

  private LeadResponse toLeadResponse(CustomerLead customerLead) {
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

  private CustomerLead findLeadOrThrow(Long leadId) {
    return customerLeadRepository
        .findById(leadId)
        .orElseThrow(() -> AppException.badRequest("线索不存在"));
  }

  private void validateTransition(String statusFrom, String statusTo) {
    if (statusFrom.equals(statusTo)) {
      throw AppException.badRequest("非法状态流转");
    }
    Set<String> allowedStatus = STATUS_TRANSITION_MAP.get(statusFrom);
    if (allowedStatus == null || !allowedStatus.contains(statusTo)) {
      throw AppException.badRequest("非法状态流转");
    }
  }

  private String normalizeStatus(String value, String fieldName) {
    String normalized = normalizeRequiredText(value, fieldName);
    return normalized.toUpperCase(Locale.ROOT);
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

  private String toCipherText(byte[] value) {
    return value == null ? null : new String(value, StandardCharsets.UTF_8);
  }

  private String toDateTimeText(LocalDateTime value) {
    return value == null ? null : value.toString();
  }
}
