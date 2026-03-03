package com.labor.system.crm.employer.web.dto;

public record EmployerUnitResponse(
    Long id,
    String unitCode,
    Long leadId,
    String unitName,
    String customerLevel,
    String address,
    String invoiceInfo,
    Boolean outsource,
    String createdAt,
    String updatedAt) {}
