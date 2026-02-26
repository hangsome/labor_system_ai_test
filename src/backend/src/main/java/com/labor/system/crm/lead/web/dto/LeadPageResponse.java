package com.labor.system.crm.lead.web.dto;

import java.util.List;

public record LeadPageResponse(List<LeadResponse> records, long total, int page, int pageSize) {}
