package com.labor.system.contract.web.dto;

import java.time.LocalDate;

public record SettlementRuleResponse(
    Long id,
    Long contractId,
    String ruleType,
    Integer versionNo,
    LocalDate effectiveFrom,
    String rulePayload,
    String status,
    String publishedAt,
    String deactivatedAt,
    String createdAt) {}
