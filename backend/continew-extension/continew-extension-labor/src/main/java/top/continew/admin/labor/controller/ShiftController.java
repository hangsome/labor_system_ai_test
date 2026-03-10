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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.labor.model.query.ShiftQuery;
import top.continew.admin.labor.model.req.ShiftReq;
import top.continew.admin.labor.model.resp.ShiftResp;
import top.continew.admin.labor.service.ShiftService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

@Tag(name = "排班管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/labor/attendance/shift", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE,
    Api.BATCH_DELETE})
public class ShiftController extends BaseController<ShiftService, ShiftResp, ShiftResp, ShiftQuery, ShiftReq> {
}
