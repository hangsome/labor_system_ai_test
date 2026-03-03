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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.labor.mapper.LeadFollowUpMapper;
import top.continew.admin.labor.mapper.LeadMapper;
import top.continew.admin.labor.model.entity.LeadDO;
import top.continew.admin.labor.model.entity.LeadFollowUpDO;
import top.continew.admin.labor.model.query.LeadQuery;
import top.continew.admin.labor.model.req.LeadFollowUpReq;
import top.continew.admin.labor.model.req.LeadReq;
import top.continew.admin.labor.model.req.LeadStatusTransitionReq;
import top.continew.admin.labor.model.resp.LeadFollowUpResp;
import top.continew.admin.labor.model.resp.LeadResp;
import top.continew.admin.labor.service.LeadService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl extends BaseServiceImpl<LeadMapper, LeadDO, LeadResp, LeadResp, LeadQuery, LeadReq> implements LeadService {

    private static final Map<String, Set<String>> STATUS_TRANSITION_MAP = Map.of("NEW", Set
        .of("FOLLOWING", "WON", "LOST"), "FOLLOWING", Set.of("WON", "LOST"), "WON", Set.of(), "LOST", Set.of());

    private final LeadFollowUpMapper leadFollowUpMapper;

    @Override
    public void beforeCreate(LeadReq req) {
        this.checkLeadCodeRepeat(req.getLeadCode(), null);
        req.setCooperationStatus(normalizeStatus(req.getCooperationStatus()));
    }

    @Override
    public void beforeUpdate(LeadReq req, Long id) {
        this.checkLeadCodeRepeat(req.getLeadCode(), id);
        req.setCooperationStatus(normalizeStatus(req.getCooperationStatus()));
    }

    @Override
    public void beforeDelete(List<Long> ids) {
        leadFollowUpMapper.delete(Wrappers.lambdaQuery(LeadFollowUpDO.class).in(LeadFollowUpDO::getLeadId, ids));
    }

    @Override
    public LeadResp transitionStatus(Long id, LeadStatusTransitionReq req) {
        LeadDO lead = super.getById(id);
        String fromStatus = normalizeStatus(lead.getCooperationStatus());
        String toStatus = normalizeStatus(req.getToStatus());
        CheckUtils.throwIf(fromStatus.equals(toStatus), "非法状态流转");
        Set<String> allowStatus = STATUS_TRANSITION_MAP.get(fromStatus);
        CheckUtils.throwIf(allowStatus == null || !allowStatus.contains(toStatus), "非法状态流转");

        lead.setCooperationStatus(toStatus);
        baseMapper.updateById(lead);

        LeadFollowUpDO followUpDO = new LeadFollowUpDO();
        followUpDO.setLeadId(id);
        followUpDO.setAction("STATUS_TRANSITION");
        followUpDO.setContent(StrUtil.emptyToNull(StrUtil.trim(req.getComment())));
        followUpDO.setStatus(toStatus);
        followUpDO.setStatusFrom(fromStatus);
        followUpDO.setStatusTo(toStatus);
        followUpDO.setOperatorId(Optional.ofNullable(UserContextHolder.getUserId()).orElse(0L));
        leadFollowUpMapper.insert(followUpDO);

        return super.get(id);
    }

    @Override
    public void createFollowUp(Long id, LeadFollowUpReq req) {
        LeadDO lead = super.getById(id);

        LeadFollowUpDO followUpDO = new LeadFollowUpDO();
        followUpDO.setLeadId(id);
        followUpDO.setAction("FOLLOW_UP");
        followUpDO.setContent(StrUtil.trim(req.getContent()));
        followUpDO.setStatus(normalizeStatus(lead.getCooperationStatus()));
        followUpDO.setNextContactAt(req.getNextContactAt());
        followUpDO.setOperatorId(Optional.ofNullable(UserContextHolder.getUserId()).orElse(0L));
        leadFollowUpMapper.insert(followUpDO);
    }

    @Override
    public List<LeadFollowUpResp> listFollowUp(Long id) {
        super.getById(id);
        return leadFollowUpMapper.lambdaQuery()
            .eq(LeadFollowUpDO::getLeadId, id)
            .orderByDesc(LeadFollowUpDO::getId)
            .list()
            .stream()
            .map(item -> BeanUtil.copyProperties(item, LeadFollowUpResp.class))
            .toList();
    }

    private void checkLeadCodeRepeat(String leadCode, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(LeadDO::getLeadCode, StrUtil.trim(leadCode))
            .ne(id != null, LeadDO::getId, id)
            .exists(), "线索编号 [{}] 已存在", leadCode);
    }

    private static String normalizeStatus(String status) {
        return StrUtil.blankToDefault(StrUtil.trim(status), "").toUpperCase(Locale.ROOT);
    }
}
