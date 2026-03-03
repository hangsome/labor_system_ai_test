package com.labor.system.crm.lead.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.labor.system.common.api.AppException;
import com.labor.system.crm.lead.domain.CustomerLead;
import com.labor.system.crm.lead.repository.CustomerLeadRepository;
import com.labor.system.crm.lead.repository.LeadFollowUpRepository;
import com.labor.system.crm.lead.repository.LeadFollowUpRepository.LeadFollowUpRecord;
import com.labor.system.crm.lead.web.dto.CreateLeadFollowUpRequest;
import com.labor.system.crm.lead.web.dto.LeadFollowUpResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.LeadStatusTransitionRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeadFlowServiceTest {

  @Mock private CustomerLeadRepository customerLeadRepository;
  @Mock private LeadFollowUpRepository leadFollowUpRepository;

  private LeadFlowService leadFlowService;

  @BeforeEach
  void setUp() {
    leadFlowService = new LeadFlowService(customerLeadRepository, leadFollowUpRepository);
  }

  @Test
  void transitionStatusShouldUpdateLeadWhenTransitionAllowed() {
    CustomerLead lead = createLead(1L, "NEW");
    when(customerLeadRepository.findById(eq(1L))).thenReturn(Optional.of(lead));
    when(customerLeadRepository.save(any(CustomerLead.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LeadStatusTransitionRequest request = new LeadStatusTransitionRequest();
    request.setToStatus("FOLLOWING");
    request.setComment("首次跟进");

    LeadResponse response = leadFlowService.transitionStatus(1L, request);

    assertEquals("FOLLOWING", response.cooperationStatus());
    verify(leadFollowUpRepository).insertStatusTransition(1L, "NEW", "FOLLOWING", "首次跟进");
  }

  @Test
  void transitionStatusShouldRejectIllegalTransition() {
    CustomerLead lead = createLead(1L, "WON");
    when(customerLeadRepository.findById(eq(1L))).thenReturn(Optional.of(lead));

    LeadStatusTransitionRequest request = new LeadStatusTransitionRequest();
    request.setToStatus("FOLLOWING");

    AppException exception =
        assertThrows(AppException.class, () -> leadFlowService.transitionStatus(1L, request));
    assertEquals("REQ-400", exception.getCode());
    verify(customerLeadRepository, never()).save(any(CustomerLead.class));
    verify(leadFollowUpRepository, never())
        .insertStatusTransition(any(Long.class), any(String.class), any(String.class), any(String.class));
  }

  @Test
  void createFollowUpShouldInsertFollowUpRecord() {
    CustomerLead lead = createLead(1L, "FOLLOWING");
    when(customerLeadRepository.findById(eq(1L))).thenReturn(Optional.of(lead));

    CreateLeadFollowUpRequest request = new CreateLeadFollowUpRequest();
    request.setContent("客户确认下周沟通");

    leadFlowService.createFollowUp(1L, request);

    verify(leadFollowUpRepository).insertFollowUp(1L, "客户确认下周沟通", null, "FOLLOWING");
  }

  @Test
  void listFollowUpsShouldReturnRecordsByLeadId() {
    CustomerLead lead = createLead(1L, "FOLLOWING");
    when(customerLeadRepository.findById(eq(1L))).thenReturn(Optional.of(lead));
    when(leadFollowUpRepository.findByLeadId(eq(1L)))
        .thenReturn(
            List.of(
                new LeadFollowUpRecord(
                    10L,
                    "FOLLOW_UP",
                    0L,
                    "客户确认下周沟通",
                    "FOLLOWING",
                    null,
                    null,
                    null,
                    "2026-02-26T22:33:00")));

    List<LeadFollowUpResponse> result = leadFlowService.listFollowUps(1L);

    assertEquals(1, result.size());
    assertEquals("FOLLOW_UP", result.get(0).action());
    assertEquals("客户确认下周沟通", result.get(0).content());
  }

  private CustomerLead createLead(Long leadId, String status) {
    CustomerLead lead = new CustomerLead();
    lead.setId(leadId);
    lead.setLeadCode("LEAD-001");
    lead.setProjectName("酒店外包项目");
    lead.setIndustryType("HOTEL");
    lead.setBizOwnerId(1001L);
    lead.setCooperationStatus(status);
    lead.setDepositStatus("UNPAID");
    return lead;
  }
}
