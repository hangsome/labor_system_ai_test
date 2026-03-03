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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.labor.model.query.ContractQuery;
import top.continew.admin.labor.model.req.ContractRenewReq;
import top.continew.admin.labor.model.req.ContractReq;
import top.continew.admin.labor.model.req.ContractTerminateReq;
import top.continew.admin.labor.model.resp.ContractResp;
import top.continew.admin.labor.service.ContractService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

@Tag(name = "合同管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/labor/contract", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE})
public class ContractController extends BaseController<ContractService, ContractResp, ContractResp, ContractQuery, ContractReq> {

    @SaCheckPermission("labor:contract:sign")
    @PutMapping("/{id}/sign")
    public ContractResp sign(@PathVariable Long id) {
        return baseService.sign(id);
    }

    @SaCheckPermission("labor:contract:renew")
    @PutMapping("/{id}/renew")
    public ContractResp renew(@PathVariable Long id, @RequestBody @Valid ContractRenewReq req) {
        return baseService.renew(id, req);
    }

    @SaCheckPermission("labor:contract:terminate")
    @PutMapping("/{id}/terminate")
    public ContractResp terminate(@PathVariable Long id, @RequestBody @Valid ContractTerminateReq req) {
        return baseService.terminate(id, req);
    }
}
