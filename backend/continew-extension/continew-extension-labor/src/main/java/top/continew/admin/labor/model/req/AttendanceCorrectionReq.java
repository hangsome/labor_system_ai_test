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

@Data
@Schema(description = "补卡申请参数")
public class AttendanceCorrectionReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "考勤记录不能为空")
    private Long attendanceId;

    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @NotBlank(message = "补卡原因不能为空")
    @Length(max = 512, message = "补卡原因长度不能超过 {max} 个字符")
    private String reason;

    @Schema(hidden = true)
    private String status;
}
