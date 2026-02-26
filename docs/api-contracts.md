# API 鍚堢害锛圫tage 1锛?
## 1. 鍏ㄥ眬绾﹀畾

- Base URL: `/api/admin/v1`
- 璁よ瘉锛歚Authorization: Bearer <JWT>`
- 骞傜瓑澶达紙鍏抽敭鍐欐搷浣滐級锛歚X-Idempotency-Key: <uuid>`
- Content-Type: `application/json; charset=utf-8`
- 鏃堕棿鏍煎紡锛欼SO8601锛圲TC+8锛?
### 1.1 缁熶竴鍝嶅簲

```json
{
  "code": "0",
  "message": "success",
  "traceId": "8d6e6e8a-50d3-47d0-b3af-39a4d09f0f1c",
  "timestamp": "2026-02-26T16:00:00+08:00",
  "data": {}
}
```

### 1.2 鍒嗛〉鍝嶅簲

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

### 1.3 閿欒鐮侊紙鎽樿锛?
| 閿欒鐮?| 鍚箟 |
|---|---|
| `AUTH-401` | 鏈璇佹垨浠ょ墝澶辨晥 |
| `AUTH-403` | 鏃犳潈闄愯闂?|
| `REQ-400` | 鍙傛暟鏍￠獙澶辫触 |
| `BUS-409` | 鐘舵€佸啿绐侊紙濡傞噸澶嶆彁浜ゃ€侀潪娉曠姸鎬佹祦杞級 |
| `SYS-500` | 绯荤粺寮傚父 |

## 2. 鎺ュ彛鍒嗙粍

### 2.1 Auth / IAM

#### 鐧诲綍
- 璺緞锛歚POST /api/admin/v1/auth/login`
- 鎻忚堪锛氱敤鎴峰悕瀵嗙爜鐧诲綍
- 璇锋眰鍙傛暟锛?  | 鍙傛暟 | 绫诲瀷 | 蹇呭～ | 璇存槑 |
  |---|---|---|---|
  | username | string | 鏄?| 鐧诲綍鍚?|
  | password | string | 鏄?| 瀵嗙爜 |
- 鍝嶅簲绀轰緥锛?```json
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

#### 鍒锋柊浠ょ墝
- 璺緞锛歚POST /api/admin/v1/auth/refresh`
- 鎻忚堪锛氫娇鐢?refresh token 鎹㈡柊 access token

#### 鑾峰彇褰撳墠鐢ㄦ埛
- 璺緞锛歚GET /api/admin/v1/auth/me`
- 鎻忚堪锛氳繑鍥炵敤鎴蜂俊鎭€佽鑹插拰鏉冮檺鐐瑰垪琛?
#### 瑙掕壊鍒楄〃
- 璺緞锛歚GET /api/admin/v1/iam/roles`
- 鎻忚堪锛氭煡璇㈣鑹蹭笌鏁版嵁鏉冮檺閰嶇疆

#### RBAC 鍏煎璺敱锛坰ystem锛?- 璺緞锛歚GET /api/admin/v1/system/roles`
- 鎻忚堪锛氬吋瀹硅矾鐢憋紝璇箟涓?`GET /api/admin/v1/iam/roles` 涓€鑷?
#### 瑙掕壊鏉冮檺鏇存柊锛坰ystem锛?- 璺緞锛歚PUT /api/admin/v1/system/roles/{roleId}/permissions`
- 鎻忚堪锛氭洿鏂拌鑹叉潈闄愰泦鍚堬紝瀛楁 `permissionCodes: string[]`

#### 鏉冮檺鐐瑰垪琛紙system锛?- 璺緞锛歚GET /api/admin/v1/system/permissions`
- 鎻忚堪锛氳繑鍥炲彲鍒嗛厤鏉冮檺鐐瑰垪琛紙鐢ㄤ簬瑙掕壊鎺堟潈闈㈡澘锛?
#### 瑙掕壊鏁版嵁鑼冨洿鏌ヨ锛坰ystem锛?- 璺緞锛歚GET /api/admin/v1/system/roles/{roleId}/data-scope`
- 鎻忚堪锛氭煡璇㈣鑹?`data-scope` 绛栫暐锛坄scopeType`/`scopeRef`锛?
### 2.2 CRM / 瀹㈡埛绾跨储

#### 绾跨储鍒嗛〉鏌ヨ
- 璺緞锛歚GET /api/admin/v1/crm/leads`
- 鎻忚堪锛氭寜绛涢€夋潯浠舵煡璇㈢嚎绱?- 璇锋眰鍙傛暟锛?  | 鍙傛暟 | 绫诲瀷 | 蹇呭～ | 璇存槑 |
  |---|---|---|---|
  | projectName | string | 鍚?| 椤圭洰鍚嶇О妯＄硦鍖归厤 |
  | bizOwnerId | long | 鍚?| 涓氬姟寮€鍙戜汉 |
  | cooperationStatus | string | 鍚?| 鍚堜綔鐘舵€?|
  | pageNo | int | 鏄?| 椤电爜 |
  | pageSize | int | 鏄?| 姣忛〉鏉℃暟 |

#### 鏂板绾跨储
- 璺緞锛歚POST /api/admin/v1/crm/leads`
- 鎻忚堪锛氬垱寤哄鎴风嚎绱?
#### 鏇存柊绾跨储
- 璺緞锛歚PUT /api/admin/v1/crm/leads/{leadId}`
- 鎻忚堪锛氭洿鏂扮嚎绱俊鎭?
#### 绾跨储璺熻繘璁板綍
- 璺緞锛歚POST /api/admin/v1/crm/leads/{leadId}/tracks`
- 鎻忚堪锛氳拷鍔犺窡杩涙棩蹇?
#### 鐢ㄥ伐鍗曚綅鍒嗛〉鏌ヨ
- 璺緞锛歚GET /api/admin/v1/crm/employer-units`
- 鎻忚堪锛氭煡璇㈠凡杞寲鐨勭敤宸ュ崟浣?
#### 鏂板鐢ㄥ伐鍗曚綅
- 璺緞锛歚POST /api/admin/v1/crm/employer-units`
- 鎻忚堪锛氭柊澧炵敤宸ュ崟浣嶄富鏁版嵁

#### 鏇存柊鐢ㄥ伐鍗曚綅
- 璺緞锛歚PUT /api/admin/v1/crm/employer-units/{unitId}`
- 鎻忚堪锛氭洿鏂板崟浣嶄俊鎭€佸紑绁ㄤ俊鎭拰瀹㈡埛绛夌骇

### 2.3 Contract / 鍚堝悓

#### 鍚堝悓璇︽儏鏌ヨ
- 璺緞锛歚GET /api/admin/v1/contracts/labor-contracts/{contractId}`
- 鎻忚堪锛氭寜鍚堝悓ID鏌ヨ鍚堝悓璇︽儏涓庡綋鍓嶇姸鎬?
#### 鍒涘缓鍚堝悓
- 璺緞锛歚POST /api/admin/v1/contracts/labor-contracts`
- 鎻忚堪锛氬垱寤?A/B 绫诲悎鍚?- 璇锋眰鍙傛暟锛堟憳瑕侊級锛?  | 鍙傛暟 | 绫诲瀷 | 蹇呭～ | 璇存槑 |
  |---|---|---|---|
  | contractNo | string | 鏄?| 鍚堝悓鍙?|
  | employerUnitId | long | 鏄?| 鐢ㄥ伐鍗曚綅ID |
  | contractType | string | 鏄?| A/B |
  | startDate | date | 鏄?| 寮€濮嬫棩鏈?|
  | endDate | date | 鏄?| 缁撴潫鏃ユ湡 |
  | settlementCycle | string | 鏄?| 缁撶畻鍛ㄦ湡 |
  | taxRate | decimal | 鏄?| 绋庣巼 |

#### 鍚堝悓绛剧讲
- 璺緞锛歚PUT /api/admin/v1/contracts/labor-contracts/{contractId}/sign`
- 鎻忚堪锛氬皢鍚堝悓浠?DRAFT 绛剧讲涓?SIGNED

#### 鍚堝悓缁
- 璺緞锛歚PUT /api/admin/v1/contracts/labor-contracts/{contractId}/renew`
- 鎻忚堪锛氭牴鎹?newEndDate 鏇存柊鍚堝悓鍒版湡鏃?

#### 鍚堝悓缁堟
- 璺緞锛歚PUT /api/admin/v1/contracts/labor-contracts/{contractId}/terminate`
- 鎻忚堪锛氭寜 terminateDate 鎻愬墠缁堟鍚堝悓锛屽苟灏嗙姸鎬佹洿鏂颁负 TERMINATED

#### 缁撶畻瑙勫垯鍒涘缓
- 璺緞锛歚POST /api/admin/v1/contracts/settlement-rules`
- 鎻忚堪锛氭柊澧炵粨绠楄鍒欑増鏈紝鍚屽悎鍚屼笅 versionNo 蹇呴』鍞竴

#### 缁撶畻瑙勫垯鍙戝竷
- 璺緞锛歚PUT /api/admin/v1/contracts/settlement-rules/{ruleId}/publish`
- 鎻忚堪锛氬皢瑙勫垯鐗堟湰鏍囪涓?PUBLISHED锛屽苟杩涜鐢熸晥鏃ュ敮涓€鏍￠獙

#### 缁撶畻瑙勫垯鍋滅敤
- 璺緞锛歚PUT /api/admin/v1/contracts/settlement-rules/{ruleId}/deactivate`
- 鎻忚堪锛氬皢宸插彂甯冭鍒欐洿鏂颁负 DISABLED锛屼繚鐣欏巻鍙茬増鏈?

#### 缁撶畻瑙勫垯鐗堟湰鍒楄〃
- 璺緞锛歚GET /api/admin/v1/contracts/{contractId}/settlement-rules/versions`
- 鎻忚堪锛氭寜鍚堝悓鏌ヨ璇ヨ鍒欏巻鍙茬増鏈垪琛?

#### 缁撶畻瑙勫垯鏈夋晥鐗堟湰鏌ヨ
- 璺緞锛歚GET /api/admin/v1/contracts/{contractId}/settlement-rules/active?onDate=YYYY-MM-DD`
- 鎻忚堪锛氭寜鏃ユ湡鏌ヨ鍚堝悓涓嬪綋鍓嶆湁鏁堢殑宸插彂甯冭鍒?

### 2.4 Workforce / 鍛樺伐

#### 鍛樺伐鍒嗛〉鏌ヨ
- 璺緞锛歚GET /api/admin/v1/workforce/employees`
- 鎻忚堪锛氭寜鐘舵€併€侀儴闂ㄣ€佸矖浣嶆煡璇㈠憳宸?
#### 鍛樺伐鍏ヨ亴
- 璺緞锛歚POST /api/admin/v1/workforce/employees/onboard`
- 鎻忚堪锛氬垱寤哄憳宸ユ。妗堝苟鍒濆鍖栬处鎴蜂俊鎭?
#### 鍛樺伐绂昏亴
- 璺緞锛歚POST /api/admin/v1/workforce/employees/{employeeId}/offboard`
- 鎻忚堪锛氭墽琛岀鑱屾祦绋嬪苟璁板綍杞ㄨ抗

#### 鍛樺伐鍚堝悓娲鹃仯
- 璺緞锛歚POST /api/admin/v1/workforce/assignments`
- 鎻忚堪锛氬垱寤哄憳宸?鍚堝悓娲鹃仯鍏崇郴锛堣繘鍦?宀椾綅/鑱岀骇锛?
### 2.5 Attendance / 鑰冨嫟

#### 鎺掔彮瀵煎叆
- 璺緞锛歚POST /api/admin/v1/attendance/schedules/import`
- 鎻忚堪锛氭壒閲忓鍏ユ帓鐝?
#### 鑰冨嫟鏌ヨ
- 璺緞锛歚GET /api/admin/v1/attendance/records`
- 鎻忚堪锛氭寜鍚堝悓銆佸憳宸ャ€佹棩鏈熸煡璇㈣€冨嫟

#### 鎻愪氦琛ュ崱
- 璺緞锛歚POST /api/admin/v1/attendance/corrections`
- 鎻忚堪锛氭彁浜よˉ鍗＄敵璇?
#### 瀹℃壒琛ュ崱
- 璺緞锛歚POST /api/admin/v1/attendance/corrections/{correctionId}/approve`
- 鎻忚堪锛氬鎵归€氳繃鍚庤Е鍙戦噸绠?
### 2.6 Settlement / 缁撶畻

#### 瑙﹀彂缁撶畻浠诲姟
- 璺緞锛歚POST /api/admin/v1/settlements/jobs/run`
- 鎻忚堪锛氭寜鍚堝悓鎴栧懆鏈熻Е鍙戠粨绠?- 澶达細`X-Idempotency-Key` 蹇呭～

#### 缁撶畻鍗曞垪琛?- 璺緞锛歚GET /api/admin/v1/settlements/orders`
- 鎻忚堪锛氭煡璇㈢粨绠楀崟

#### 缁撶畻鍗曞鎵?- 璺緞锛歚POST /api/admin/v1/settlements/orders/{orderId}/approve`
- 鎻忚堪锛氬鎵圭粨绠楀崟

#### 鍒涘缓璋冩暣鍗?- 璺緞锛歚POST /api/admin/v1/settlements/orders/{orderId}/adjustments`
- 鎻忚堪锛氬皝鏉垮悗璋冩暣閲戦/宸ユ椂
- 澶达細`X-Idempotency-Key` 蹇呭～

### 2.7 Billing / 鏀粯

#### 鍒涘缓鏀粯鎵规
- 璺緞锛歚POST /api/admin/v1/billing/payment-batches`
- 鎻忚堪锛氬垱寤烘敮浠樻壒娆?- 澶达細`X-Idempotency-Key` 蹇呭～

#### 鎵ц鏀粯
- 璺緞锛歚POST /api/admin/v1/billing/payment-batches/{batchId}/pay`
- 鎻忚堪锛氭墽琛屾墦娆?- 澶达細`X-Idempotency-Key` 蹇呭～

#### 鏀粯缁撴灉鏌ヨ
- 璺緞锛歚GET /api/admin/v1/billing/payment-batches/{batchId}`
- 鎻忚堪锛氭煡璇㈡壒娆″拰璁板綍鐘舵€?
### 2.8 Finance / 鍙戠エ涓庡簲鏀?
#### 鍙戠エ鐢宠
- 璺緞锛歚POST /api/admin/v1/finance/invoices/applications`
- 鎻忚堪锛氭彁浜ゅ紑绁ㄧ敵璇?- 澶达細`X-Idempotency-Key` 蹇呭～

#### 鍙戠エ瀹℃壒
- 璺緞锛歚POST /api/admin/v1/finance/invoices/applications/{applyId}/approve`
- 鎻忚堪锛氬鎵瑰紑绁ㄧ敵璇?
#### 搴旀敹鍙拌处鏌ヨ
- 璺緞锛歚GET /api/admin/v1/finance/receivables`
- 鎻忚堪锛氬垎椤垫煡璇㈠簲鏀朵笌鍥炴鐘舵€?
#### 搴旀敹鏍搁攢
- 璺緞锛歚POST /api/admin/v1/finance/receivables/{ledgerId}/reconcile`
- 鎻忚堪锛氭墽琛屾牳閿€
- 澶达細`X-Idempotency-Key` 蹇呭～

### 2.9 Out of Scope锛圫tage 1锛?
1. `training/*`銆乣performance/*`銆乣points/*` 鎺ュ彛鍦?Stage 1 浠呭畾涔夋ā鍧楄竟鐣岋紝涓嶅湪鏈増鏈?API 濂戠害鍐呫€? 
2. 杩欎簺妯″潡灏嗗湪 Stage 2 鐨?Phase 瑙勫垝鍚庤ˉ鍏呰缁嗘帴鍙ｃ€?
## 3. 浜嬩欢濂戠害锛堝紓姝ワ級

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

## 4. 鐗堟湰绛栫暐

1. 褰撳墠涓荤増鏈細`v1`銆? 
2. 鏂板瀛楁锛氬悜鍚庡吋瀹癸紝榛樿鍙€夈€? 
3. 瀛楁璇箟鍙樻洿鎴栧垹闄わ細蹇呴』鍗囨鐗堟湰锛坄v2`锛夈€? 
4. 鎵€鏈夊彉鏇撮渶鍚屾鏇存柊 `docs/api-contracts.md` 涓庡搴旀祴璇曠敤渚嬨€?

## 4. Auth 字段补充（Phase 1）

### 4.1 `/api/admin/v1/auth/login`
- 请求字段：`username`、`password`
- 响应字段：`accessToken`、`refreshToken`、`expiresIn`
- 失败响应：`AUTH-401`（用户名或密码错误）

### 4.2 `/api/admin/v1/auth/refresh`
- 请求字段：`refreshToken`
- 响应字段：`accessToken`、`refreshToken`、`expiresIn`
- 失败响应：`AUTH-401`（refresh token 非法或失效）

### 4.3 `/api/admin/v1/auth/me`
- 请求头：`Authorization: Bearer <accessToken>`
- 响应字段：`id`、`username`、`displayName`、`roles[]`、`permissions[]`
- 失败响应：`AUTH-401`（未认证或令牌失效）
