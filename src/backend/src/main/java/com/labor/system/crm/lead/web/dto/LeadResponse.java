package com.labor.system.crm.lead.web.dto;

import java.time.LocalDate;

public record LeadResponse(
    Long id,
    String leadCode,
    String projectName,
    String contactName,
    String contactPhoneCipher,
    String industryType,
    Long bizOwnerId,
    String cooperationStatus,
    LocalDate tenderAt,
    String depositStatus,
    String createdAt,
    String updatedAt) {}
