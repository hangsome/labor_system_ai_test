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

package top.continew.admin.labor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.labor.model.query.LeadQuery;
import top.continew.admin.labor.model.req.LeadFollowUpReq;
import top.continew.admin.labor.model.req.LeadReq;
import top.continew.admin.labor.model.req.LeadStatusTransitionReq;
import top.continew.admin.labor.model.resp.LeadFollowUpResp;
import top.continew.admin.labor.model.resp.LeadResp;
import top.continew.admin.labor.service.LeadService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.util.List;

@Tag(name = "线索管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/labor/lead", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE})
public class LeadController extends BaseController<LeadService, LeadResp, LeadResp, LeadQuery, LeadReq> {

    @SaCheckPermission("labor:lead:transition")
    @PutMapping("/{id}/status")
    public LeadResp transitionStatus(@PathVariable Long id, @RequestBody @Valid LeadStatusTransitionReq req) {
        return baseService.transitionStatus(id, req);
    }

    @SaCheckPermission("labor:lead:followUp:create")
    @PostMapping("/{id}/follow-up")
    public void createFollowUp(@PathVariable Long id, @RequestBody @Valid LeadFollowUpReq req) {
        baseService.createFollowUp(id, req);
    }

    @SaCheckPermission("labor:lead:followUp:list")
    @GetMapping("/{id}/follow-up")
    public List<LeadFollowUpResp> listFollowUp(@PathVariable Long id) {
        return baseService.listFollowUp(id);
    }
}
