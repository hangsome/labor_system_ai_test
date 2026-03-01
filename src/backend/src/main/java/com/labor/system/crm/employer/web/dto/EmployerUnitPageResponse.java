package com.labor.system.crm.employer.web.dto;

import java.util.List;

public record EmployerUnitPageResponse(
    List<EmployerUnitResponse> records, long total, int page, int pageSize) {}
