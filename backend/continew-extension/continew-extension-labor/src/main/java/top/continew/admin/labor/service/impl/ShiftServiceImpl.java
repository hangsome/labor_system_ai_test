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
import top.continew.admin.labor.mapper.ShiftMapper;
import top.continew.admin.labor.model.entity.ContractDO;
import top.continew.admin.labor.model.entity.ShiftDO;
import top.continew.admin.labor.model.query.ShiftQuery;
import top.continew.admin.labor.model.req.ShiftReq;
import top.continew.admin.labor.model.resp.ShiftResp;
import top.continew.admin.labor.service.ShiftService;
import top.continew.starter.core.util.validation.CheckUtils;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl extends BaseServiceImpl<ShiftMapper, ShiftDO, ShiftResp, ShiftResp, ShiftQuery, ShiftReq> implements ShiftService {

    private final ContractMapper contractMapper;

    @Override
    public void beforeCreate(ShiftReq req) {
        this.validateReq(req);
    }

    @Override
    public void beforeUpdate(ShiftReq req, Long id) {
        this.validateReq(req);
    }

    private void validateReq(ShiftReq req) {
        req.setShiftName(StrUtil.trim(req.getShiftName()));
        ContractDO contract = contractMapper.selectById(req.getContractId());
        CheckUtils.throwIf(contract == null, "合同不存在");
        CheckUtils.throwIf(req.getStartTime() == null || req.getEndTime() == null, "班次时间不能为空");
        CheckUtils.throwIf(req.getStartTime().equals(req.getEndTime()), "开始时间不能与结束时间相同");
    }
}
