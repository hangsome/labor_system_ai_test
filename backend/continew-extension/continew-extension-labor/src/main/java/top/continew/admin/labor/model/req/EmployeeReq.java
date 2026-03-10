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
@Schema(description = "员工创建或修改参数")
public class EmployeeReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "员工编号不能为空")
    @Length(max = 64, message = "员工编号长度不能超过 {max} 个字符")
    private String employeeNo;

    @NotBlank(message = "员工姓名不能为空")
    @Length(max = 128, message = "员工姓名长度不能超过 {max} 个字符")
    private String name;

    @Length(max = 64, message = "证件号长度不能超过 {max} 个字符")
    private String idNo;

    @Length(max = 32, message = "手机号长度不能超过 {max} 个字符")
    private String phone;

    private Long deptId;

    @NotNull(message = "入职日期不能为空")
    private LocalDate hiredAt;

    @NotBlank(message = "开户名不能为空")
    @Length(max = 128, message = "开户名长度不能超过 {max} 个字符")
    private String accountName;

    @NotBlank(message = "开户行不能为空")
    @Length(max = 128, message = "开户行长度不能超过 {max} 个字符")
    private String bankName;

    @NotBlank(message = "银行卡号不能为空")
    @Length(max = 128, message = "银行卡号长度不能超过 {max} 个字符")
    private String bankNo;

    @Schema(hidden = true)
    private String status;

    @Schema(hidden = true)
    private LocalDate offboardAt;
}
