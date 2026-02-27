package com.labor.system.crm.employer.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.crm.employer.service.EmployerUnitService;
import com.labor.system.crm.employer.web.dto.CreateEmployerUnitRequest;
import com.labor.system.crm.employer.web.dto.EmployerUnitPageResponse;
import com.labor.system.crm.employer.web.dto.EmployerUnitResponse;
import com.labor.system.crm.employer.web.dto.UpdateEmployerUnitRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/admin/v1/crm/employer-units")
public class EmployerUnitController {

  private final EmployerUnitService employerUnitService;

  public EmployerUnitController(EmployerUnitService employerUnitService) {
    this.employerUnitService = employerUnitService;
  }

  @PostMapping
  public ApiResponse<EmployerUnitResponse> createEmployerUnit(
      @Valid @RequestBody CreateEmployerUnitRequest request) {
    return ApiResponse.success(employerUnitService.createEmployerUnit(request));
  }

  @GetMapping("/{employerUnitId}")
  public ApiResponse<EmployerUnitResponse> getEmployerUnit(
      @PathVariable("employerUnitId") Long employerUnitId) {
    return ApiResponse.success(employerUnitService.getEmployerUnit(employerUnitId));
  }

  @GetMapping
  public ApiResponse<EmployerUnitPageResponse> listEmployerUnits(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "customerLevel", required = false) String customerLevel,
      @RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "page 必须大于等于 1")
          int page,
      @RequestParam(value = "pageSize", defaultValue = "20")
          @Min(value = 1, message = "pageSize 必须大于等于 1")
          @Max(value = 100, message = "pageSize 不能超过 100")
          int pageSize) {
    return ApiResponse.success(employerUnitService.listEmployerUnits(keyword, customerLevel, page, pageSize));
  }

  @PutMapping("/{employerUnitId}")
  public ApiResponse<EmployerUnitResponse> updateEmployerUnit(
      @PathVariable("employerUnitId") Long employerUnitId,
      @Valid @RequestBody UpdateEmployerUnitRequest request) {
    return ApiResponse.success(employerUnitService.updateEmployerUnit(employerUnitId, request));
  }

  @PutMapping("/{employerUnitId}/deactivate")
  public ApiResponse<EmployerUnitResponse> deactivateEmployerUnit(
      @PathVariable("employerUnitId") Long employerUnitId) {
    return ApiResponse.success(employerUnitService.deactivateEmployerUnit(employerUnitId));
  }
}
