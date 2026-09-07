# Chapter 4: Dockerfile Optimization & Multi-Stage Builds — Deep Dive Notes

> **Core Theme:** Minimizing image size, optimizing Build Cache layer Ordering, multi-stage builds, and enforcing non-root container security.

---

## 1. Build Context & Layer Caching Mechanics

Each instruction in a Dockerfile creates an immutable, read-only layer.

```
                  CACHE INVALIDATION WORKFLOW
                  
Step 1: FROM node:18-alpine              [CACHED]
Step 2: WORKDIR /app                     [CACHED]
Step 3: COPY package*.json ./            [CACHED]
Step 4: RUN npm install                  [CACHED]
Step 5: COPY . .                         [CACHE MISS! - File modified]
Step 6: RUN npm run build                [CACHE INVALIDATED - Must re-run!]
```

### Build Cache Rules:
1. Docker checks if file contents match previous cache checksums for `COPY` and `ADD`.
2. **Chain Invalidation:** If a single step suffers a **Cache Miss**, Docker invalidates the build cache for **ALL SUBSEQUENT STEPS**!
3. **Golden Ordering Rule:** Place static dependencies (`package.json`, `pom.xml`) at the top; place active source code near the bottom.

---

## 2. Multi-Stage Builds (Shrinking Images from 1GB to 15MB)

Multi-stage builds use multiple `FROM` statements in a single Dockerfile to discard build tools (compilers, SDKs, build caches) from final runtime image.

```dockerfile
# --- Stage 1: Build Stage ---
FROM golang:1.20-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o server .

# --- Stage 2: Minimal Runtime Stage ---
FROM scratch
WORKDIR /app
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=builder /app/server .
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["./server"]
```

### Impact Comparison:
- Single-Stage Image (`golang:1.20` base): **~850 MB**
- Multi-Stage Image (`scratch` / Distroless base): **~12 MB**

---

## 3. Production Dockerfile Checklist

1. **Never Run as Root:** Add non-root user (`RUN adduser -D appuser && USER appuser`).
2. **Clean Package Manager Caches:** `apt-get update && apt-get install -y --no-install-recommends pkg && rm -rf /var/lib/apt/lists/*`.
3. **Use `.dockerignore`:** Exclude `.git`, `node_modules`, `.env`, `target/`, and logs.
4. **Use Explicit Version Tags:** Avoid `node:latest`; specify pinned digest `node:18.16.0-alpine`.
5. **Use Exec Form for `ENTRYPOINT` / `CMD`:**
   - **Exec Form (Correct):** `ENTRYPOINT ["node", "app.js"]` (Executes as PID 1, receiving `SIGTERM` shutdown signals).
   - **Shell Form (Anti-Pattern):** `ENTRYPOINT node app.js` (Spawns `/bin/sh -c`, swallowing shutdown signals).
