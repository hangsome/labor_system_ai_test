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

package top.continew.admin.labor.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.data.annotation.Query;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "结算规则查询条件")
public class SettlementRuleQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同 ID", example = "1")
    @Query
    private Long contractId;

    @Schema(description = "规则状态", example = "PUBLISHED")
    @Query
    private String status;
}
