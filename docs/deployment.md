# Deployment Guide

## 1. Overview
- Runtime: Docker Compose
- Services: `frontend`, `backend`, `mysql`, `redis`, `rabbitmq`, `minio`
- Backend uses ContiNew config (`DB_HOST/DB_PORT/DB_USER/DB_PWD/DB_NAME`)

## 2. Required Files
- `docker-compose.yml`
- `.env` (from `.env.example`)
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `frontend/nginx.conf`

## 3. Build & Deploy
```bash
cp .env.example .env
# edit secrets in .env
docker compose up -d --build
docker compose ps
```

## 4. Health Check
```bash
curl -f http://127.0.0.1:${BACKEND_PORT:-8000}/actuator/health
curl -f http://127.0.0.1:${FRONTEND_PORT}
```

## 5. Logs
```bash
docker compose logs --tail=200 backend
docker compose logs --tail=200 frontend
```

## 6. Rollback
1. Keep previous image/package snapshot.
2. Restore `.env` and previous release files.
3. Rebuild and restart:
```bash
docker compose up -d --build
```
