---
description: 澶?Agent 璺敱瑙勫垯涓庨檷绾х瓥鐣?
version: "2.0"
---

# 澶?Agent 璺敱瑙勫垯

鏈枃浠跺畾涔変簡 AI 澶у瀷绯荤粺寮€鍙戝伐浣滄祦涓 Agent 鐨勫垎宸ャ€佽矾鐢卞拰闄嶇骇绛栫暐銆傛敮鎸併€屽崟 Agent 涓茶銆嶅拰銆屽 Agent 骞惰銆嶄袱绉嶆ā寮忋€?

---

## 涓€銆丄gent 瑙掕壊瀹氫箟

### Codex锛堜富缂栨帓 + 鍚庣寮€鍙戯級

**鑱岃矗**锛?
- 宸ヤ綔娴佸叏灞€缂栨帓锛圫tage 娴佽浆銆佽繘搴︾鐞嗭級
- 鍚庣浠ｇ爜寮€鍙戯紙Java / Python / Node.js锛?
- 鏁版嵁搴撹璁′笌杩佺Щ鑴氭湰
- API 寮€鍙戜笌鑱旇皟
- 闆嗘垚娴嬭瘯涓?E2E 娴嬭瘯鍗忚皟
- CSV 鐘舵€佺鐞嗕笌 process.md 鏇存柊

**韬唤**锛氫富 Agent锛屾墍鏈夊伐浣滄祦鎺у埗鏉冨綊 Codex銆?

### Gemini锛堝墠绔紑鍙戯級

**鑱岃矗**锛?
- React / Vue 缁勪欢寮€鍙?
- UI/UX 瀹炵幇锛堥〉闈㈠竷灞€銆佹牱寮忋€佸姩鐢伙級
- CSS / Sass / Less 缂栧啓
- 鍓嶇璺敱鍜岀姸鎬佺鐞?
- 鍓嶇缁勪欢娴嬭瘯

**璋冪敤鏂瑰紡**锛氱洿鎺ラ€氳繃鏈湴 Gemini CLI 璋冪敤锛堜笉渚濊禆 API锛屼娇鐢ㄦ湰鍦?`gemini` 鍛戒护琛屽伐鍏凤級锛?
```bash
gemini -p "<task-description>" \
  --sandbox none
```
鎴栦互浜や簰妯″紡鍦ㄩ」鐩洰褰曚笅鍚姩锛?
```bash
cd <project-root>
gemini
```
> 娉ㄦ剰锛氫笉浣跨敤 `collaborating-with-gemini-cli` skill锛堝畠闇€瑕?API 鎺ュ叆锛夛紝鑰屾槸鐩存帴鎷夎捣鏈湴宸插畨瑁呯殑 Gemini CLI銆?

**闄嶇骇鏉′欢**锛欸emini 杩斿洖 429 / 闄愭祦 / 浠绘剰闈炴垚鍔熺姸鎬佺爜 鏃讹紝绔嬪嵆闄嶇骇銆?

### Claude锛堝璁?+ 璇勫锛?

**鑱岃矗**锛?
- Plan 璇勫锛堥樆鏂棬 鈥?Stage 1 鏋舵瀯璇勫蹇呯粡锛?
- 浠ｇ爜瀹夊叏瀹¤锛堟瘡涓?Phase 瀹屾垚鍚庯級
- Code Review锛坮eview 绫诲瀷浠诲姟锛?
- 鍥炲綊娴嬭瘯瀹℃煡
- 椋庨櫓璇勪及涓庣紦瑙ｅ缓璁?

**璋冪敤鏂瑰紡**锛氶€氳繃 Claude 瀛愪唬鐞嗭紙缁堢鎴?API锛夈€?

**瑕佹眰**锛氭瘡娆¤皟鐢?Claude 鍓嶏紝纭鍏跺浜?Opus 妯″紡銆?

---

## 浜屻€佽矾鐢卞喅绛栫煩闃?

### 鎸変换鍔＄被鍨嬭矾鐢?

| area | task_type | 涓?Agent | 闄嶇骇 Agent | 澶囨敞 |
|------|-----------|---------|-----------|------|
| `backend` | `backend` | Codex | 鈥?| 涓嶉檷绾?|
| `backend` | `database` | Codex | 鈥?| DDL/杩佺Щ |
| `backend` | `api` | Codex | 鈥?| API 寮€鍙?|
| `backend` | `config` | Codex | 鈥?| 閰嶇疆鏂囦欢 |
| `frontend` | `frontend` | Gemini | Claude 鈫?Codex | 429 闄嶇骇閾?|
| `frontend` | `test-unit` | Gemini | Claude 鈫?Codex | 鍓嶇娴嬭瘯 |
| `both` | `test-integration` | Codex | 鈥?| 闆嗘垚娴嬭瘯 |
| `both` | `test-e2e` | Codex | 鈥?| E2E 娴嬭瘯 |
| `*` | `review` | Claude | 鈥?| 浠ｇ爜瀹℃煡 |
| `*` | `docs` | Codex | 鈥?| 鏂囨。鏇存柊 |

### 鎸?Stage 璺敱

| Stage | 涓?Agent | 杈呭姪 Agent |
|-------|---------|-----------|
| Stage 0: 椤圭洰鍒濆鍖?| Codex | 鈥?|
| Stage 1: 鏋舵瀯璁捐 | Codex + sequential-thinking | Claude锛堣瘎瀹￠棬锛?|
| Stage 2: 闃舵瑙勫垝 | Codex | 鈥?|
| Stage 3: 浠诲姟鍒嗚В | Codex | 鈥?|
| Stage 4: 鎵ц锛堝悗绔級 | Codex | 鈥?|
| Stage 4: 鎵ц锛堝墠绔級 | Gemini | Claude锛堥檷绾э級 |
| Stage 4: 鎵ц锛堝鏌ワ級 | Claude | 鈥?|
| Stage 5: 瀹℃煡浜ゆ帴 | Claude锛堝璁★級 | Codex锛堟姤鍛婏級 |

### 澶?Agent 骞惰妯″紡锛堟寜鐙珛浼氳瘽璺敱锛?

> 褰撶敤鎴蜂互瑙掕壊鍏抽敭璇嶈繘鍏ユ椂鐢熸晥銆傝缁嗗崗璁 `multi-agent-protocol.md`銆?

| 瑙掕壊 | 鐙珛浼氳瘽 | 璐熻矗 Stage | Git 鍒嗘敮 | 鍙搷浣滅洰褰?|
|------|---------|-----------|---------|----------|
| Orchestrator | Codex / Antigravity | 0/1/2/3 + 5 鍐崇瓥 + 6 | `main` | `docs/`銆乣phases/`銆侀厤缃?|
| Backend Engineer | Codex / Antigravity | 4锛坅rea=backend锛?| `feature/phase-XX-<slug>` | `backend/`銆乣database/` |
| Frontend Engineer | Gemini / Antigravity | 4锛坅rea=frontend锛?| `feature/phase-XX-<slug>-fe` | `frontend/` |
| Audit Engineer | Claude Opus | 5锛堝璁★級 | 鍙鎵€鏈夊垎鏀?| 鍙啓 `review/` |

**骞惰绐楀彛瑙勫垯**锛欱ackend 濮嬬粓棰嗗厛 Frontend **1 涓?Phase**锛岀‘淇?Frontend 鎬绘湁绋冲畾鐨?API 鍙鎺ャ€?

**浼氳瘽鍚姩鎸囦护**锛?
- Backend锛氥€屼綘鏄悗绔伐绋嬪笀璇风户缁伐浣溿€?
- Frontend锛氥€屼綘鏄墠绔伐绋嬪笀璇风户缁伐浣溿€?
- Audit锛氥€屼綘鏄璁″伐绋嬪笀璇峰紑濮嬪鏌ャ€?

---

## 涓夈€侀檷绾х瓥鐣?

### Gemini 闄嶇骇閾?

```
Gemini (涓? 
  鈫?429/闄愭祦/闈炴垚鍔熻繑鍥炵爜
Claude (绗竴闄嶇骇)
  鈫?涓嶅彲鐢?
Codex (鏈€缁堥檷绾э紝鐢卞綋鍓?Agent 鐩存帴鎵ц)
```

**闄嶇骇瑙﹀彂鏉′欢**锛?
- HTTP 鐘舵€佺爜 429 (Too Many Requests)
- HTTP 鐘舵€佺爜 439 鎴栦换浣?4xx/5xx
- 瓒呮椂锛堥粯璁?1800 绉掞級
- 缃戠粶杩炴帴澶辫触

**闄嶇骇鍚庤涓?*锛?
1. 鍦?CSV 鐨?`notes` 瀛楁璁板綍锛歚gemini_fallback:<鍘熷洜>`
2. 灏?`assigned_agent` 鏇存柊涓哄疄闄呮墽琛岀殑 Agent
3. **涓嶇瓑寰?Gemini 鎭㈠**锛岀珛鍗充娇鐢ㄩ檷绾?Agent 缁х画鎵ц
4. 鍚庣画鍚岀被浠诲姟涔熶娇鐢ㄩ檷绾?Agent锛堢洿鍒颁笅涓€娆′細璇濆紑濮嬫椂閲嶈瘯 Gemini锛?

### Claude 涓嶅彲鐢ㄥ鐞?

濡傛灉 Claude 涓嶅彲鐢紝鎸変换鍔＄被鍨嬪垎娴侊細

1. **闃绘柇闂ㄤ换鍔★紙涓嶅彲闄嶇骇锛?*  
   - 鍖呮嫭锛歋tage 1 鏋舵瀯璇勫銆丼tage 5 鍙戝竷鍓嶅璁°€佹墍鏈夋爣璁颁负 `review_gate=blocking` 鐨勪换鍔°€? 
   - 澶勭悊锛氱珛鍗虫寕璧凤紝璁板綍 `claude_unavailable:blocked`锛屽苟閫氱煡鐢ㄦ埛杞汉宸ュ鏌ャ€? 
   - 瑙勫垯锛?*绂佹**鐢?Codex 鑷浠ｆ浛闃绘柇闂ㄣ€?

2. **闈為樆鏂鏌ヤ换鍔★紙鍙檷绾э級**  
   - 鍙敱 Codex 鎵ц涓存椂鑷锛岃褰?`claude_unavailable:self_review_non_blocking`銆? 
   - 鍚庣画蹇呴』鍦?Claude 鎭㈠鍚庤ˉ涓€娆℃寮忓璁″苟鏇存柊缁撹銆?

3. **鍓嶇浠诲姟**  
   - Gemini 涓嶅彲鐢ㄤ笖 Claude 涔熶笉鍙敤鏃讹紝鍏佽鐢?Codex 鍏滃簳鎵ц銆? 
   - 浣嗚嫢璇ヤ换鍔″睘浜庡彂甯冮樆鏂棬锛屼粛闇€绛夊緟浜哄伐鎴?Claude 瀹¤閫氳繃銆?

---

## 鍥涖€佽皟鐢ㄨ褰曡姹?

姣忔 Agent 璋冪敤蹇呴』鍦?`process.md` 鐨勬墽琛屾棩蹇椾腑璁板綍锛?

```
<鏃堕棿> [gemini] 寮€濮嬫墽琛?PH01-040: 鐧诲綍椤甸潰缁勪欢
<鏃堕棿> [gemini] 杩斿洖 429, 闄嶇骇涓?claude
<鏃堕棿> [claude] 瀹屾垚 PH01-040, commit: abc1234
```

姣忔浼氳瘽缁撴潫鏃讹紝鍦?`process.md` 搴曢儴姹囨€诲悇 Agent 浣跨敤鎯呭喌锛?

```
## Agent 浣跨敤缁熻
| Agent | 浠诲姟鏁?| 鎴愬姛 | 闄嶇骇 |
|-------|-------|------|------|
| codex | 15 | 15 | 0 |
| gemini | 8 | 6 | 2 |
| claude | 5 | 5 | 0 |
```

---

## 浜斻€丆odex 鐗规湁瑙勫垯

浣滀负涓荤紪鎺?Agent锛孋odex 璐熻矗锛?

1. **娴佺▼鎺у埗**锛氬喅瀹氬綋鍓嶅浜庡摢涓?Stage锛屼綍鏃惰繘鍏?Stage 杞崲
2. **浠诲姟璋冨害**锛氬喅瀹?CSV 涓换鍔＄殑鎵ц椤哄簭
3. **鐘舵€佺鐞?*锛氭洿鏂?CSV 鍜?process.md
4. **Agent 璋冨害**锛氳皟鐢?Gemini 鍜?Claude
5. **寮傚父澶勭悊**锛氬鐞?Agent 闄嶇骇銆侀樆濉炪€佸け璐ョ瓑寮傚父鎯呭喌

Codex 涓嶅簲灏嗕互涓婅亴璐ｅ鎵樼粰鍏朵粬 Agent銆?

---

## 鍏€佽法骞冲彴鍏煎璇存槑

### 鍦?Antigravity 涓?

- Antigravity 鑷韩鎵挎媴 Codex 鐨勮鑹?
- 閫氳繃 `browser_subagent` 宸ュ叿娴嬭瘯鍓嶇
- Claude 閫氳繃绯荤粺鍐呯疆鑳藉姏璋冪敤
- Gemini 閫氳繃鏈湴 Gemini CLI 鐩存帴璋冪敤

### 鍦?Claude 涓?

- Claude 鑷韩鎵挎媴 Codex + Claude 鐨勮鑹诧紙鑷紪鎺?+ 鑷鏌ワ級
- Gemini 閫氳繃鏈湴 Gemini CLI 鐩存帴璋冪敤
- 鑷鏌ユ椂闇€鍦?`notes` 鏍囨敞 `self_review:true`

