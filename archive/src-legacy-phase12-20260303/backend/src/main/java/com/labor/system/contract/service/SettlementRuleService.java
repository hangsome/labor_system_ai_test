package com.labor.system.contract.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.labor.system.common.api.AppException;
import com.labor.system.contract.domain.SettlementRule;
import com.labor.system.contract.repository.LaborContractRepository;
import com.labor.system.contract.repository.SettlementRuleRepository;
import com.labor.system.contract.web.dto.CreateSettlementRuleRequest;
import com.labor.system.contract.web.dto.SettlementRuleResponse;
import com.labor.system.contract.web.dto.SettlementRuleVersionListResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementRuleService {

  private static final String STATUS_DRAFT = "DRAFT";
  private static final String STATUS_PUBLISHED = "PUBLISHED";
  private static final String STATUS_DISABLED = "DISABLED";

  private final SettlementRuleRepository settlementRuleRepository;
  private final LaborContractRepository laborContractRepository;
  private final ObjectMapper objectMapper;

  public SettlementRuleService(
      SettlementRuleRepository settlementRuleRepository,
      LaborContractRepository laborContractRepository,
      ObjectMapper objectMapper) {
    this.settlementRuleRepository = settlementRuleRepository;
    this.laborContractRepository = laborContractRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public SettlementRuleResponse createRule(CreateSettlementRuleRequest request) {
    Long contractId = request.getContractId();
    if (!laborContractRepository.existsById(contractId)) {
      throw AppException.badRequest("合同不存在");
    }
    if (settlementRuleRepository.existsByContractIdAndVersionNo(contractId, request.getVersionNo())) {
      throw AppException.badRequest("同一合同下 versionNo 已存在");
    }

    JsonNode contentNode = parseJsonPayload(request.getRulePayload());
    SettlementRule settlementRule = new SettlementRule();
    settlementRule.setContractId(contractId);
    settlementRule.setRuleType(normalizeRequiredText(request.getRuleType(), "ruleType").toUpperCase(Locale.ROOT));
    settlementRule.setVersionNo(request.getVersionNo());
    settlementRule.setEffectiveFrom(request.getEffectiveFrom());
    settlementRule.setRulePayload(toWrappedPayload(contentNode, STATUS_DRAFT, null, null));
    SettlementRule saved = settlementRuleRepository.save(settlementRule);
    return toResponse(saved);
  }

  @Transactional
  public SettlementRuleResponse publishRule(Long ruleId) {
    SettlementRule settlementRule = findRuleOrThrow(ruleId);
    RulePayloadState state = parseRulePayloadState(settlementRule.getRulePayload());
    if (STATUS_DISABLED.equals(state.status())) {
      throw AppException.badRequest("停用版本不允许发布");
    }
    if (STATUS_PUBLISHED.equals(state.status())) {
      return toResponse(settlementRule, state);
    }
    ensureUniquePublishedEffectiveDate(settlementRule);
    settlementRule.setRulePayload(
        toWrappedPayload(state.content(), STATUS_PUBLISHED, LocalDateTime.now(), state.deactivatedAt()));
    SettlementRule updated = settlementRuleRepository.save(settlementRule);
    return toResponse(updated);
  }

  @Transactional
  public SettlementRuleResponse deactivateRule(Long ruleId) {
    SettlementRule settlementRule = findRuleOrThrow(ruleId);
    RulePayloadState state = parseRulePayloadState(settlementRule.getRulePayload());
    if (!STATUS_PUBLISHED.equals(state.status())) {
      throw AppException.badRequest("仅已发布版本可停用");
    }
    settlementRule.setRulePayload(
        toWrappedPayload(state.content(), STATUS_DISABLED, state.publishedAt(), LocalDateTime.now()));
    SettlementRule updated = settlementRuleRepository.save(settlementRule);
    return toResponse(updated);
  }

  public SettlementRuleVersionListResponse listVersions(Long contractId) {
    if (!laborContractRepository.existsById(contractId)) {
      throw AppException.badRequest("合同不存在");
    }
    List<SettlementRuleResponse> records =
        settlementRuleRepository.findByContractIdOrderByVersionNoDesc(contractId).stream()
            .map(this::toResponse)
            .toList();
    return new SettlementRuleVersionListResponse(records, (long) records.size());
  }

  public SettlementRuleResponse getActiveRule(Long contractId, LocalDate onDate) {
    if (!laborContractRepository.existsById(contractId)) {
      throw AppException.badRequest("合同不存在");
    }
    LocalDate effectiveDate = onDate == null ? LocalDate.now() : onDate;

    return settlementRuleRepository.findByContractIdOrderByVersionNoDesc(contractId).stream()
        .map(rule -> new RuleLookup(rule, parseRulePayloadState(rule.getRulePayload())))
        .filter(lookup -> !lookup.rule().getEffectiveFrom().isAfter(effectiveDate))
        .filter(lookup -> STATUS_PUBLISHED.equals(lookup.state().status()))
        .findFirst()
        .map(lookup -> toResponse(lookup.rule(), lookup.state()))
        .orElseThrow(() -> AppException.badRequest("未找到有效规则版本"));
  }

  private SettlementRule findRuleOrThrow(Long ruleId) {
    return settlementRuleRepository
        .findById(ruleId)
        .orElseThrow(() -> AppException.badRequest("结算规则不存在"));
  }

  private void ensureUniquePublishedEffectiveDate(SettlementRule currentRule) {
    List<SettlementRule> sameDateRules =
        settlementRuleRepository.findByContractIdAndEffectiveFrom(
            currentRule.getContractId(), currentRule.getEffectiveFrom());
    boolean hasConflict =
        sameDateRules.stream()
            .filter(rule -> !rule.getId().equals(currentRule.getId()))
            .map(rule -> parseRulePayloadState(rule.getRulePayload()))
            .anyMatch(state -> STATUS_PUBLISHED.equals(state.status()));
    if (hasConflict) {
      throw AppException.badRequest("同一合同同一生效日仅允许一个已发布规则");
    }
  }

  private RulePayloadState parseRulePayloadState(String payloadText) {
    JsonNode rootNode = parseJsonPayload(payloadText);
    JsonNode contentNode = rootNode.path("content");
    JsonNode lifecycleNode = rootNode.path("lifecycle");

    JsonNode normalizedContent = contentNode.isMissingNode() || contentNode.isNull() ? rootNode : contentNode;
    String status = STATUS_DRAFT;
    LocalDateTime publishedAt = null;
    LocalDateTime deactivatedAt = null;
    if (lifecycleNode.isObject()) {
      status = normalizeLifecycleStatus(lifecycleNode.path("status").asText(status));
      publishedAt = parseLocalDateTime(lifecycleNode.path("publishedAt").asText(null));
      deactivatedAt = parseLocalDateTime(lifecycleNode.path("deactivatedAt").asText(null));
    }
    return new RulePayloadState(normalizedContent, status, publishedAt, deactivatedAt);
  }

  private String normalizeLifecycleStatus(String rawStatus) {
    if (rawStatus == null || rawStatus.trim().isEmpty()) {
      return STATUS_DRAFT;
    }
    String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
    if (STATUS_PUBLISHED.equals(normalized) || STATUS_DISABLED.equals(normalized)) {
      return normalized;
    }
    return STATUS_DRAFT;
  }

  private LocalDateTime parseLocalDateTime(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(text);
    } catch (Exception ignore) {
      return null;
    }
  }

  private SettlementRuleResponse toResponse(SettlementRule settlementRule) {
    return toResponse(settlementRule, parseRulePayloadState(settlementRule.getRulePayload()));
  }

  private SettlementRuleResponse toResponse(SettlementRule settlementRule, RulePayloadState state) {
    return new SettlementRuleResponse(
        settlementRule.getId(),
        settlementRule.getContractId(),
        settlementRule.getRuleType(),
        settlementRule.getVersionNo(),
        settlementRule.getEffectiveFrom(),
        toJsonText(state.content()),
        state.status(),
        toDateTimeText(state.publishedAt()),
        toDateTimeText(state.deactivatedAt()),
        toDateTimeText(settlementRule.getCreatedAt()));
  }

  private JsonNode parseJsonPayload(String payloadText) {
    String value = normalizeRequiredText(payloadText, "rulePayload");
    try {
      JsonNode jsonNode = objectMapper.readTree(value);
      if (jsonNode == null || jsonNode.isNull()) {
        throw AppException.badRequest("rulePayload 必须为合法 JSON");
      }
      return jsonNode;
    } catch (JsonProcessingException ex) {
      throw AppException.badRequest("rulePayload 必须为合法 JSON");
    }
  }

  private String toWrappedPayload(
      JsonNode contentNode, String status, LocalDateTime publishedAt, LocalDateTime deactivatedAt) {
    ObjectNode rootNode = objectMapper.createObjectNode();
    rootNode.set("content", contentNode);

    ObjectNode lifecycleNode = rootNode.putObject("lifecycle");
    lifecycleNode.put("status", normalizeLifecycleStatus(status));
    if (publishedAt == null) {
      lifecycleNode.putNull("publishedAt");
    } else {
      lifecycleNode.put("publishedAt", publishedAt.toString());
    }
    if (deactivatedAt == null) {
      lifecycleNode.putNull("deactivatedAt");
    } else {
      lifecycleNode.put("deactivatedAt", deactivatedAt.toString());
    }
    return toJsonText(rootNode);
  }

  private String normalizeRequiredText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw AppException.badRequest(fieldName + " 不能为空");
    }
    return value.trim();
  }

  private String toDateTimeText(LocalDateTime value) {
    return value == null ? null : value.toString();
  }

  private String toJsonText(JsonNode node) {
    try {
      return objectMapper.writeValueAsString(node);
    } catch (JsonProcessingException ex) {
      throw AppException.badRequest("rulePayload 序列化失败");
    }
  }

  private record RuleLookup(SettlementRule rule, RulePayloadState state) {}

  private record RulePayloadState(
      JsonNode content, String status, LocalDateTime publishedAt, LocalDateTime deactivatedAt) {}
}
