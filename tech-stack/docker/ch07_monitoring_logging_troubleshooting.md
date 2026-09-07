# Chapter 7: Logging, Resource Monitoring & Low-Level Troubleshooting — Deep Dive Notes

> **Core Theme:** Configuring **Logging Drivers**, inspecting **cgroups Resource Usage**, and executing low-level namespace debugging using **`nsenter`**.

---

## 1. Docker Logging Drivers & Rotation

By default, Docker captures container `stdout` and `stderr` streams using the `json-file` driver.

### Preventing Disk Full Outages (Log Rotation):
Without log rotation configuration, runaway application logs can consume 100% of host disk space!

```json
// /etc/docker/daemon.json (Global Engine Log Rotation)
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

---

## 2. Resource Inspection (`docker stats` & cgroups)

```bash
# Stream real-time container resource usage:
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
```

### Direct Kernel cgroup Inspection:
Docker reads metrics directly from kernel cgroup pseudo-filesystems:
- **cgroups v1:** `/sys/fs/cgroup/memory/docker/<container_id>/memory.usage_in_bytes`
- **cgroups v2:** `/sys/fs/cgroup/docker/<container_id>/memory.current`

---

## 3. Low-Level Troubleshooting with `nsenter`

### The Distroless / Scratch Debugging Problem:
Minimal production containers (`FROM scratch` or Distroless) do NOT contain `bash`, `sh`, `curl`, or `netstat`. `docker exec -it <container> bash` fails!

### The `nsenter` Solution:
`nsenter` (namespace enter) attaches host tools directly into the target container's kernel namespaces.

```bash
# Step 1: Find container PID on Host OS
CONTAINER_PID=$(docker inspect --format '{{.State.Pid}}' my_container)

# Step 2: Enter container Network (-n) and Mount (-m) namespaces using HOST bash/tcpdump:
sudo nsenter -t $CONTAINER_PID -n -m netstat -tuln
sudo nsenter -t $CONTAINER_PID -n tcpdump -i eth0 port 8080
```

---

## 4. Container Troubleshooting Command Cheat-Sheet

| Objective | Command |
| :--- | :--- |
| **Inspect Container JSON State** | `docker inspect <container_id>` |
| **View Tail Logs** | `docker logs --tail 100 -f <container_id>` |
| **Inspect OOM Kill Event** | `docker inspect --format '{{.State.OOMKilled}}' <container>` |
| **Analyze Filesystem Changes** | `docker diff <container_id>` |
| **Enter PID/NET Namespace** | `sudo nsenter -t <PID> -n -p bash` |
