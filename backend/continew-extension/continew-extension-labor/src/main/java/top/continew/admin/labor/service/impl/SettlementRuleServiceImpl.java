/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.labor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.labor.mapper.ContractMapper;
import top.continew.admin.labor.mapper.SettlementRuleMapper;
import top.continew.admin.labor.model.entity.ContractDO;
import top.continew.admin.labor.model.entity.SettlementRuleDO;
import top.continew.admin.labor.model.query.SettlementRuleQuery;
import top.continew.admin.labor.model.req.SettlementRuleReq;
import top.continew.admin.labor.model.resp.SettlementRuleResp;
import top.continew.admin.labor.service.SettlementRuleService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SettlementRuleServiceImpl extends BaseServiceImpl<SettlementRuleMapper, SettlementRuleDO, SettlementRuleResp, SettlementRuleResp, SettlementRuleQuery, SettlementRuleReq> implements SettlementRuleService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_DISABLED = "DISABLED";

    private final ContractMapper contractMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void beforeCreate(SettlementRuleReq req) {
        this.checkContractExists(req.getContractId());
        this.checkVersionRepeat(req.getContractId(), req.getVersionNo(), null);
        this.validateJsonPayload(req.getRulePayload());
        req.setRuleType(StrUtil.trim(req.getRuleType()).toUpperCase(Locale.ROOT));
    }

    @Override
    public Long create(SettlementRuleReq req) {
        req.setStatus(STATUS_DRAFT);
        return super.create(req);
    }

    @Override
    public void beforeUpdate(SettlementRuleReq req, Long id) {
        SettlementRuleDO oldRule = super.getById(id);
        CheckUtils.throwIf(!STATUS_DRAFT.equals(oldRule.getStatus()), "仅草稿状态支持修改");
        this.checkContractExists(req.getContractId());
        this.checkVersionRepeat(req.getContractId(), req.getVersionNo(), id);
        this.validateJsonPayload(req.getRulePayload());
        req.setRuleType(StrUtil.trim(req.getRuleType()).toUpperCase(Locale.ROOT));
        req.setStatus(oldRule.getStatus());
    }

    @Override
    public SettlementRuleResp publish(Long id) {
        SettlementRuleDO rule = super.getById(id);
        CheckUtils.throwIf(STATUS_DISABLED.equals(rule.getStatus()), "停用版本不允许发布");
        if (STATUS_PUBLISHED.equals(rule.getStatus())) {
            return super.get(id);
        }
        this.checkPublishedConflict(rule.getContractId(), rule.getEffectiveFrom(), id);
        rule.setStatus(STATUS_PUBLISHED);
        rule.setPublishedAt(LocalDateTime.now());
        baseMapper.updateById(rule);
        return super.get(id);
    }

    @Override
    public SettlementRuleResp deactivate(Long id) {
        SettlementRuleDO rule = super.getById(id);
        CheckUtils.throwIf(!STATUS_PUBLISHED.equals(rule.getStatus()), "仅已发布版本可停用");
        rule.setStatus(STATUS_DISABLED);
        rule.setDeactivatedAt(LocalDateTime.now());
        baseMapper.updateById(rule);
        return super.get(id);
    }

    @Override
    public List<SettlementRuleResp> listVersion(Long contractId) {
        this.checkContractExists(contractId);
        return baseMapper.lambdaQuery()
            .eq(SettlementRuleDO::getContractId, contractId)
            .orderByDesc(SettlementRuleDO::getVersionNo)
            .list()
            .stream()
            .map(item -> BeanUtil.copyProperties(item, SettlementRuleResp.class))
            .toList();
    }

    @Override
    public SettlementRuleResp getActive(Long contractId, LocalDate onDate) {
        this.checkContractExists(contractId);
        LocalDate effectiveDate = onDate == null ? LocalDate.now() : onDate;
        List<SettlementRuleDO> list = baseMapper.lambdaQuery()
            .eq(SettlementRuleDO::getContractId, contractId)
            .eq(SettlementRuleDO::getStatus, STATUS_PUBLISHED)
            .le(SettlementRuleDO::getEffectiveFrom, effectiveDate)
            .orderByDesc(SettlementRuleDO::getEffectiveFrom)
            .orderByDesc(SettlementRuleDO::getVersionNo)
            .list();
        CheckUtils.throwIf(CollUtil.isEmpty(list), "未找到有效规则版本");
        return BeanUtil.copyProperties(list.get(0), SettlementRuleResp.class);
    }

    private void checkContractExists(Long contractId) {
        CheckUtils.throwIf(!contractMapper.lambdaQuery().eq(ContractDO::getId, contractId).exists(), "合同不存在");
    }

    private void checkVersionRepeat(Long contractId, Integer versionNo, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(SettlementRuleDO::getContractId, contractId)
            .eq(SettlementRuleDO::getVersionNo, versionNo)
            .ne(id != null, SettlementRuleDO::getId, id)
            .exists(), "同一合同下 versionNo 已存在");
    }

    private void checkPublishedConflict(Long contractId, LocalDate effectiveFrom, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(SettlementRuleDO::getContractId, contractId)
            .eq(SettlementRuleDO::getEffectiveFrom, effectiveFrom)
            .eq(SettlementRuleDO::getStatus, STATUS_PUBLISHED)
            .ne(id != null, SettlementRuleDO::getId, id)
            .exists(), "同一合同同一生效日仅允许一个已发布规则");
    }

    private void validateJsonPayload(String rulePayload) {
        try {
            objectMapper.readTree(rulePayload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("rulePayload 必须为合法 JSON");
        }
    }
}
