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
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.labor.model.query.SettlementRuleQuery;
import top.continew.admin.labor.model.req.SettlementRuleReq;
import top.continew.admin.labor.model.resp.SettlementRuleResp;
import top.continew.admin.labor.service.SettlementRuleService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "结算规则管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/labor/settlement", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE})
public class SettlementRuleController extends BaseController<SettlementRuleService, SettlementRuleResp, SettlementRuleResp, SettlementRuleQuery, SettlementRuleReq> {

    @SaCheckPermission("labor:settlement:publish")
    @PutMapping("/{id}/publish")
    public SettlementRuleResp publish(@PathVariable Long id) {
        return baseService.publish(id);
    }

    @SaCheckPermission("labor:settlement:deactivate")
    @PutMapping("/{id}/deactivate")
    public SettlementRuleResp deactivate(@PathVariable Long id) {
        return baseService.deactivate(id);
    }

    @SaCheckPermission("labor:settlement:version")
    @GetMapping("/version/{contractId}")
    public List<SettlementRuleResp> listVersion(@PathVariable Long contractId) {
        return baseService.listVersion(contractId);
    }

    @SaCheckPermission("labor:settlement:active")
    @GetMapping("/active/{contractId}")
    public SettlementRuleResp getActive(@PathVariable Long contractId,
                                        @RequestParam(value = "onDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
        return baseService.getActive(contractId, onDate);
    }
}
