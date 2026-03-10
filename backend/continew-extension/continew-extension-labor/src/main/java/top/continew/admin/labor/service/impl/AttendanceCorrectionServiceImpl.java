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
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.labor.mapper.AttendanceCorrectionMapper;
import top.continew.admin.labor.mapper.AttendanceRecordMapper;
import top.continew.admin.labor.mapper.EmployeeProfileMapper;
import top.continew.admin.labor.model.entity.AttendanceCorrectionDO;
import top.continew.admin.labor.model.entity.AttendanceRecordDO;
import top.continew.admin.labor.model.entity.EmployeeProfileDO;
import top.continew.admin.labor.model.query.AttendanceCorrectionQuery;
import top.continew.admin.labor.model.req.AttendanceCorrectionReq;
import top.continew.admin.labor.model.resp.AttendanceCorrectionResp;
import top.continew.admin.labor.service.AttendanceCorrectionService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceCorrectionServiceImpl extends BaseServiceImpl<AttendanceCorrectionMapper, AttendanceCorrectionDO, AttendanceCorrectionResp, AttendanceCorrectionResp, AttendanceCorrectionQuery, AttendanceCorrectionReq> implements AttendanceCorrectionService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String RECORD_STATUS_CORRECTED = "CORRECTED";

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final EmployeeProfileMapper employeeProfileMapper;

    @Override
    public void beforeCreate(AttendanceCorrectionReq req) {
        req.setReason(StrUtil.trim(req.getReason()));
        AttendanceRecordDO attendanceRecord = attendanceRecordMapper.selectById(req.getAttendanceId());
        CheckUtils.throwIf(attendanceRecord == null, "考勤记录不存在");
        CheckUtils.throwIf(!attendanceRecord.getEmployeeId().equals(req.getEmployeeId()), "补卡员工与考勤记录不匹配");
        EmployeeProfileDO employee = employeeProfileMapper.selectById(req.getEmployeeId());
        CheckUtils.throwIf(employee == null, "员工不存在");
        req.setStatus(STATUS_PENDING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCorrectionResp approve(Long id) {
        AttendanceCorrectionDO correction = super.getById(id);
        CheckUtils.throwIf(!STATUS_PENDING.equals(correction.getStatus()), "仅待审批申请允许通过");
        correction.setStatus(STATUS_APPROVED);
        correction.setReviewedBy(Optional.ofNullable(UserContextHolder.getUserId()).orElse(0L));
        correction.setReviewedAt(LocalDateTime.now());
        baseMapper.updateById(correction);
        AttendanceRecordDO attendanceRecord = attendanceRecordMapper.selectById(correction.getAttendanceId());
        attendanceRecord.setStatus(RECORD_STATUS_CORRECTED);
        attendanceRecordMapper.updateById(attendanceRecord);
        return super.get(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCorrectionResp reject(Long id) {
        AttendanceCorrectionDO correction = super.getById(id);
        CheckUtils.throwIf(!STATUS_PENDING.equals(correction.getStatus()), "仅待审批申请允许驳回");
        correction.setStatus(STATUS_REJECTED);
        correction.setReviewedBy(Optional.ofNullable(UserContextHolder.getUserId()).orElse(0L));
        correction.setReviewedAt(LocalDateTime.now());
        baseMapper.updateById(correction);
        return super.get(id);
    }
}
