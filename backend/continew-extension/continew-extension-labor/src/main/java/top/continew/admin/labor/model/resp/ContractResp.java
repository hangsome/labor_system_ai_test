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

package top.continew.admin.labor.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.base.model.resp.BaseDetailResp;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "合同响应参数")
public class ContractResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    private String contractNo;

    private Long employerUnitId;

    private String contractName;

    private String contractType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String settlementCycle;

    private String status;

    private BigDecimal taxRate;
}
