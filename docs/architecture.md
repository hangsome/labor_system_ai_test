# 绯荤粺鏋舵瀯璁捐锛圫tage 1锛?

## 1. 绯荤粺姒傝堪

鍔冲姟绯荤粺鏄潰鍚戦厭搴楃瓑鐢ㄥ伐琛屼笟鐨勪紒涓氱鐞嗗钩鍙帮紝瑕嗙洊浠庡鎴风嚎绱€佸悎鍚岀绾︺€佸憳宸ョ鐞嗐€佽€冨嫟缁撶畻鍒板彂绁ㄥ洖娆剧殑涓氬姟闂幆銆? 
鏈湡閲囩敤鈥滄ā鍧楀寲鍗曚綋 + 浜嬩欢椹卞姩鎵╁睍鈥濇灦鏋勶紝鍏堜繚闅滀氦浠橀€熷害涓庝笟鍔′竴鑷存€э紝鍦ㄥ苟鍙戝拰缁勭粐瑙勬ā澧為暱鏃舵紨杩涗负鏈嶅姟鍖栥€?

## 2. 妯″潡鍒掑垎

### 2.1 妯″潡鎬昏

| 妯″潡鍚?| 鑱岃矗 | 绫诲瀷 | 渚濊禆妯″潡 |
|---|---|---|---|
| iam | 鐧诲綍銆佽璇併€丷BAC銆佹暟鎹潈闄?| 鍏ㄦ爤 | - |
| platform | 瀛楀吀銆佺郴缁熼厤缃€佸璁℃棩蹇椼€侀€氱煡 | 鍚庣 | iam |
| crm | 瀹㈡埛绾跨储銆佺敤宸ュ崟浣嶃€侀」鐩熀纭€璧勬枡 | 鍏ㄦ爤 | iam, platform |
| contract | 鍚堝悓绠＄悊銆佹潯娆俱€侀檮浠躲€佺画绛句笌缁堟 | 鍏ㄦ爤 | crm, iam |
| workforce | 鍛樺伐妗ｆ銆佸叆绂昏亴銆侀摱琛岃处鎴枫€佷繚闄?| 鍏ㄦ爤 | iam, platform |
| attendance | 鎺掔彮銆佹墦鍗°€佽ˉ鍗″鎵广€佽€冨嫟鍙拌处 | 鍏ㄦ爤 | workforce, contract |
| settlement | 缁撶畻瑙勫垯銆佺粨绠楀崟銆佹槑缁嗐€佽皟鏁村崟 | 鍏ㄦ爤 | attendance, contract |
| billing | 鏀粯鎵规銆佹墦娆捐褰曘€佸箓绛夋帶鍒?| 鍏ㄦ爤 | settlement, iam |
| finance | 鍙戠エ鐢宠銆佸簲鏀跺彴璐︺€佹牳閿€銆佽祫閲戠湅鏉?| 鍏ㄦ爤 | billing, settlement |
| training | 鍩硅璁″垝銆佸涔犺繘搴︺€佽浠惰祫婧?| 鍏ㄦ爤 | workforce |
| performance | KPI/琛屼负鑰冩牳銆佽瘎鍒嗘祦绋嬨€佺粨鏋滄眹鎬?| 鍏ㄦ爤 | workforce, training |
| points | 绉垎瑙勫垯銆佺Н鍒嗘祦姘淬€佹帓琛屼笌婵€鍔?| 鍏ㄦ爤 | workforce, performance |

### 2.2 妯″潡渚濊禆鍥撅紙鏂囧瓧锛?

1. `iam` 涓?`platform` 鏄簳搴фā鍧楋紝鍏朵粬妯″潡缁熶竴澶嶇敤銆? 
2. `crm -> contract -> attendance -> settlement -> billing -> finance` 鏋勬垚鏍稿績璐㈠姟涓婚摼璺€? 
3. `workforce` 浣滀负浜哄姏鍩虹锛岃 `attendance/training/performance/points` 渚濊禆銆? 
4. 璺ㄥ煙鏁版嵁鍚屾浼樺厛閫氳繃棰嗗煙浜嬩欢锛圧abbitMQ锛夊紓姝ヤ紶鎾紝閬垮厤寮鸿€﹀悎鐩磋繛銆? 
5. 绂佹璺ㄦā鍧楃洿鎺ヨ闂?Repository锛屽彧鍏佽閫氳繃 Application Service 鎴?Domain Service銆?

### 2.3 鏈樁娈佃寖鍥存爣娉?

1. 鏈樁娈碉紙Stage 1锛夊凡瀹屾暣瑕嗙洊鏍稿績涓婚摼璺細`iam/crm/contract/workforce/attendance/settlement/billing/finance`銆? 
2. `training/performance/points` 鍦ㄦ灦鏋勫眰宸插畾涔夎竟鐣岋紝浣嗘暟鎹簱涓?API 缁嗗寲寤跺悗鍒?Stage 2 鐨?Phase 鍒掑垎鍚庤惤鍦般€? 
3. 涓洪伩鍏嶆涔夛紝鍚庣画鏂囨。鑻ユ湭瑕嗙洊鎵╁睍妯″潡锛岀粺涓€鏍囪涓?`Out of Scope (Stage 1)`銆?

### 2.4 Phase 2 CRM/Contract 实施边界（2026-02-27）

1. 客户线索（CRM）：
   - 已落地接口：`/api/admin/v1/crm/leads`、`/api/admin/v1/crm/leads/{leadId}/status`、`/api/admin/v1/crm/leads/{leadId}/follow-ups`。
   - 业务边界：线索状态机（NEW/FOLLOWING/WON/LOST）与跟进记录写入/查询。
2. 用工单位（CRM）：
   - 已落地接口：`/api/admin/v1/crm/employer-units`、`/api/admin/v1/crm/employer-units/{unitId}`、`/api/admin/v1/crm/employer-units/{unitId}/deactivate`。
   - 业务边界：单位主数据维护与停用管理，作为合同创建上游输入。
3. 合同生命周期（Contract）：
   - 已落地接口：`/api/admin/v1/contracts/labor-contracts`、`/api/admin/v1/contracts/labor-contracts/{contractId}/sign|renew|terminate`。
   - 业务边界：合同状态机（DRAFT -> SIGNED -> TERMINATED）和日期合法性校验。
4. 结算规则版本管理（Contract）：
   - 已落地接口：`/api/admin/v1/contracts/settlement-rules`、`/api/admin/v1/contracts/settlement-rules/{ruleId}/publish|deactivate`、`/api/admin/v1/contracts/{contractId}/settlement-rules/versions|active`。
   - 业务边界：同合同 `versionNo` 唯一、发布状态唯一性校验与版本历史可追溯。
5. 依赖关系：
   - `labor_contract.employer_unit_id -> employer_unit.id`
   - `settlement_rule.contract_id -> labor_contract.id`
## 3. 鎶€鏈灦鏋?

### 3.1 鍓嶇鏋舵瀯

- 鎶€鏈爤锛歏ue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus銆?
- 璺敱鍒嗗眰锛?
  - `/login`锛氳璇佸叆鍙?
  - `/dashboard`锛氱鐞嗛┚椹惰埍
  - `/crm/*`锛氬鎴蜂笌绾跨储
  - `/contract/*`锛氬悎鍚屼笌缁撶畻瑙勫垯
  - `/workforce/*`锛氬憳宸ヤ笌缁勭粐
  - `/attendance/*`锛氭帓鐝笌鑰冨嫟
  - `/settlement/*`锛氱粨绠椾笌鏀粯
  - `/finance/*`锛氬彂绁ㄣ€佸簲鏀朵笌璧勯噾
  - `/system/*`锛氳鑹叉潈闄愪笌绯荤粺閰嶇疆
- 鐘舵€佺鐞嗭細
  - `authStore`锛氫护鐗屻€佺敤鎴蜂俊鎭€佹潈闄愮偣
  - `appStore`锛氬叏灞€瀛楀吀銆佺鎴蜂笌涓婚淇℃伅
  - `domainStore`锛氬悇涓氬姟鍩熸寜闇€鎷嗗垎锛岄伩鍏嶈秴澶?store
- 缁勪欢瑙勮寖锛?
  - 椤甸潰瀹瑰櫒锛圥age锛? 涓氬姟缁勪欢锛圔iz锛? 閫氱敤缁勪欢锛圕ommon锛?
  - 琛ㄦ牸銆佽〃鍗曘€佺瓫閫夊櫒缁熶竴鎶借薄锛屽噺灏戦〉闈㈤噸澶嶅疄鐜?

### 3.2 鍚庣鏋舵瀯

- 鎶€鏈爤锛歋pring Boot 3.3 + Spring Security + Spring Data JPA + Flyway + Redis + RabbitMQ銆?
- 鍒嗗眰璁捐锛?
  - `controller`锛氭帴鍙ｉ€傞厤涓庡弬鏁版牎楠?
  - `application`锛氱敤渚嬬紪鎺掋€佷簨鍔¤竟鐣?
  - `domain`锛氭牳蹇冧笟鍔¤鍒?
  - `infrastructure`锛氭寔涔呭寲銆佹秷鎭€佸閮ㄩ€傞厤
- 妯垏鑳藉姏锛?
  - 缁熶竴寮傚父澶勭悊鍣紙閿欒鐮?+ traceId锛?
  - 瀹¤鏃ュ織鍒囬潰锛堝叧閿啓鎿嶄綔鍏ㄩ噺璁板綍锛?
  - 鏁版嵁鏉冮檺鎷︽埅鍣紙鎸?`ALL/DEPT/PROJECT/CLIENT/SELF` 娉ㄥ叆杩囨护锛?
  - 骞傜瓑缁勪欢锛坄X-Idempotency-Key`锛?
- 鏃ュ織瑙勮寖锛?
  - JSON 缁撴瀯鍖栨棩蹇楋紝瀛楁鍖呭惈 `traceId/userId/module/action/result`
  - 鏁忔劅瀛楁锛堟墜鏈哄彿銆佽韩浠借瘉銆侀摱琛屽崱锛夋棩蹇楄劚鏁?

### 3.3 閫氫俊鏂瑰紡

- 鍓嶅悗绔細RESTful JSON API锛坄/api/admin/v1`锛夈€?
- 閴存潈锛欽WT Access Token + Refresh Token銆?
- 鍚屾璋冪敤锛欻TTP锛堝唴閮ㄦā鍧楅€氳繃 Service 璋冪敤锛屼笉閫氳繃 HTTP 鍥炵幆锛夈€?
- 寮傛浜嬩欢锛圧abbitMQ Topic锛夛細
  - `settlement.completed`
  - `payment.status.changed`
  - `attendance.abnormal`
  - `invoice.status.changed`
  - `audit.action`

## 4. 瀹夊叏璁捐

- 璁よ瘉锛欽WT + Refresh锛涚煭鏈?access token锛屾敮鎸佷富鍔ㄥけ鏁堬紙Redis 榛戝悕鍗曪級銆?
- 鎺堟潈锛歊BAC + 鏁版嵁鏉冮檺锛堣绾ц繃婊わ級銆?
- 杈撳叆鏍￠獙锛氬墠绔〃鍗曟牎楠?+ 鍚庣 Bean Validation 鍙岄噸鏍￠獙銆?
- 骞傜瓑涓庨槻閲嶏細鍏抽敭鍐欐帴鍙ｅ繀椤绘惡甯?`X-Idempotency-Key`銆?
- 鏁版嵁瀹夊叏锛?
  - 浼犺緭灞?TLS
  - 鏁忔劅瀛楁鍔犲瘑鎴栬劚鏁忥紙韬唤璇併€佹墜鏈哄彿銆侀摱琛屽崱鍙凤級
  - 闄勪欢璁块棶 URL 绛惧悕骞惰缃椂鏁?
- 瀹¤涓庤拷婧細鍏抽敭涓氬姟鍔ㄤ綔鍐欏叆 `audit_log`锛屾敮鎸佹寜瀵硅薄鍜屾搷浣滀汉妫€绱€?

### 4.1 Phase 1 宸茶惤鍦板畨鍏ㄨ竟鐣岋紙2026-02-26锛?

- 璁よ瘉杈圭晫锛?
  - `/api/admin/v1/auth/login`銆乣/refresh`銆乣/me` 宸茶惤鍦般€?
  - token 鏈哄埗鏀寔 access/refresh 涓庝富鍔ㄥけ鏁堛€?
- 鎺堟潈杈圭晫锛?
  - 瑙掕壊鏉冮檺鎺ュ彛宸茶惤鍦帮細`/api/admin/v1/iam/roles`銆乣/roles/{roleId}/permissions`銆?
  - 鏂囨。鍏煎璺敱琛ラ綈锛歚/api/admin/v1/system/roles`銆乣/system/permissions`銆?
- 鏁版嵁鏉冮檺杈圭晫锛?
  - 鏁版嵁鏉冮檺鎷︽埅鍣ㄥ凡娉ㄥ叆 `/api/admin/v1/system/**` 閾捐矾銆?
  - 鏀寔 `ALL/DEPT/SELF/NONE` 绛栫暐锛岄粯璁ゆ嫆缁濊秺鏉冭闂€?
- 瀹¤杈圭晫锛?
  - 鍏抽敭鍐欐搷浣滈€氳繃 `@AuditAction` 璁板綍瀹¤鏃ュ織銆?
  - 鏌ヨ鎺ュ彛 `/api/admin/v1/platform/audit-logs` 宸叉敮鎸佹寜 `bizType/bizId/operatorId` 绛涢€夈€?

## 5. 闈炲姛鑳介渶姹傝惤鍦?

- 鎬ц兘鐩爣锛?
  - API P95 < 300ms
  - API P99 < 800ms
  - 棣栧睆鍔犺浇 < 2.5s
- 鍙敤鎬э細
  - 鐩爣鍙敤鐜?>= 99.9%
  - 鍏抽敭浠诲姟鏀寔澶辫触閲嶈瘯涓庢淇￠槦鍒?
- 鍙墿灞曟€э細
  - 褰撳墠涓烘ā鍧楀寲鍗曚綋
  - 婊¤冻浠ヤ笅浠讳竴鏉′欢瑙﹀彂鏈嶅姟鍖栬瘎浼帮細杩炵画 2 鍛?P99 > 800ms銆佹牳蹇冮槦鍒楃Н鍘?> 10 鍒嗛挓銆佸苟鍙?> 1 涓?

## 6. 鎶€鏈闄?

| 椋庨櫓 | 褰卞搷 | 缂撹В鎺柦 |
|---|---|---|
| Java 鍩虹嚎涓€鑷存€?| Java 17 涓?Java 21 骞跺瓨绛栫暐鑻ヤ笉娓呮櫚浼氬鑷寸幆澧冨垎姝?| 2026-02-26 璧风粺涓€浠?Java 17 浣滀负寮€鍙戜笌 CI 鍩虹嚎锛屽悗缁寜闃舵璇勪及鍗囩骇 21 |
| 鏈湴渚濊禆瀹瑰櫒鍙敤鎬?| 鑻?Docker 渚濊禆鏈惎鍔紝闆嗘垚閾捐矾楠岃瘉浼氫腑鏂?| 2026-02-26 宸插畬鎴?`docker compose up -d` 骞堕€氳繃鍋ュ悍妫€鏌ワ紙MySQL/Redis/RabbitMQ锛?|
| 澶氭潵婧?PRD 鐗堟湰涓嶄竴鑷?| 闇€姹傜悊瑙ｆ紓绉伙紝鎺ュ彛鍙嶅璋冩暣 | Stage 2 鍥哄寲 PRD 涓荤増鏈竻鍗曞苟寤虹珛鍙樻洿鐧昏 |
| 缁撶畻瑙勫垯澶嶆潅搴﹂珮 | 璁＄畻鍙ｅ緞閿欒浼氱洿鎺ュ奖鍝嶈祫閲戝噯纭€?| 鍦?Stage 3 寮哄埗澧炲姞濂戠害娴嬭瘯涓庡璐︽祴璇曚换鍔?|
| 鏁版嵁鏉冮檺绛栫暐閬楁紡 | 瓒婃潈鏌ヨ瀵艰嚧瀹夊叏闂 | 鎺ュ彛灞備笌浠撳偍灞傚弻閲嶆牎楠?+ 瀹夊叏鐢ㄤ緥鍥炲綊 |

## 7. Pattern Library 浣跨敤鎯呭喌

- 宸叉鏌ラ」鐩牴鐩綍 `patterns/`锛氬綋鍓嶄笉瀛樺湪鍙鐢?Pattern銆?
- 鏈樁娈甸噰鐢ㄦ爣鍑嗘ā寮忎綔涓哄垵濮嬪熀绾匡細
  - RBAC + DataScope
  - JWT + Refresh
  - 妯″潡鍖栧崟浣?+ 浜嬩欢瑙ｈ€?

