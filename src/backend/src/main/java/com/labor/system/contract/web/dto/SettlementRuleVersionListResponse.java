package com.labor.system.contract.web.dto;

import java.util.List;

public record SettlementRuleVersionListResponse(List<SettlementRuleResponse> records, Long total) {}
