package com.labor.system.crm.lead.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.crm.lead.service.LeadService;
import com.labor.system.crm.lead.web.dto.CreateLeadRequest;
import com.labor.system.crm.lead.web.dto.LeadPageResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import com.labor.system.crm.lead.web.dto.UpdateLeadRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

  public LeadController(LeadService leadService) {
    this.leadService = leadService;
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

  @DeleteMapping("/{leadId}")
  public ApiResponse<Void> deleteLead(@PathVariable("leadId") Long leadId) {
    leadService.deleteLead(leadId);
    return ApiResponse.success(null);
  }
}
