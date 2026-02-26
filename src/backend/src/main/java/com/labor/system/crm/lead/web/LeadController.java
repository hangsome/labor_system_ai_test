package com.labor.system.crm.lead.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.crm.lead.service.LeadFlowService;
import com.labor.system.crm.lead.service.LeadService;
import com.labor.system.crm.lead.web.dto.CreateLeadFollowUpRequest;
import com.labor.system.crm.lead.web.dto.CreateLeadRequest;
import com.labor.system.crm.lead.web.dto.LeadFollowUpResponse;
import com.labor.system.crm.lead.web.dto.LeadPageResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.LeadStatusTransitionRequest;
import com.labor.system.crm.lead.web.dto.UpdateLeadRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/v1/crm/leads")
public class LeadController {

  private final LeadService leadService;
  private final LeadFlowService leadFlowService;

  public LeadController(LeadService leadService, LeadFlowService leadFlowService) {
    this.leadService = leadService;
    this.leadFlowService = leadFlowService;
  }

  @PostMapping
  public ApiResponse<LeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
    return ApiResponse.success(leadService.createLead(request));
  }

  @GetMapping("/{leadId}")
  public ApiResponse<LeadResponse> getLead(@PathVariable("leadId") Long leadId) {
    return ApiResponse.success(leadService.getLead(leadId));
  }

  @GetMapping
  public ApiResponse<LeadPageResponse> listLeads(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "page 必须大于等于 1")
          int page,
      @RequestParam(value = "pageSize", defaultValue = "20")
          @Min(value = 1, message = "pageSize 必须大于等于 1")
          @Max(value = 100, message = "pageSize 不能超过 100")
          int pageSize) {
    return ApiResponse.success(leadService.listLeads(keyword, status, page, pageSize));
  }

  @PutMapping("/{leadId}")
  public ApiResponse<LeadResponse> updateLead(
      @PathVariable("leadId") Long leadId, @Valid @RequestBody UpdateLeadRequest request) {
    return ApiResponse.success(leadService.updateLead(leadId, request));
  }

  @PutMapping("/{leadId}/status")
  public ApiResponse<LeadResponse> transitionStatus(
      @PathVariable("leadId") Long leadId,
      @Valid @RequestBody LeadStatusTransitionRequest request) {
    return ApiResponse.success(leadFlowService.transitionStatus(leadId, request));
  }

  @PostMapping("/{leadId}/follow-ups")
  public ApiResponse<Void> createFollowUp(
      @PathVariable("leadId") Long leadId, @Valid @RequestBody CreateLeadFollowUpRequest request) {
    leadFlowService.createFollowUp(leadId, request);
    return ApiResponse.success(null);
  }

  @GetMapping("/{leadId}/follow-ups")
  public ApiResponse<List<LeadFollowUpResponse>> listFollowUps(@PathVariable("leadId") Long leadId) {
    return ApiResponse.success(leadFlowService.listFollowUps(leadId));
  }

  @DeleteMapping("/{leadId}")
  public ApiResponse<Void> deleteLead(@PathVariable("leadId") Long leadId) {
    leadService.deleteLead(leadId);
    return ApiResponse.success(null);
  }
}
