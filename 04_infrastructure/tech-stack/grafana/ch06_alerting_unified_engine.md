# Chapter 6: Unified Alerting Engine Architecture

Grafana **Unified Alerting** combines query evaluation across multi-cloud data sources with an embedded Alertmanager routing engine for centralized incident response.

---

## 1. Unified Alerting Execution Flow

```
[ Evaluation Scheduler ] ---> Tickers every 1 minute
       |
       v
[ Server-Side Expression Engine ]
       ├── 1. Queries Data Sources (Prometheus, Loki, SQL)
       ├── 2. Executes Reduce / Math Expressions
       └── 3. Evaluates Threshold Condition (e.g., CPU > 85%)
       |
       v
[ State Manager ]
       ├── Condition Met?  ===> State: Pending (Waits for `for` duration, e.g. 5m)
       ├── Still Met after 5m? ===> State: Firing / Alerting
       └── Condition Cleared?  ===> State: Normal / OK
       |
       v Firing Alert Instance
[ Embedded Alertmanager ]
       ├── Grouping & Deduplication (Combines 50 alerts into 1 Slack message)
       ├── Inhibition Rules (Mutes secondary alerts if primary host is DOWN)
       └── Notification Policies (Routes via label matchers to Contact Points)
       |
       v
[ Contact Points ] (PagerDuty, Slack, Webhook, Opsgenie)
```

---

## 2. Alertmanager Routing & Grouping Mechanics

### 2.1 Notification Policies (Routing Tree)
Notification policies use a tree structure to match alert labels and determine which **Contact Point** receives the notification.

```yaml
# Notification Routing Tree Example
routes:
- matchers: [ severity = "critical" ]
  receiver: "PagerDuty-OnCall"
  routes:
  - matchers: [ team = "payments" ]
    receiver: "Payments-PagerDuty"
- matchers: [ severity = "warning" ]
  receiver: "Slack-Alerts-Channel"
```

### 2.2 Grouping Variables
* `group_by`: Array of label names (e.g., `['cluster', 'alertname']`). Alerts sharing these labels are aggregated into a single payload.
* `group_wait`: Initial buffer delay (e.g., `30s`) to wait for sibling alerts before firing the first notification.
* `group_interval`: Wait time (e.g., `5m`) before sending a batch update for newly added alerts in an existing group.
* `repeat_interval`: Re-notification period (e.g., `4h`) for unacknowledged, continuously firing alerts.

---

## 3. Silence & Inhibition Rules

### 3.1 Silences
Temporarily mutes alert notifications matching label patterns for scheduled maintenance windows without disabling the underlying alert rules.

### 3.2 Inhibition Rules
Mutes an alert (Target) if another alert (Source) is already actively firing.

```yaml
inhibit_rules:
- source_matchers: [ alertname = "NodeDown" ]
  target_matchers: [ alertname = "InstancePodMemoryHigh" ]
  equal: [ "node" ]  # Mutes Pod memory alerts if the entire Node is DOWN!
```

---

## 4. Staff Engineer Alerting Best Practices
1. **Always Set `for` Duration:** Avoid `for: 0s` on noisy metrics. Set a reasonable `for` duration (e.g. 3m - 5m) to allow transient spikes to settle, preventing false-positive page alerts.
2. **Alert on Symptoms, Not Causes:** Page on user-impacting symptoms (e.g., High Error Rate $5xx > 1\%$, P99 Latency $> 2s$) rather than underlying causes (High CPU usage).
3. **Enforce Grouping to Prevent Alert Storms:** Configure `group_by: ['alertname', 'cluster', 'service']` to aggregate cascading failure alerts into a single actionable PagerDuty incident.
