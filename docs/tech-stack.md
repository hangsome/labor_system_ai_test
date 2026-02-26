# 技术栈说明

## 系统概述

劳务系统管理后台，覆盖客户线索、合同、考勤、结算、发票与资金管理的企业级业务链路。

## 技术选型

### 前端

- 框架：Vue `3.5.x`
- 状态管理：Pinia `3.x`
- 路由：Vue Router `5.x`
- UI 库：Element Plus `2.x`
- 构建工具：Vite `7.x`
- 语言：TypeScript `5.x`

### 后端

- 语言：Java `17`（生产目标 `21`）
- 框架：Spring Boot `3.3.11`
- ORM/数据访问：Spring Data JPA
- 迁移：Flyway
- 安全：Spring Security + JWT（后续 Stage 实现）

### 数据与中间件

- 主库：MySQL `8`
- 缓存：Redis `7`
- 消息：RabbitMQ `3`
- 对象存储：MinIO `latest`

### 开发工具

- 版本管理：Git
- 前端包管理：npm
- 后端构建：Maven
- 容器编排：Docker Compose

## 目录结构

```text
docs/
  specification.md
  tech-stack.md
  calicat-inputs.md
src/
  frontend/
  backend/
database/
  init.sql
  migrations/
    V1__baseline.sql
ai-dev-workflow/
docker-compose.dev.yml
.github/workflows/ci-baseline.yml
```

## 开发环境要求

- Node.js: `>= 20`（当前 `v24.11.1`）
- npm: `>= 10`（当前 `11.6.2`）
- JDK: `17+`（推荐 `21`）
- Maven: `3.8+`
- Docker & Docker Compose

## 选型依据（Stage 0 调研）

1. Spring Boot 3.3 官方文档确认 Java 支持范围为 17-23，满足平滑升级到 Java 21。
2. Vue 官方文档建议使用 Vite + TypeScript 进行项目脚手架与生产构建。
3. 当前业务以管理台表单和数据密集交互为主，Element Plus 可缩短开发周期。

