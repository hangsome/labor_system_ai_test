package com.labor.system.contract.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.common.api.AppException;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.contract.service.LaborContractService;
import com.labor.system.contract.web.LaborContractController;
import com.labor.system.contract.web.dto.LaborContractResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LaborContractController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ContractApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LaborContractService laborContractService;

  @Test
  void contractLifecycleShouldWorkAsIntegratedFlow() throws Exception {
    when(laborContractService.createContract(any()))
        .thenReturn(
            new LaborContractResponse(
                1L,
                "LC-001",
                1L,
                "酒店劳务外包合同",
                "A",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2027-03-01"),
                "MONTHLY",
                "DRAFT",
                new BigDecimal("0.0600"),
                "2026-02-27T06:30:00",
                "2026-02-27T06:30:00"));
    when(laborContractService.signContract(eq(1L)))
        .thenReturn(
            new LaborContractResponse(
                1L,
                "LC-001",
                1L,
                "酒店劳务外包合同",
                "A",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2027-03-01"),
                "MONTHLY",
                "SIGNED",
                new BigDecimal("0.0600"),
                "2026-02-27T06:30:00",
                "2026-02-27T06:31:00"));
    when(laborContractService.renewContract(eq(1L), any()))
        .thenReturn(
            new LaborContractResponse(
                1L,
                "LC-001",
                1L,
                "酒店劳务外包合同",
                "A",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2027-06-01"),
                "MONTHLY",
                "SIGNED",
                new BigDecimal("0.0600"),
                "2026-02-27T06:30:00",
                "2026-02-27T06:32:00"));
    when(laborContractService.terminateContract(eq(1L), any()))
        .thenReturn(
            new LaborContractResponse(
                1L,
                "LC-001",
                1L,
                "酒店劳务外包合同",
                "A",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2026-12-31"),
                "MONTHLY",
                "TERMINATED",
                new BigDecimal("0.0600"),
                "2026-02-27T06:30:00",
                "2026-02-27T06:33:00"));
    when(laborContractService.getContract(eq(1L)))
        .thenReturn(
            new LaborContractResponse(
                1L,
                "LC-001",
                1L,
                "酒店劳务外包合同",
                "A",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2026-12-31"),
                "MONTHLY",
                "TERMINATED",
                new BigDecimal("0.0600"),
                "2026-02-27T06:30:00",
                "2026-02-27T06:33:00"));

    mockMvc
        .perform(
            post("/api/admin/v1/contracts/labor-contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "contractNo":"LC-001",
                      "employerUnitId":1,
                      "contractName":"酒店劳务外包合同",
                      "contractType":"A",
                      "startDate":"2026-03-01",
                      "endDate":"2027-03-01",
                      "settlementCycle":"MONTHLY",
                      "taxRate":0.0600
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("DRAFT"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/labor-contracts/1/sign"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("SIGNED"));

    mockMvc
        .perform(
            put("/api/admin/v1/contracts/labor-contracts/1/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newEndDate\":\"2027-06-01\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.endDate").value("2027-06-01"));

    mockMvc
        .perform(
            put("/api/admin/v1/contracts/labor-contracts/1/terminate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"terminateDate\":\"2026-12-31\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("TERMINATED"));

    mockMvc
        .perform(get("/api/admin/v1/contracts/labor-contracts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.contractNo").value("LC-001"))
        .andExpect(jsonPath("$.data.status").value("TERMINATED"));
  }

  @Test
  void shouldRejectRenewWhenLifecycleInvalid() throws Exception {
    when(laborContractService.renewContract(eq(2L), any())).thenThrow(AppException.badRequest("当前状态不允许续签"));

    mockMvc
        .perform(
            put("/api/admin/v1/contracts/labor-contracts/2/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newEndDate\":\"2027-06-01\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
