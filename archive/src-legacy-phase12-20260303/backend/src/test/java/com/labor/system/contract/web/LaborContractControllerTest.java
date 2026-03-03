package com.labor.system.contract.web;

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
class LaborContractControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LaborContractService laborContractService;

  @Test
  void createContractShouldReturnSuccess() throws Exception {
    when(laborContractService.createContract(org.mockito.ArgumentMatchers.any()))
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
                "2026-02-26T23:00:00",
                "2026-02-26T23:00:00"));

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
  }

  @Test
  void signContractShouldReturnSignedStatus() throws Exception {
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
                "2026-02-26T23:00:00",
                "2026-02-26T23:10:00"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/labor-contracts/1/sign"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("SIGNED"));
  }

  @Test
  void renewContractShouldReturnUpdatedEndDate() throws Exception {
    when(laborContractService.renewContract(eq(1L), org.mockito.ArgumentMatchers.any()))
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
                "2026-02-26T23:00:00",
                "2026-02-26T23:20:00"));

    mockMvc
        .perform(
            put("/api/admin/v1/contracts/labor-contracts/1/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newEndDate\":\"2027-06-01\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.endDate").value("2027-06-01"));
  }

  @Test
  void terminateContractShouldReturnTerminatedStatus() throws Exception {
    when(laborContractService.terminateContract(eq(1L), org.mockito.ArgumentMatchers.any()))
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
                "2026-02-26T23:00:00",
                "2026-02-26T23:30:00"));

    mockMvc
        .perform(
            put("/api/admin/v1/contracts/labor-contracts/1/terminate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"terminateDate\":\"2026-12-31\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("TERMINATED"));
  }

  @Test
  void getContractShouldReturnSuccess() throws Exception {
    when(laborContractService.getContract(eq(1L)))
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
                "2026-02-26T23:00:00",
                "2026-02-26T23:10:00"));

    mockMvc
        .perform(get("/api/admin/v1/contracts/labor-contracts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.contractNo").value("LC-001"));
  }

  @Test
  void shouldRejectInvalidPayload() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/v1/contracts/labor-contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void shouldReturnBadRequestWhenLifecycleInvalid() throws Exception {
    when(laborContractService.signContract(eq(2L))).thenThrow(AppException.badRequest("当前状态不允许签署"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/labor-contracts/2/sign"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
