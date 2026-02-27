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
import com.labor.system.contract.service.SettlementRuleService;
import com.labor.system.contract.web.dto.SettlementRuleResponse;
import com.labor.system.contract.web.dto.SettlementRuleVersionListResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SettlementRuleController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SettlementRuleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SettlementRuleService settlementRuleService;

  @Test
  void createRuleShouldReturnSuccess() throws Exception {
    when(settlementRuleService.createRule(org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new SettlementRuleResponse(
                1L,
                11L,
                "HOURLY",
                1,
                LocalDate.parse("2026-03-01"),
                "{\"unitPrice\":35}",
                "DRAFT",
                null,
                null,
                "2026-02-27T00:10:00"));

    mockMvc
        .perform(
            post("/api/admin/v1/contracts/settlement-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "contractId":11,
                      "ruleType":"HOURLY",
                      "versionNo":1,
                      "effectiveFrom":"2026-03-01",
                      "rulePayload":"{\\"unitPrice\\":35}"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("DRAFT"));
  }

  @Test
  void publishRuleShouldReturnPublishedStatus() throws Exception {
    when(settlementRuleService.publishRule(eq(1L)))
        .thenReturn(
            new SettlementRuleResponse(
                1L,
                11L,
                "HOURLY",
                1,
                LocalDate.parse("2026-03-01"),
                "{\"unitPrice\":35}",
                "PUBLISHED",
                "2026-02-27T00:12:00",
                null,
                "2026-02-27T00:10:00"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/settlement-rules/1/publish"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
  }

  @Test
  void deactivateRuleShouldReturnDisabledStatus() throws Exception {
    when(settlementRuleService.deactivateRule(eq(1L)))
        .thenReturn(
            new SettlementRuleResponse(
                1L,
                11L,
                "HOURLY",
                1,
                LocalDate.parse("2026-03-01"),
                "{\"unitPrice\":35}",
                "DISABLED",
                "2026-02-27T00:12:00",
                "2026-02-27T00:20:00",
                "2026-02-27T00:10:00"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/settlement-rules/1/deactivate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.status").value("DISABLED"));
  }

  @Test
  void listVersionsShouldReturnRecords() throws Exception {
    when(settlementRuleService.listVersions(eq(11L)))
        .thenReturn(
            new SettlementRuleVersionListResponse(
                List.of(
                    new SettlementRuleResponse(
                        2L,
                        11L,
                        "HOURLY",
                        2,
                        LocalDate.parse("2026-04-01"),
                        "{\"unitPrice\":36}",
                        "DRAFT",
                        null,
                        null,
                        "2026-02-27T00:30:00"),
                    new SettlementRuleResponse(
                        1L,
                        11L,
                        "HOURLY",
                        1,
                        LocalDate.parse("2026-03-01"),
                        "{\"unitPrice\":35}",
                        "PUBLISHED",
                        "2026-02-27T00:12:00",
                        null,
                        "2026-02-27T00:10:00")),
                2L));

    mockMvc
        .perform(get("/api/admin/v1/contracts/11/settlement-rules/versions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(2))
        .andExpect(jsonPath("$.data.records[0].versionNo").value(2));
  }

  @Test
  void getActiveRuleShouldReturnPublishedVersion() throws Exception {
    when(settlementRuleService.getActiveRule(eq(11L), eq(LocalDate.parse("2026-03-15"))))
        .thenReturn(
            new SettlementRuleResponse(
                1L,
                11L,
                "HOURLY",
                1,
                LocalDate.parse("2026-03-01"),
                "{\"unitPrice\":35}",
                "PUBLISHED",
                "2026-02-27T00:12:00",
                null,
                "2026-02-27T00:10:00"));

    mockMvc
        .perform(
            get("/api/admin/v1/contracts/11/settlement-rules/active").param("onDate", "2026-03-15"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.versionNo").value(1));
  }

  @Test
  void shouldRejectInvalidPayload() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/v1/contracts/settlement-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void shouldReturnBadRequestWhenServiceThrows() throws Exception {
    when(settlementRuleService.publishRule(eq(2L))).thenThrow(AppException.badRequest("结算规则不存在"));

    mockMvc
        .perform(put("/api/admin/v1/contracts/settlement-rules/2/publish"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
