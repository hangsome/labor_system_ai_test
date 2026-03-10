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
import top.continew.admin.labor.mapper.ContractAssignmentMapper;
import top.continew.admin.labor.mapper.ContractMapper;
import top.continew.admin.labor.mapper.EmployeeProfileMapper;
import top.continew.admin.labor.model.entity.ContractAssignmentDO;
import top.continew.admin.labor.model.entity.ContractDO;
import top.continew.admin.labor.model.entity.EmployeeProfileDO;
import top.continew.admin.labor.model.query.ContractAssignmentQuery;
import top.continew.admin.labor.model.req.ContractAssignmentReq;
import top.continew.admin.labor.model.resp.ContractAssignmentResp;
import top.continew.admin.labor.service.ContractAssignmentService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContractAssignmentServiceImpl extends BaseServiceImpl<ContractAssignmentMapper, ContractAssignmentDO, ContractAssignmentResp, ContractAssignmentResp, ContractAssignmentQuery, ContractAssignmentReq> implements ContractAssignmentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ENDED = "ENDED";
    private static final String EMPLOYEE_STATUS_OFFBOARDED = "OFFBOARDED";
    private static final String CONTRACT_STATUS_TERMINATED = "TERMINATED";

    private final ContractMapper contractMapper;
    private final EmployeeProfileMapper employeeProfileMapper;

    @Override
    public void beforeCreate(ContractAssignmentReq req) {
        this.validateRelation(req, null);
        req.setStatus(this.resolveStatus(req.getAssignedAt(), req.getUnassignedAt()));
    }

    @Override
    public void beforeUpdate(ContractAssignmentReq req, Long id) {
        this.validateRelation(req, id);
        req.setStatus(this.resolveStatus(req.getAssignedAt(), req.getUnassignedAt()));
    }

    private void validateRelation(ContractAssignmentReq req, Long id) {
        req.setPositionName(StrUtil.trim(req.getPositionName()));
        req.setLevelName(StrUtil.trim(req.getLevelName()));
        CheckUtils.throwIf(req.getAssignedAt() == null, "派遣日期不能为空");
        CheckUtils.throwIf(req.getUnassignedAt() != null && req.getUnassignedAt()
            .isBefore(req.getAssignedAt()), "撤场日期不能早于派遣日期");
        ContractDO contract = contractMapper.selectById(req.getContractId());
        CheckUtils.throwIf(contract == null, "合同不存在");
        CheckUtils.throwIf(CONTRACT_STATUS_TERMINATED.equals(contract.getStatus()), "已终止合同不允许新增派遣");
        this.checkDateInContract(req.getAssignedAt(), contract.getStartDate(), contract.getEndDate(), "派遣日期");
        if (req.getUnassignedAt() != null) {
            this.checkDateInContract(req.getUnassignedAt(), contract.getStartDate(), contract.getEndDate(), "撤场日期");
        }
        EmployeeProfileDO employee = employeeProfileMapper.selectById(req.getEmployeeId());
        CheckUtils.throwIf(employee == null, "员工不存在");
        CheckUtils.throwIf(EMPLOYEE_STATUS_OFFBOARDED.equals(employee.getStatus()) && req
            .getUnassignedAt() == null, "离职员工不允许新增在岗派遣");
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(ContractAssignmentDO::getContractId, req.getContractId())
            .eq(ContractAssignmentDO::getEmployeeId, req.getEmployeeId())
            .eq(ContractAssignmentDO::getStatus, STATUS_ACTIVE)
            .ne(id != null, ContractAssignmentDO::getId, id)
            .exists() && req.getUnassignedAt() == null, "员工当前已存在生效中的派遣关系");
    }

    private String resolveStatus(LocalDate assignedAt, LocalDate unassignedAt) {
        CheckUtils.throwIf(unassignedAt != null && unassignedAt.isBefore(assignedAt), "撤场日期不能早于派遣日期");
        return unassignedAt == null ? STATUS_ACTIVE : STATUS_ENDED;
    }

    private void checkDateInContract(LocalDate targetDate, LocalDate startDate, LocalDate endDate, String fieldName) {
        CheckUtils.throwIf(targetDate.isBefore(startDate) || targetDate.isAfter(endDate), "{}必须处于合同有效期内", fieldName);
    }
}
