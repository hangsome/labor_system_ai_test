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

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.labor.mapper.ContractMapper;
import top.continew.admin.labor.mapper.EmployerMapper;
import top.continew.admin.labor.model.entity.ContractDO;
import top.continew.admin.labor.model.entity.EmployerDO;
import top.continew.admin.labor.model.query.ContractQuery;
import top.continew.admin.labor.model.req.ContractRenewReq;
import top.continew.admin.labor.model.req.ContractReq;
import top.continew.admin.labor.model.req.ContractTerminateReq;
import top.continew.admin.labor.model.resp.ContractResp;
import top.continew.admin.labor.service.ContractService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl extends BaseServiceImpl<ContractMapper, ContractDO, ContractResp, ContractResp, ContractQuery, ContractReq> implements ContractService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_TERMINATED = "TERMINATED";
    private static final Set<String> ALLOWED_CONTRACT_TYPES = Set.of("A", "B");

    private final EmployerMapper employerMapper;

    @Override
    public void beforeCreate(ContractReq req) {
        this.checkContractNoRepeat(req.getContractNo(), null);
        this.checkEmployerExists(req.getEmployerUnitId());
        this.checkDateRange(req.getStartDate(), req.getEndDate());
        req.setContractType(normalizeContractType(req.getContractType()));
        req.setSettlementCycle(StrUtil.trim(req.getSettlementCycle()));
    }

    @Override
    public Long create(ContractReq req) {
        req.setStatus(STATUS_DRAFT);
        return super.create(req);
    }

    @Override
    public void beforeUpdate(ContractReq req, Long id) {
        ContractDO oldContract = super.getById(id);
        CheckUtils.throwIf(!STATUS_DRAFT.equals(oldContract.getStatus()), "仅草稿状态支持修改");
        this.checkContractNoRepeat(req.getContractNo(), id);
        this.checkEmployerExists(req.getEmployerUnitId());
        this.checkDateRange(req.getStartDate(), req.getEndDate());
        req.setContractType(normalizeContractType(req.getContractType()));
        req.setSettlementCycle(StrUtil.trim(req.getSettlementCycle()));
        req.setStatus(oldContract.getStatus());
    }

    @Override
    public ContractResp sign(Long id) {
        ContractDO contract = super.getById(id);
        CheckUtils.throwIf(!STATUS_DRAFT.equals(contract.getStatus()), "当前状态不允许签署");
        contract.setStatus(STATUS_SIGNED);
        baseMapper.updateById(contract);
        return super.get(id);
    }

    @Override
    public ContractResp renew(Long id, ContractRenewReq req) {
        ContractDO contract = super.getById(id);
        CheckUtils.throwIf(!STATUS_SIGNED.equals(contract.getStatus()), "当前状态不允许续签");
        CheckUtils.throwIf(req.getNewEndDate() == null || !req.getNewEndDate()
            .isAfter(contract.getEndDate()), "newEndDate 必须晚于当前 endDate");
        contract.setEndDate(req.getNewEndDate());
        baseMapper.updateById(contract);
        return super.get(id);
    }

    @Override
    public ContractResp terminate(Long id, ContractTerminateReq req) {
        ContractDO contract = super.getById(id);
        CheckUtils.throwIf(!STATUS_SIGNED.equals(contract.getStatus()), "当前状态不允许终止");
        CheckUtils.throwIf(req.getTerminateDate() == null, "terminateDate 不能为空");
        CheckUtils.throwIf(req.getTerminateDate().isBefore(contract.getStartDate()), "terminateDate 不能早于 startDate");
        contract.setEndDate(req.getTerminateDate());
        contract.setStatus(STATUS_TERMINATED);
        baseMapper.updateById(contract);
        return super.get(id);
    }

    private void checkContractNoRepeat(String contractNo, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(ContractDO::getContractNo, StrUtil.trim(contractNo))
            .ne(id != null, ContractDO::getId, id)
            .exists(), "合同编号 [{}] 已存在", contractNo);
    }

    private void checkEmployerExists(Long employerUnitId) {
        CheckUtils.throwIf(!employerMapper.lambdaQuery().eq(EmployerDO::getId, employerUnitId).exists(), "用工单位不存在");
    }

    private void checkDateRange(LocalDate startDate, LocalDate endDate) {
        CheckUtils.throwIf(startDate == null || endDate == null, "startDate/endDate 不能为空");
        CheckUtils.throwIf(!endDate.isAfter(startDate), "endDate 必须晚于 startDate");
    }

    private String normalizeContractType(String contractType) {
        String normalized = StrUtil.trim(contractType).toUpperCase(Locale.ROOT);
        CheckUtils.throwIf(!ALLOWED_CONTRACT_TYPES.contains(normalized), "contractType 仅支持 A/B");
        return normalized;
    }
}
