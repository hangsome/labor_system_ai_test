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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.labor.mapper.AttendanceRecordMapper;
import top.continew.admin.labor.mapper.ContractMapper;
import top.continew.admin.labor.mapper.EmployeeProfileMapper;
import top.continew.admin.labor.mapper.ShiftMapper;
import top.continew.admin.labor.model.entity.AttendanceRecordDO;
import top.continew.admin.labor.model.entity.ContractDO;
import top.continew.admin.labor.model.entity.EmployeeProfileDO;
import top.continew.admin.labor.model.entity.ShiftDO;
import top.continew.admin.labor.model.query.AttendanceRecordQuery;
import top.continew.admin.labor.model.req.AttendanceRecordReq;
import top.continew.admin.labor.model.resp.AttendanceRecordResp;
import top.continew.admin.labor.service.AttendanceRecordService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AttendanceRecordServiceImpl extends BaseServiceImpl<AttendanceRecordMapper, AttendanceRecordDO, AttendanceRecordResp, AttendanceRecordResp, AttendanceRecordQuery, AttendanceRecordReq> implements AttendanceRecordService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_ABNORMAL = "ABNORMAL";
    private static final String STATUS_CORRECTED = "CORRECTED";
    private static final int STANDARD_WORK_MINUTES = 8 * 60;

    private final ContractMapper contractMapper;
    private final EmployeeProfileMapper employeeProfileMapper;
    private final ShiftMapper shiftMapper;

    @Override
    public void beforeCreate(AttendanceRecordReq req) {
        this.validateReq(req, null, false);
        this.fillAttendanceMetrics(req);
    }

    @Override
    public void beforeUpdate(AttendanceRecordReq req, Long id) {
        AttendanceRecordDO oldRecord = super.getById(id);
        CheckUtils.throwIf(STATUS_CORRECTED.equals(oldRecord.getStatus()), "已修正记录不允许再次修改");
        this.validateReq(req, id, true);
        this.fillAttendanceMetrics(req);
    }

    private void validateReq(AttendanceRecordReq req, Long id, boolean isUpdate) {
        ContractDO contract = contractMapper.selectById(req.getContractId());
        CheckUtils.throwIf(contract == null, "合同不存在");
        EmployeeProfileDO employee = employeeProfileMapper.selectById(req.getEmployeeId());
        CheckUtils.throwIf(employee == null, "员工不存在");
        ShiftDO shift = shiftMapper.selectById(req.getShiftId());
        CheckUtils.throwIf(shift == null, "班次不存在");
        CheckUtils.throwIf(!shift.getContractId().equals(req.getContractId()), "班次与合同不匹配");
        CheckUtils.throwIf(req.getWorkDate() == null, "考勤日期不能为空");
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(AttendanceRecordDO::getEmployeeId, req.getEmployeeId())
            .eq(AttendanceRecordDO::getWorkDate, req.getWorkDate())
            .eq(AttendanceRecordDO::getShiftId, req.getShiftId())
            .ne(isUpdate, AttendanceRecordDO::getId, id)
            .exists(), "该员工当天班次考勤已存在");
    }

    private void fillAttendanceMetrics(AttendanceRecordReq req) {
        if (req.getCheckInAt() == null || req.getCheckOutAt() == null) {
            req.setWorkMinutes(0);
            req.setOvertimeMinutes(0);
            req.setStatus(STATUS_ABNORMAL);
            return;
        }
        long minutes = Duration.between(req.getCheckInAt(), req.getCheckOutAt()).toMinutes();
        CheckUtils.throwIf(minutes < 0, "签退时间不能早于签到时间");
        req.setWorkMinutes((int)minutes);
        req.setOvertimeMinutes((int)Math.max(minutes - STANDARD_WORK_MINUTES, 0));
        req.setStatus(STATUS_NORMAL);
    }
}
