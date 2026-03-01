package com.labor.system.crm.lead.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.labor.system.common.api.AppException;
import com.labor.system.crm.lead.domain.CustomerLead;
import com.labor.system.crm.lead.repository.CustomerLeadRepository;
import com.labor.system.crm.lead.web.dto.CreateLeadRequest;
import com.labor.system.crm.lead.web.dto.LeadPageResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.UpdateLeadRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

  @Mock private CustomerLeadRepository customerLeadRepository;

  private LeadService leadService;

  @BeforeEach
  void setUp() {
    leadService = new LeadService(customerLeadRepository);
  }

  @Test
  void createLeadShouldPersistWhenLeadCodeUnique() {
    when(customerLeadRepository.existsByLeadCode(eq("LEAD-001"))).thenReturn(false);
    when(customerLeadRepository.save(any(CustomerLead.class)))
        .thenAnswer(
            invocation -> {
              CustomerLead lead = invocation.getArgument(0);
              lead.setId(1L);
              return lead;
            });

    CreateLeadRequest request = new CreateLeadRequest();
    request.setLeadCode("LEAD-001");
    request.setProjectName("酒店外包项目");
    request.setIndustryType("HOTEL");
    request.setBizOwnerId(1001L);
    request.setCooperationStatus("NEW");
    request.setDepositStatus("UNPAID");
    request.setContactPhoneCipher("cipher-text");

    LeadResponse response = leadService.createLead(request);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("LEAD-001", response.leadCode());
    assertEquals("cipher-text", response.contactPhoneCipher());
  }

  @Test
  void createLeadShouldRejectDuplicateLeadCode() {
    when(customerLeadRepository.existsByLeadCode(eq("LEAD-001"))).thenReturn(true);

    CreateLeadRequest request = new CreateLeadRequest();
    request.setLeadCode("LEAD-001");
    request.setProjectName("酒店外包项目");
    request.setIndustryType("HOTEL");
    request.setBizOwnerId(1001L);
    request.setCooperationStatus("NEW");
    request.setDepositStatus("UNPAID");

    AppException exception = assertThrows(AppException.class, () -> leadService.createLead(request));
    assertEquals("REQ-400", exception.getCode());
    verify(customerLeadRepository, never()).save(any(CustomerLead.class));
  }

  @Test
  void listLeadsShouldReturnPagedResult() {
    CustomerLead lead = createLead(1L, "LEAD-001", "FOLLOWING");
    when(customerLeadRepository.search(eq("酒店"), eq("FOLLOWING"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(lead), PageRequest.of(0, 10), 1));

    LeadPageResponse pageResponse = leadService.listLeads("酒店", "FOLLOWING", 1, 10);

    assertEquals(1L, pageResponse.total());
    assertEquals(1, pageResponse.records().size());
    assertEquals("LEAD-001", pageResponse.records().get(0).leadCode());
  }

  @Test
  void listLeadsShouldRejectInvalidPageSize() {
    AppException exception = assertThrows(AppException.class, () -> leadService.listLeads(null, null, 1, 0));
    assertEquals("REQ-400", exception.getCode());
  }

  @Test
  void updateLeadShouldRejectDuplicateLeadCode() {
    CustomerLead lead = createLead(1L, "LEAD-001", "NEW");
    when(customerLeadRepository.findById(eq(1L))).thenReturn(Optional.of(lead));
    when(customerLeadRepository.existsByLeadCodeAndIdNot(eq("LEAD-002"), eq(1L))).thenReturn(true);

    UpdateLeadRequest request = new UpdateLeadRequest();
    request.setLeadCode("LEAD-002");
    request.setProjectName("酒店外包项目");
    request.setIndustryType("HOTEL");
    request.setBizOwnerId(1001L);
    request.setCooperationStatus("NEW");
    request.setDepositStatus("UNPAID");

    AppException exception = assertThrows(AppException.class, () -> leadService.updateLead(1L, request));
    assertEquals("REQ-400", exception.getCode());
    verify(customerLeadRepository, never()).save(any(CustomerLead.class));
  }

  @Test
  void deleteLeadShouldRejectWhenLeadNotFound() {
    when(customerLeadRepository.existsById(eq(999L))).thenReturn(false);

    AppException exception = assertThrows(AppException.class, () -> leadService.deleteLead(999L));

    assertEquals("REQ-400", exception.getCode());
    verify(customerLeadRepository, never()).deleteById(any(Long.class));
  }

  private CustomerLead createLead(Long id, String leadCode, String status) {
    CustomerLead lead = new CustomerLead();
    lead.setId(id);
    lead.setLeadCode(leadCode);
    lead.setProjectName("酒店外包项目");
    lead.setContactName("张三");
    lead.setContactPhoneCipher("cipher-text".getBytes());
    lead.setIndustryType("HOTEL");
    lead.setBizOwnerId(1001L);
    lead.setCooperationStatus(status);
    lead.setDepositStatus("UNPAID");
    return lead;
  }
}
