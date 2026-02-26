# 劳务系统（Stage 0 骨架）

本仓库按 `ai-dev-workflow` 工作流完成了 Stage 0 初始化，目标是搭建劳务系统后台版开发基线。

## 技术栈

- 前端：Vue 3 + Vite + TypeScript + Vue Router + Pinia + Element Plus
- 后端：Spring Boot 3.3 + Java 17（生产目标 Java 21）
- 数据：MySQL 8 + Redis + RabbitMQ + MinIO
- 迁移：Flyway

## 目录

```text
docs/
src/
  frontend/
  backend/
database/
  migrations/
ai-dev-workflow/
docker-compose.dev.yml
```

## 本地启动

1. 启动依赖服务：
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```
2. 启动后端：
   ```bash
   cd src/backend
   mvn spring-boot:run
   ```
3. 启动前端：
   ```bash
   cd src/frontend
   npm install
   npm run dev
   ```

## 质量基线

- CI：`.github/workflows/ci-baseline.yml`
- 文档：`docs/specification.md`、`docs/tech-stack.md`
- 阶段跟踪：`todolist.csv`、`process.md`

