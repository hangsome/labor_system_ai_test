package com.labor.system.crm.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.labor.system.common.api.AppException;
import com.labor.system.common.api.GlobalExceptionHandler;
import com.labor.system.config.SecurityConfig;
import com.labor.system.crm.lead.service.LeadFlowService;
import com.labor.system.crm.lead.service.LeadService;
import com.labor.system.crm.lead.web.LeadController;
import com.labor.system.crm.lead.web.dto.LeadFollowUpResponse;
import com.labor.system.crm.lead.web.dto.LeadResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LeadController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class LeadApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LeadService leadService;
  @MockBean private LeadFlowService leadFlowService;

  @Test
  void leadCreateTransitionFollowUpShouldWorkAsIntegratedFlow() throws Exception {
    when(leadService.createLead(any()))
        .thenReturn(
            new LeadResponse(
                1L,
                "LEAD-001",
                "酒店外包项目",
                "张三",
                "cipher",
                "HOTEL",
                1001L,
                "NEW",
                LocalDate.parse("2026-02-26"),
                "UNPAID",
                "2026-02-26T22:40:00",
                "2026-02-26T22:40:00"));
    when(leadFlowService.transitionStatus(eq(1L), any()))
        .thenReturn(
            new LeadResponse(
                1L,
                "LEAD-001",
                "酒店外包项目",
                "张三",
                "cipher",
                "HOTEL",
                1001L,
                "FOLLOWING",
                LocalDate.parse("2026-02-26"),
                "UNPAID",
                "2026-02-26T22:40:00",
                "2026-02-26T22:42:00"));
    doNothing().when(leadFlowService).createFollowUp(eq(1L), any());
    when(leadFlowService.listFollowUps(eq(1L)))
        .thenReturn(
            List.of(
                new LeadFollowUpResponse(
                    100L,
                    "FOLLOW_UP",
                    "客户确认下周沟通",
                    "FOLLOWING",
                    null,
                    null,
                    "2026-03-01T10:00:00",
                    0L,
                    "2026-02-26T22:43:00")));

    mockMvc
        .perform(
            post("/api/admin/v1/crm/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "leadCode":"LEAD-001",
                      "projectName":"酒店外包项目",
                      "contactName":"张三",
                      "industryType":"HOTEL",
                      "bizOwnerId":1001,
                      "cooperationStatus":"NEW",
                      "tenderAt":"2026-02-26",
                      "depositStatus":"UNPAID"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.leadCode").value("LEAD-001"));

    mockMvc
        .perform(
            put("/api/admin/v1/crm/leads/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toStatus\":\"FOLLOWING\",\"comment\":\"首次跟进\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.cooperationStatus").value("FOLLOWING"));

    mockMvc
        .perform(
            post("/api/admin/v1/crm/leads/1/follow-ups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"客户确认下周沟通\",\"nextContactAt\":\"2026-03-01T10:00:00\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    mockMvc
        .perform(get("/api/admin/v1/crm/leads/1/follow-ups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data[0].action").value("FOLLOW_UP"))
        .andExpect(jsonPath("$.data[0].content").value("客户确认下周沟通"));
  }

  @Test
  void shouldRejectIllegalTransition() throws Exception {
    when(leadFlowService.transitionStatus(eq(1L), any())).thenThrow(AppException.badRequest("非法状态流转"));

    mockMvc
        .perform(
            put("/api/admin/v1/crm/leads/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toStatus\":\"FOLLOWING\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
