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
import java.time.LocalDate;

@Data
@Schema(description = "考勤查询条件")
public class AttendanceRecordQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同 ID", example = "1")
    @Query
    private Long contractId;

    @Schema(description = "员工 ID", example = "1")
    @Query
    private Long employeeId;

    @Schema(description = "班次 ID", example = "1")
    @Query
    private Long shiftId;

    @Schema(description = "考勤日期", example = "2026-03-10")
    @Query
    private LocalDate workDate;

    @Schema(description = "考勤状态", example = "NORMAL")
    @Query
    private String status;
}
