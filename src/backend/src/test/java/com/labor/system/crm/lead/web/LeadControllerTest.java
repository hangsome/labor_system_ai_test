package com.labor.system.crm.lead.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.labor.system.crm.lead.web.dto.LeadPageResponse;
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
class LeadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LeadService leadService;
  @MockBean private LeadFlowService leadFlowService;

  @Test
  void createLeadShouldReturnSuccess() throws Exception {
    when(leadService.createLead(org.mockito.ArgumentMatchers.any()))
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
                "2026-02-26T22:20:00",
                "2026-02-26T22:20:00"));

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
  }

  @Test
  void createLeadShouldReturnBadRequestWhenBodyInvalid() throws Exception {
    mockMvc
        .perform(post("/api/admin/v1/crm/leads").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void getLeadShouldReturnSuccess() throws Exception {
    when(leadService.getLead(eq(1L)))
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
                null,
                "UNPAID",
                "2026-02-26T22:20:00",
                "2026-02-26T22:20:00"));

    mockMvc
        .perform(get("/api/admin/v1/crm/leads/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  void getLeadShouldReturnBadRequestWhenLeadMissing() throws Exception {
    when(leadService.getLead(eq(404L))).thenThrow(AppException.badRequest("线索不存在"));

    mockMvc
        .perform(get("/api/admin/v1/crm/leads/404"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void listLeadsShouldReturnPageData() throws Exception {
    when(leadService.listLeads(eq("酒店"), eq("NEW"), eq(1), eq(10)))
        .thenReturn(
            new LeadPageResponse(
                List.of(
                    new LeadResponse(
                        1L,
                        "LEAD-001",
                        "酒店外包项目",
                        "张三",
                        "cipher",
                        "HOTEL",
                        1001L,
                        "NEW",
                        null,
                        "UNPAID",
                        "2026-02-26T22:20:00",
                        "2026-02-26T22:20:00")),
                1L,
                1,
                10));

    mockMvc
        .perform(get("/api/admin/v1/crm/leads").param("keyword", "酒店").param("status", "NEW").param("page", "1")
            .param("pageSize", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].leadCode").value("LEAD-001"));
  }

  @Test
  void updateLeadShouldReturnSuccess() throws Exception {
    when(leadService.updateLead(eq(1L), org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new LeadResponse(
                1L,
                "LEAD-001",
                "酒店外包项目-更新",
                "李四",
                "cipher",
                "HOTEL",
                1002L,
                "FOLLOWING",
                null,
                "PAID",
                "2026-02-26T22:20:00",
                "2026-02-26T22:30:00"));

    mockMvc
        .perform(
            put("/api/admin/v1/crm/leads/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "leadCode":"LEAD-001",
                      "projectName":"酒店外包项目-更新",
                      "contactName":"李四",
                      "industryType":"HOTEL",
                      "bizOwnerId":1002,
                      "cooperationStatus":"FOLLOWING",
                      "depositStatus":"PAID"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.projectName").value("酒店外包项目-更新"));
  }

  @Test
  void deleteLeadShouldReturnSuccess() throws Exception {
    doNothing().when(leadService).deleteLead(eq(1L));

    mockMvc
        .perform(delete("/api/admin/v1/crm/leads/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void transitionStatusShouldReturnSuccess() throws Exception {
    when(leadFlowService.transitionStatus(eq(1L), org.mockito.ArgumentMatchers.any()))
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
                null,
                "UNPAID",
                "2026-02-26T22:20:00",
                "2026-02-26T22:30:00"));

    mockMvc
        .perform(
            put("/api/admin/v1/crm/leads/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toStatus\":\"FOLLOWING\",\"comment\":\"首次跟进\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.cooperationStatus").value("FOLLOWING"));
  }

  @Test
  void createFollowUpShouldReturnSuccess() throws Exception {
    doNothing().when(leadFlowService).createFollowUp(eq(1L), org.mockito.ArgumentMatchers.any());

    mockMvc
        .perform(
            post("/api/admin/v1/crm/leads/1/follow-ups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"客户确认下周沟通\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void listFollowUpsShouldReturnSuccess() throws Exception {
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
                    null,
                    0L,
                    "2026-02-26T22:30:00")));

    mockMvc
        .perform(get("/api/admin/v1/crm/leads/1/follow-ups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data[0].action").value("FOLLOW_UP"));
  }
}
