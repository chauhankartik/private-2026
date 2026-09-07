# System Design 5: Real-Time Chat System (WhatsApp / Slack)

## 1. Problem Statement & Requirements

Design a real-time messaging application (like WhatsApp, Slack, or WeChat) supporting 1-on-1 chat, group chat, online presence, and read receipts.

### Functional Requirements:
* Low-latency 1-on-1 and Group Messaging ($< 100\text{ms}$ delivered).
* Real-time Online / Offline user status indicator.
* Message History & Sync across multiple devices.
* Push Notifications for offline users.

### Non-Functional Requirements:
* Scale: 50 Million Active Users (1 Billion messages sent/day).
* High Availability & Zero Message Loss (100% durability).
* End-to-End Encryption (E2EE) support.

---

## 2. Real-Time Transport Protocols

```
+-----------------------------------------------------------------------------------+
| Communication Protocol Comparison                                                 |
+-------------------+-----------------------+-------------------+-------------------+
| Protocol          | Connection Type       | Direction         | Use Case          |
+-------------------+-----------------------+-------------------+-------------------+
| HTTP Long Polling | Ephemeral HTTP        | Client Pull       | Legacy Web        |
| WebSockets        | Persistent Full-Duplex| Bi-Directional    | Real-Time Chat    |
| HTTP/2 SSE        | Persistent Connection | Server Push Only  | Ticker Feeds      |
+-------------------+-----------------------+-------------------+-------------------+
```
* **Selected Protocol:** **WebSockets**. After an initial HTTP handshake, a long-lived, full-duplex TCP WebSocket connection is established between client devices and WebSocket Gateway Servers.

---

## 3. High-Level Chat System Blueprint

```
 Client A (Online)                                WebSocket Gateway Cluster                                Client B (Online)
      |                                                      |                                                    |
 1. WebSocket Send Msg ------------------------------------->|                                                    |
                                                             | 2. Route Msg to Client B Connection                |
                                                             |--------------------------------------------------->|
                                                             |                                                    |
                                                             | 3. If Client B Offline:                            |
                                                             |    Send to Push Notification Service (APNs/FCM)    |
                                                             |                                                    |
                                                             | 4. Async Append Message to Message Store           |
                                                             v                                                    v
                                                +-----------------------------------+
                                                | Cassandra / ScyllaDB Store        |
                                                | Partition Key: (chat_id)          |
                                                | Clustering Key: (message_id DESC) |
                                                +-----------------------------------+
```

---

## 4. Message Storage Schema & Presence Architecture

### Cassandra / ScyllaDB Message Schema:
```sql
CREATE TABLE chat_messages (
    chat_id uuid,
    message_id timeuuid,
    sender_id bigint,
    message_text text,
    media_url text,
    PRIMARY KEY (chat_id, message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);
```
* **Why Wide-Column Store (Cassandra)?** Chat messages are append-only sequential writes. Cassandra handles high write throughput ($> 100,000 \text{ writes/sec}$) with ultra-fast sequential SSTable disk appends.

### Online Presence System (Redis Heartbeats):
* Clients send a heartbeat ping every 5 seconds to a **Presence Service**.
* The Presence Service updates Redis key `presence:user_101` with a `TTL = 10` seconds. If no ping arrives within 10s, the key expires, automatically marking the user `Offline`.
