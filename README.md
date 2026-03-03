# 劳务系统（ContiNew 4.1.x 迁移版）

本仓库已完成 Phase01 + Phase02 到 ContiNew 4.1.x 的脚手架切换：
- 后端目录：`backend/`
- 前端目录：`frontend/`
- 旧实现归档：`archive/src-legacy-phase12-20260303/`

## 技术栈
- 前端：Vue 3 + Vite + TypeScript + Arco Design（ContiNew UI）
- 后端：Spring Boot 3 + Java 17（ContiNew Admin 4.1.x）
- 数据：MySQL 8 + Redis
- 数据库变更：Liquibase

## 目录
```text
backend/
frontend/
archive/
  src-legacy-phase12-20260303/
database/
docs/
phases/
scripts/
```

## 本地启动
1. 启动依赖服务
```bash
docker compose -f docker-compose.dev.yml up -d
```

2. 启动后端（端口 `8000`）
```bash
cd backend
mvn -B -pl continew-server -am spring-boot:run
```

3. 启动前端（端口 `5173`）
```bash
cd frontend
pnpm install
pnpm dev
```

## Docker 启动
```bash
docker compose up -d --build
```

## 验证
- 后端健康检查：`http://127.0.0.1:8000/actuator/health`
- 前端地址：`http://127.0.0.1:${FRONTEND_PORT}`
