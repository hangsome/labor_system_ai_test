# API 合约（Stage 1）

## 1. 全局约定

- Base URL: `/api/admin/v1`
- 认证：`Authorization: Bearer <JWT>`
- 幂等头（关键写操作）：`X-Idempotency-Key: <uuid>`
- Content-Type: `application/json; charset=utf-8`
- 时间格式：ISO8601（UTC+8）

### 1.1 统一响应

```json
{
  "code": "0",
  "message": "success",
  "traceId": "8d6e6e8a-50d3-47d0-b3af-39a4d09f0f1c",
  "timestamp": "2026-02-26T16:00:00+08:00",
  "data": {}
}
```

### 1.2 分页响应

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-id",
  "timestamp": "2026-02-26T16:00:00+08:00",
  "data": {
    "records": [],
    "pageNo": 1,
    "pageSize": 20,
    "total": 128
  }
}
```

### 1.3 错误码（摘要）

| 错误码 | 含义 |
|---|---|
| `AUTH-401` | 未认证或令牌失效 |
| `AUTH-403` | 无权限访问 |
| `REQ-400` | 参数校验失败 |
| `BUS-409` | 状态冲突（如重复提交、非法状态流转） |
| `SYS-500` | 系统异常 |

## 2. 接口分组

### 2.1 Auth / IAM

#### 登录
- 路径：`POST /api/admin/v1/auth/login`
- 描述：用户名密码登录
- 请求参数：
  | 参数 | 类型 | 必填 | 说明 |
  |---|---|---|---|
  | username | string | 是 | 登录名 |
  | password | string | 是 | 密码 |
- 响应示例：
```json
{
  "code": "0",
  "message": "success",
  "data": {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "expiresIn": 3600
  }
}
```

#### 刷新令牌
- 路径：`POST /api/admin/v1/auth/refresh`
- 描述：使用 refresh token 换新 access token

#### 获取当前用户
- 路径：`GET /api/admin/v1/auth/me`
- 描述：返回用户信息、角色和权限点列表

#### 角色列表
- 路径：`GET /api/admin/v1/iam/roles`
- 描述：查询角色与数据权限配置

### 2.2 CRM / 客户线索

#### 线索分页查询
- 路径：`GET /api/admin/v1/crm/leads`
- 描述：按筛选条件查询线索
- 请求参数：
  | 参数 | 类型 | 必填 | 说明 |
  |---|---|---|---|
  | projectName | string | 否 | 项目名称模糊匹配 |
  | bizOwnerId | long | 否 | 业务开发人 |
  | cooperationStatus | string | 否 | 合作状态 |
  | pageNo | int | 是 | 页码 |
  | pageSize | int | 是 | 每页条数 |

#### 新增线索
- 路径：`POST /api/admin/v1/crm/leads`
- 描述：创建客户线索

#### 更新线索
- 路径：`PUT /api/admin/v1/crm/leads/{leadId}`
- 描述：更新线索信息

#### 线索跟进记录
- 路径：`POST /api/admin/v1/crm/leads/{leadId}/tracks`
- 描述：追加跟进日志

#### 用工单位分页查询
- 路径：`GET /api/admin/v1/crm/employer-units`
- 描述：查询已转化的用工单位

#### 新增用工单位
- 路径：`POST /api/admin/v1/crm/employer-units`
- 描述：新增用工单位主数据

#### 更新用工单位
- 路径：`PUT /api/admin/v1/crm/employer-units/{unitId}`
- 描述：更新单位信息、开票信息和客户等级

### 2.3 Contract / 合同

#### 合同分页查询
- 路径：`GET /api/admin/v1/contracts`
- 描述：按状态、单位、周期筛选合同

#### 创建合同
- 路径：`POST /api/admin/v1/contracts`
- 描述：创建 A/B 类合同
- 请求参数（摘要）：
  | 参数 | 类型 | 必填 | 说明 |
  |---|---|---|---|
  | employerUnitId | long | 是 | 用工单位ID |
  | contractType | string | 是 | A/B |
  | startDate | date | 是 | 开始日期 |
  | endDate | date | 是 | 结束日期 |
  | settlementCycle | string | 是 | 结算周期 |

#### 配置结算规则
- 路径：`POST /api/admin/v1/contracts/{contractId}/settlement-rules`
- 描述：新增结算规则版本
- 头：`X-Idempotency-Key` 必填

#### 合同续签
- 路径：`POST /api/admin/v1/contracts/{contractId}/renew`
- 描述：基于旧合同快速创建新合同

#### 合同终止
- 路径：`POST /api/admin/v1/contracts/{contractId}/terminate`
- 描述：提前终止并生成终止结算清单

### 2.4 Workforce / 员工

#### 员工分页查询
- 路径：`GET /api/admin/v1/workforce/employees`
- 描述：按状态、部门、岗位查询员工

#### 员工入职
- 路径：`POST /api/admin/v1/workforce/employees/onboard`
- 描述：创建员工档案并初始化账户信息

#### 员工离职
- 路径：`POST /api/admin/v1/workforce/employees/{employeeId}/offboard`
- 描述：执行离职流程并记录轨迹

#### 员工合同派遣
- 路径：`POST /api/admin/v1/workforce/assignments`
- 描述：创建员工-合同派遣关系（进场/岗位/职级）

### 2.5 Attendance / 考勤

#### 排班导入
- 路径：`POST /api/admin/v1/attendance/schedules/import`
- 描述：批量导入排班

#### 考勤查询
- 路径：`GET /api/admin/v1/attendance/records`
- 描述：按合同、员工、日期查询考勤

#### 提交补卡
- 路径：`POST /api/admin/v1/attendance/corrections`
- 描述：提交补卡申请

#### 审批补卡
- 路径：`POST /api/admin/v1/attendance/corrections/{correctionId}/approve`
- 描述：审批通过后触发重算

### 2.6 Settlement / 结算

#### 触发结算任务
- 路径：`POST /api/admin/v1/settlements/jobs/run`
- 描述：按合同或周期触发结算
- 头：`X-Idempotency-Key` 必填

#### 结算单列表
- 路径：`GET /api/admin/v1/settlements/orders`
- 描述：查询结算单

#### 结算单审批
- 路径：`POST /api/admin/v1/settlements/orders/{orderId}/approve`
- 描述：审批结算单

#### 创建调整单
- 路径：`POST /api/admin/v1/settlements/orders/{orderId}/adjustments`
- 描述：封板后调整金额/工时
- 头：`X-Idempotency-Key` 必填

### 2.7 Billing / 支付

#### 创建支付批次
- 路径：`POST /api/admin/v1/billing/payment-batches`
- 描述：创建支付批次
- 头：`X-Idempotency-Key` 必填

#### 执行支付
- 路径：`POST /api/admin/v1/billing/payment-batches/{batchId}/pay`
- 描述：执行打款
- 头：`X-Idempotency-Key` 必填

#### 支付结果查询
- 路径：`GET /api/admin/v1/billing/payment-batches/{batchId}`
- 描述：查询批次和记录状态

### 2.8 Finance / 发票与应收

#### 发票申请
- 路径：`POST /api/admin/v1/finance/invoices/applications`
- 描述：提交开票申请
- 头：`X-Idempotency-Key` 必填

#### 发票审批
- 路径：`POST /api/admin/v1/finance/invoices/applications/{applyId}/approve`
- 描述：审批开票申请

#### 应收台账查询
- 路径：`GET /api/admin/v1/finance/receivables`
- 描述：分页查询应收与回款状态

#### 应收核销
- 路径：`POST /api/admin/v1/finance/receivables/{ledgerId}/reconcile`
- 描述：执行核销
- 头：`X-Idempotency-Key` 必填

### 2.9 Out of Scope（Stage 1）

1. `training/*`、`performance/*`、`points/*` 接口在 Stage 1 仅定义模块边界，不在本版本 API 契约内。  
2. 这些模块将在 Stage 2 的 Phase 规划后补充详细接口。

## 3. 事件契约（异步）

### `settlement.completed`
```json
{
  "eventId": "uuid",
  "occurredAt": "2026-02-26T16:00:00+08:00",
  "settlementOrderId": 1001,
  "contractId": 88,
  "totalAmount": 25600.50
}
```

### `payment.status.changed`
```json
{
  "eventId": "uuid",
  "batchId": 9001,
  "status": "PAID",
  "occurredAt": "2026-02-26T16:05:00+08:00",
  "paidAt": "2026-02-26T16:05:00+08:00"
}
```

### `invoice.status.changed`
```json
{
  "eventId": "uuid",
  "invoiceApplyId": 3008,
  "status": "ISSUED",
  "occurredAt": "2026-02-26T16:09:00+08:00",
  "issuedAmount": 120000.00
}
```

### `attendance.abnormal`
```json
{
  "eventId": "uuid",
  "attendanceId": 7788,
  "employeeId": 1024,
  "contractId": 88,
  "workDate": "2026-02-26",
  "occurredAt": "2026-02-26T16:08:00+08:00",
  "reasonCode": "MISSING_CHECK_OUT"
}
```

### `audit.action`
```json
{
  "eventId": "uuid",
  "bizType": "settlement_order",
  "bizId": "1001",
  "action": "APPROVE",
  "operatorId": 501,
  "occurredAt": "2026-02-26T16:10:00+08:00"
}
```

## 4. 版本策略

1. 当前主版本：`v1`。  
2. 新增字段：向后兼容，默认可选。  
3. 字段语义变更或删除：必须升次版本（`v2`）。  
4. 所有变更需同步更新 `docs/api-contracts.md` 与对应测试用例。
