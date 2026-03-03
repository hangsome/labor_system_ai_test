package com.labor.system.contract.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.contract.service.LaborContractService;
import com.labor.system.contract.web.dto.CreateLaborContractRequest;
import com.labor.system.contract.web.dto.LaborContractResponse;
import com.labor.system.contract.web.dto.RenewLaborContractRequest;
import com.labor.system.contract.web.dto.TerminateLaborContractRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/contracts/labor-contracts")
public class LaborContractController {

  private final LaborContractService laborContractService;

  public LaborContractController(LaborContractService laborContractService) {
    this.laborContractService = laborContractService;
  }

  @PostMapping
  public ApiResponse<LaborContractResponse> createContract(
      @Valid @RequestBody CreateLaborContractRequest request) {
    return ApiResponse.success(laborContractService.createContract(request));
  }

  @GetMapping("/{contractId}")
  public ApiResponse<LaborContractResponse> getContract(@PathVariable("contractId") Long contractId) {
    return ApiResponse.success(laborContractService.getContract(contractId));
  }

  @PutMapping("/{contractId}/sign")
  public ApiResponse<LaborContractResponse> signContract(@PathVariable("contractId") Long contractId) {
    return ApiResponse.success(laborContractService.signContract(contractId));
  }

  @PutMapping("/{contractId}/renew")
  public ApiResponse<LaborContractResponse> renewContract(
      @PathVariable("contractId") Long contractId,
      @Valid @RequestBody RenewLaborContractRequest request) {
    return ApiResponse.success(laborContractService.renewContract(contractId, request));
  }

  @PutMapping("/{contractId}/terminate")
  public ApiResponse<LaborContractResponse> terminateContract(
      @PathVariable("contractId") Long contractId,
      @Valid @RequestBody TerminateLaborContractRequest request) {
    return ApiResponse.success(laborContractService.terminateContract(contractId, request));
  }
}
