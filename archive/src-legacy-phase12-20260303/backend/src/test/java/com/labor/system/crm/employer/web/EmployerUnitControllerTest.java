package com.labor.system.crm.employer.web;

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
import com.labor.system.crm.employer.service.EmployerUnitService;
import com.labor.system.crm.employer.web.dto.EmployerUnitPageResponse;
import com.labor.system.crm.employer.web.dto.EmployerUnitResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EmployerUnitController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class EmployerUnitControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EmployerUnitService employerUnitService;

  @Test
  void createEmployerUnitShouldReturnSuccess() throws Exception {
    when(employerUnitService.createEmployerUnit(org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new EmployerUnitResponse(
                1L,
                "UNIT-001",
                10L,
                "华东酒店集团",
                "A",
                "上海浦东",
                "{\"taxNo\":\"123\"}",
                false,
                "2026-02-26T22:50:00",
                "2026-02-26T22:50:00"));

    mockMvc
        .perform(
            post("/api/admin/v1/crm/employer-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "unitCode":"UNIT-001",
                      "leadId":10,
                      "unitName":"华东酒店集团",
                      "customerLevel":"A",
                      "address":"上海浦东",
                      "invoiceInfo":"{\\"taxNo\\":\\"123\\"}",
                      "outsource":false
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.unitCode").value("UNIT-001"));
  }

  @Test
  void getEmployerUnitShouldReturnSuccess() throws Exception {
    when(employerUnitService.getEmployerUnit(eq(1L)))
        .thenReturn(
            new EmployerUnitResponse(
                1L,
                "UNIT-001",
                10L,
                "华东酒店集团",
                "A",
                "上海浦东",
                null,
                false,
                "2026-02-26T22:50:00",
                "2026-02-26T22:50:00"));

    mockMvc
        .perform(get("/api/admin/v1/crm/employer-units/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  void listEmployerUnitsShouldReturnPageData() throws Exception {
    when(employerUnitService.listEmployerUnits(eq("华东"), eq("A"), eq(1), eq(10)))
        .thenReturn(
            new EmployerUnitPageResponse(
                List.of(
                    new EmployerUnitResponse(
                        1L,
                        "UNIT-001",
                        10L,
                        "华东酒店集团",
                        "A",
                        "上海浦东",
                        null,
                        false,
                        "2026-02-26T22:50:00",
                        "2026-02-26T22:50:00")),
                1L,
                1,
                10));

    mockMvc
        .perform(
            get("/api/admin/v1/crm/employer-units")
                .param("keyword", "华东")
                .param("customerLevel", "A")
                .param("page", "1")
                .param("pageSize", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1));
  }

  @Test
  void updateEmployerUnitShouldReturnSuccess() throws Exception {
    when(employerUnitService.updateEmployerUnit(eq(1L), org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new EmployerUnitResponse(
                1L,
                "UNIT-001",
                10L,
                "华东酒店集团-更新",
                "B",
                "上海浦东新区",
                null,
                true,
                "2026-02-26T22:50:00",
                "2026-02-26T22:55:00"));

    mockMvc
        .perform(
            put("/api/admin/v1/crm/employer-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "unitCode":"UNIT-001",
                      "leadId":10,
                      "unitName":"华东酒店集团-更新",
                      "customerLevel":"B",
                      "address":"上海浦东新区",
                      "outsource":true
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.customerLevel").value("B"));
  }

  @Test
  void deactivateEmployerUnitShouldReturnInactiveStatus() throws Exception {
    when(employerUnitService.deactivateEmployerUnit(eq(1L)))
        .thenReturn(
            new EmployerUnitResponse(
                1L,
                "UNIT-001",
                10L,
                "华东酒店集团",
                "INACTIVE",
                "上海浦东",
                null,
                false,
                "2026-02-26T22:50:00",
                "2026-02-26T22:58:00"));

    mockMvc
        .perform(put("/api/admin/v1/crm/employer-units/1/deactivate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.customerLevel").value("INACTIVE"));
  }

  @Test
  void shouldRejectInvalidPayload() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/v1/crm/employer-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }

  @Test
  void shouldReturnBadRequestWhenServiceThrows() throws Exception {
    when(employerUnitService.getEmployerUnit(eq(999L))).thenThrow(AppException.badRequest("用工单位不存在"));

    mockMvc
        .perform(get("/api/admin/v1/crm/employer-units/999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-400"));
  }
}
