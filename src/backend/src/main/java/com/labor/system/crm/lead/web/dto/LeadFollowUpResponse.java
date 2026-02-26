package com.labor.system.crm.lead.web.dto;

public record LeadFollowUpResponse(
    Long id,
    String action,
    String content,
    String status,
    String statusFrom,
    String statusTo,
    String nextContactAt,
    Long operatorId,
    String createdAt) {}
