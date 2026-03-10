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
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.labor.mapper.ContractAssignmentMapper;
import top.continew.admin.labor.mapper.EmployeeBankAccountMapper;
import top.continew.admin.labor.mapper.EmployeeProfileMapper;
import top.continew.admin.labor.model.entity.ContractAssignmentDO;
import top.continew.admin.labor.model.entity.EmployeeBankAccountDO;
import top.continew.admin.labor.model.entity.EmployeeProfileDO;
import top.continew.admin.labor.model.query.EmployeeQuery;
import top.continew.admin.labor.model.req.EmployeeOffboardReq;
import top.continew.admin.labor.model.req.EmployeeReq;
import top.continew.admin.labor.model.resp.EmployeeResp;
import top.continew.admin.labor.service.EmployeeService;
import top.continew.starter.core.util.validation.CheckUtils;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends BaseServiceImpl<EmployeeProfileMapper, EmployeeProfileDO, EmployeeResp, EmployeeResp, EmployeeQuery, EmployeeReq> implements EmployeeService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_OFFBOARDED = "OFFBOARDED";
    private static final String ASSIGNMENT_STATUS_ENDED = "ENDED";

    private final EmployeeBankAccountMapper bankAccountMapper;
    private final ContractAssignmentMapper assignmentMapper;

    @Override
    public void beforeCreate(EmployeeReq req) {
        this.checkEmployeeNoRepeat(req.getEmployeeNo(), null);
        this.normalize(req);
        req.setStatus(STATUS_ACTIVE);
        req.setOffboardAt(null);
    }

    @Override
    public void beforeUpdate(EmployeeReq req, Long id) {
        EmployeeProfileDO oldEmployee = super.getById(id);
        this.checkEmployeeNoRepeat(req.getEmployeeNo(), id);
        this.normalize(req);
        req.setStatus(oldEmployee.getStatus());
        req.setOffboardAt(oldEmployee.getOffboardAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(EmployeeReq req) {
        Long id = super.create(req);
        this.saveDefaultBankAccount(id, req);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EmployeeReq req, Long id) {
        super.update(req, id);
        this.saveDefaultBankAccount(id, req);
    }

    @Override
    public EmployeeResp get(Long id) {
        EmployeeResp resp = super.get(id);
        EmployeeBankAccountDO bankAccount = bankAccountMapper.lambdaQuery()
            .eq(EmployeeBankAccountDO::getEmployeeId, id)
            .eq(EmployeeBankAccountDO::getDefaultAccount, true)
            .one();
        if (bankAccount != null) {
            resp.setAccountName(bankAccount.getAccountName());
            resp.setBankName(bankAccount.getBankName());
            resp.setBankNo(bankAccount.getBankNo());
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offboard(Long id, EmployeeOffboardReq req) {
        EmployeeProfileDO employee = super.getById(id);
        CheckUtils.throwIf(STATUS_OFFBOARDED.equals(employee.getStatus()), "当前员工已离职");
        CheckUtils.throwIf(req.getOffboardAt().isBefore(employee.getHiredAt()), "离职日期不能早于入职日期");
        employee.setStatus(STATUS_OFFBOARDED);
        employee.setOffboardAt(req.getOffboardAt());
        baseMapper.updateById(employee);
        assignmentMapper.lambdaUpdate()
            .set(ContractAssignmentDO::getStatus, ASSIGNMENT_STATUS_ENDED)
            .set(ContractAssignmentDO::getUnassignedAt, req.getOffboardAt())
            .eq(ContractAssignmentDO::getEmployeeId, id)
            .eq(ContractAssignmentDO::getStatus, "ACTIVE")
            .update();
    }

    private void checkEmployeeNoRepeat(String employeeNo, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(EmployeeProfileDO::getEmployeeNo, StrUtil.trim(employeeNo))
            .ne(id != null, EmployeeProfileDO::getId, id)
            .exists(), "员工编号 [{}] 已存在", employeeNo);
    }

    private void normalize(EmployeeReq req) {
        req.setEmployeeNo(StrUtil.trim(req.getEmployeeNo()));
        req.setName(StrUtil.trim(req.getName()));
        req.setIdNo(StrUtil.trim(req.getIdNo()));
        req.setPhone(StrUtil.trim(req.getPhone()));
        req.setAccountName(StrUtil.trim(req.getAccountName()));
        req.setBankName(StrUtil.trim(req.getBankName()));
        req.setBankNo(StrUtil.trim(req.getBankNo()));
        CheckUtils.throwIf(req.getHiredAt() == null, "入职日期不能为空");
        CheckUtils.throwIf(StrUtil.isBlank(req.getBankNo()), "银行卡号不能为空");
    }

    private void saveDefaultBankAccount(Long employeeId, EmployeeReq req) {
        EmployeeBankAccountDO bankAccount = bankAccountMapper.lambdaQuery()
            .eq(EmployeeBankAccountDO::getEmployeeId, employeeId)
            .eq(EmployeeBankAccountDO::getDefaultAccount, true)
            .one();
        if (bankAccount == null) {
            bankAccount = new EmployeeBankAccountDO();
            bankAccount.setEmployeeId(employeeId);
            bankAccount.setDefaultAccount(true);
            bankAccount.setAccountName(req.getAccountName());
            bankAccount.setBankName(req.getBankName());
            bankAccount.setBankNo(req.getBankNo());
            bankAccountMapper.insert(bankAccount);
            return;
        }
        bankAccount.setAccountName(req.getAccountName());
        bankAccount.setBankName(req.getBankName());
        bankAccount.setBankNo(req.getBankNo());
        bankAccountMapper.updateById(bankAccount);
    }
}
