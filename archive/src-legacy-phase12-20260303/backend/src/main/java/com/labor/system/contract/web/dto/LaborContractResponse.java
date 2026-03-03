package com.labor.system.contract.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaborContractResponse(
    Long id,
    String contractNo,
    Long employerUnitId,
    String contractName,
    String contractType,
    LocalDate startDate,
    LocalDate endDate,
    String settlementCycle,
    String status,
    BigDecimal taxRate,
    String createdAt,
    String updatedAt) {}
