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
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "用工单位创建或修改参数")
public class EmployerReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "单位编号不能为空")
    @Length(max = 64, message = "单位编号长度不能超过 {max} 个字符")
    private String unitCode;

    private Long leadId;

    @NotBlank(message = "单位名称不能为空")
    @Length(max = 255, message = "单位名称长度不能超过 {max} 个字符")
    private String unitName;

    @NotBlank(message = "客户等级不能为空")
    @Length(max = 16, message = "客户等级长度不能超过 {max} 个字符")
    private String customerLevel;

    @Length(max = 512, message = "地址长度不能超过 {max} 个字符")
    private String address;

    private String invoiceInfo;

    private Boolean outsource;
}
