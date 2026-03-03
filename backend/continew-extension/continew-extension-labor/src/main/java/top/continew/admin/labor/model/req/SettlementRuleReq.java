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

package top.continew.admin.labor.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "结算规则创建或修改参数")
public class SettlementRuleReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "合同 ID 不能为空")
    private Long contractId;

    @NotBlank(message = "规则类型不能为空")
    @Length(max = 16, message = "规则类型长度不能超过 {max} 个字符")
    private String ruleType;

    @NotNull(message = "版本号不能为空")
    @Min(value = 1, message = "版本号必须大于等于 {value}")
    private Integer versionNo;

    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveFrom;

    @NotBlank(message = "规则内容不能为空")
    private String rulePayload;

    @Schema(hidden = true)
    private String status;
}
