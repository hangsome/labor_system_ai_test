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

package top.continew.admin.labor.service;

import top.continew.admin.common.base.service.BaseService;
import top.continew.admin.labor.model.query.EmployeeQuery;
import top.continew.admin.labor.model.req.EmployeeOffboardReq;
import top.continew.admin.labor.model.req.EmployeeReq;
import top.continew.admin.labor.model.resp.EmployeeResp;

public interface EmployeeService extends BaseService<EmployeeResp, EmployeeResp, EmployeeQuery, EmployeeReq> {

    void offboard(Long id, EmployeeOffboardReq req);
}
