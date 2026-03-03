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
import top.continew.admin.labor.mapper.EmployerMapper;
import top.continew.admin.labor.model.entity.EmployerDO;
import top.continew.admin.labor.model.query.EmployerQuery;
import top.continew.admin.labor.model.req.EmployerReq;
import top.continew.admin.labor.model.resp.EmployerResp;
import top.continew.admin.labor.service.EmployerService;
import top.continew.starter.core.util.validation.CheckUtils;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl extends BaseServiceImpl<EmployerMapper, EmployerDO, EmployerResp, EmployerResp, EmployerQuery, EmployerReq> implements EmployerService {

    @Override
    public void beforeCreate(EmployerReq req) {
        this.checkUnitCodeRepeat(req.getUnitCode(), null);
    }

    @Override
    public void beforeUpdate(EmployerReq req, Long id) {
        this.checkUnitCodeRepeat(req.getUnitCode(), id);
    }

    @Override
    public EmployerResp deactivate(Long id) {
        EmployerDO employer = super.getById(id);
        employer.setCustomerLevel("INACTIVE");
        baseMapper.updateById(employer);
        return super.get(id);
    }

    private void checkUnitCodeRepeat(String unitCode, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(EmployerDO::getUnitCode, StrUtil.trim(unitCode))
            .ne(id != null, EmployerDO::getId, id)
            .exists(), "单位编号 [{}] 已存在", unitCode);
    }
}
