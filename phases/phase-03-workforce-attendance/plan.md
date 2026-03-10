# Phase 03: Workforce & Attendance

## 目标
- 建立员工档案与默认银行卡维护能力
- 建立合同派遣、排班、考勤记录三层数据链路
- 建立补卡申请与审批回写闭环

## 实施结果
- 后端已在 `continew-extension-labor` 中新增 5 组 controller/service/model/mapper
- 数据库已在 Liquibase 中新增 6 张 `labor_` 前缀表，并补齐菜单与权限
- 前端已新增 5 个 labor 页面和对应 API/types/constants

## 验证结果
- `mvn -pl continew-extension/continew-extension-labor -am -DskipTests compile`
- `pnpm typecheck`
- `pnpm build`

## 后续
- 合并到 `main`
- 启动前后端后做一次人工验收
