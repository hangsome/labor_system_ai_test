# Deployment Guide

## 1. Overview
- Deployment target: Ubuntu server.
- Runtime: Docker Compose.
- Services: `frontend`, `backend`, `mysql`, `redis`, `rabbitmq`, `minio`.

## 2. Required Files
- `docker-compose.yml`
- `.env` (copied from `.env.example` and filled with production secrets)
- `src/backend/Dockerfile`
- `src/frontend/Dockerfile`
- `src/frontend/nginx.conf`

## 3. Local Packaging
Run on local machine from repository root:

```bash
git archive --format=tar.gz --output labor-system-main.tar.gz main
```

## 4. Server Deployment
On local machine:

```bash
scp labor-system-main.tar.gz ubuntu@<SERVER_IP>:~/labor-system-main.tar.gz
ssh ubuntu@<SERVER_IP>
```

On server:

```bash
mkdir -p ~/labor-system
tar -xzf ~/labor-system-main.tar.gz -C ~/labor-system
cd ~/labor-system
cp .env.example .env
# Edit .env with production secrets
docker compose pull
docker compose up -d --build
docker compose ps
```

## 5. Health Check
```bash
curl -f http://127.0.0.1/
curl -f http://127.0.0.1/api/actuator/health
```

Expected backend response includes:

```json
{"status":"UP"}
```

## 6. Logs
```bash
docker compose logs --tail=200 backend
docker compose logs --tail=200 frontend
```

## 7. Rollback
1. Keep previous release package (e.g. `labor-system-release-previous.tar.gz`).
2. Replace current directory with previous package and rerun:

```bash
docker compose up -d --build
```
