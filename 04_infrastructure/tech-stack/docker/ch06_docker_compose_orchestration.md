# Chapter 6: Docker Compose & Multi-Container Orchestration — Deep Dive Notes

> **Core Theme:** Defining and running multi-container applications declaratively using **Docker Compose**, **Healthchecks**, and **Isolated Service Networks**.

---

## 1. Multi-Container Orchestration Architecture

Docker Compose translates declarative YAML specifications (`compose.yaml`) into individual Docker networks, volumes, and containers.

```yaml
version: '3.8'

services:
  web:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - app-net

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
    volumes:
      - pg-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-net

networks:
  app-net:
    driver: bridge

volumes:
  pg-data:
```

---

## 2. Service Dependency & Healthcheck Control

### The `depends_on` Flaw without Healthchecks:
Standard `depends_on: [db]` only waits for the database container *process to start*, NOT for the database engine to accept socket connections! Application crashes with `Connection Refused` on boot.

### Solution: `condition: service_healthy`:
- Configure a `healthcheck` in the target service.
- Set `condition: service_healthy` in caller service to delay application startup until database passes health checks!

---

## 3. Network & Volume Isolation in Compose

1. **Automatic Network Isolation:** Compose creates a dedicated user-defined bridge network per project (e.g. `myproject_app-net`). Services outside the stack cannot access internal database ports.
2. **Embedded DNS Interoperability:** Services refer to each other by service name (e.g. `postgres:5432` or `redis:6379`).

---

## 4. Production Compose Command Reference

| Command | Action |
| :--- | :--- |
| **`docker compose up -d`** | Builds, creates, and starts services detached in background. |
| **`docker compose down -v`** | Stops containers, removes networks, and purges named volumes. |
| **`docker compose ps`** | Lists container status, ports, and healthcheck states. |
| **`docker compose logs -f web`**| Streams live logs for specific `web` service. |
