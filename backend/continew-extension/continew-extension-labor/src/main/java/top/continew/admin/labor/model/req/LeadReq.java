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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "线索创建或修改参数")
public class LeadReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "线索编号不能为空")
    @Length(max = 64, message = "线索编号长度不能超过 {max} 个字符")
    private String leadCode;

    @NotBlank(message = "项目名称不能为空")
    @Length(max = 255, message = "项目名称长度不能超过 {max} 个字符")
    private String projectName;

    @Length(max = 64, message = "联系人长度不能超过 {max} 个字符")
    private String contactName;

    @Length(max = 64, message = "联系方式长度不能超过 {max} 个字符")
    private String contactPhone;

    @NotBlank(message = "行业类型不能为空")
    @Length(max = 32, message = "行业类型长度不能超过 {max} 个字符")
    private String industryType;

    @NotNull(message = "业务负责人不能为空")
    private Long bizOwnerId;

    @NotBlank(message = "合作状态不能为空")
    @Length(max = 32, message = "合作状态长度不能超过 {max} 个字符")
    private String cooperationStatus;

    private LocalDate tenderAt;

    @NotBlank(message = "保证金状态不能为空")
    @Length(max = 32, message = "保证金状态长度不能超过 {max} 个字符")
    private String depositStatus;
}
