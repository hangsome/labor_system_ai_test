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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "合同创建或修改参数")
public class ContractReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "合同编号不能为空")
    @Length(max = 128, message = "合同编号长度不能超过 {max} 个字符")
    private String contractNo;

    @NotNull(message = "用工单位不能为空")
    private Long employerUnitId;

    @NotBlank(message = "合同名称不能为空")
    @Length(max = 255, message = "合同名称长度不能超过 {max} 个字符")
    private String contractName;

    @NotBlank(message = "合同类型不能为空")
    @Length(max = 16, message = "合同类型长度不能超过 {max} 个字符")
    private String contractType;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotBlank(message = "结算周期不能为空")
    @Length(max = 32, message = "结算周期长度不能超过 {max} 个字符")
    private String settlementCycle;

    @NotNull(message = "税率不能为空")
    @DecimalMin(value = "0.0000", message = "税率不能小于 {value}")
    @DecimalMax(value = "1.0000", message = "税率不能大于 {value}")
    private BigDecimal taxRate;

    @Schema(hidden = true)
    private String status;
}
