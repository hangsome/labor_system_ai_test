# 劳务系统（ContiNew 4.1.x）

当前仓库只保留正在维护的主代码库、AI 工作流和阶段文档：
- 后端目录：`backend/`
- 前端目录：`frontend/`
- AI 工作流：`ai-dev-workflow/`
- 阶段文档：`phases/`

## 技术栈
- 前端：Vue 3 + Vite + TypeScript + Arco Design（ContiNew UI）
- 后端：Spring Boot 3 + Java 17（ContiNew Admin 4.1.x）
- 数据：MySQL 8 + Redis
- 数据库变更：Liquibase

## 目录
```text
ai-dev-workflow/
backend/
database/
docs/
frontend/
phases/
scripts/
产品原型/
```

## 约定
- 根目录 `process.md` 仅描述仓库主线与状态约定，不再承载阶段执行进度。
- 各阶段的真实状态以 `phases/phase-xx-*/process.md` 和 `todolist.csv` 为准。
- 历史归档代码和根目录旧计划文件已从主仓库清理，避免与当前开发主线混淆。

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
