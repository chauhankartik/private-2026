# Chapter 3: SSL/TLS Termination, Keep-Alive & Protocol Multiplexing

Edge proxies terminate encrypted client TLS connections, offload cryptographic handshake CPU overhead, and multiplex application traffic using modern HTTP/2 and gRPC binary protocols.

---

## 1. SSL/TLS Termination & Acceleration

SSL/TLS termination offloads asymmetric key exchange cryptography (RSA / ECDSA) at the edge proxy, allowing internal backend microservices to communicate over plain HTTP/gRPC or dedicated internal mTLS.

```
Client (Internet) ──── TLS 1.3 Encryption ────> [ Edge Proxy (Envoy / NGINX) ]
                                                        |
                                                        | Offloads TLS Handshake
                                                        v Internal Network / Plain HTTP
                                                [ Backend Microservices ]
```

### 1.1 TLS Handshake Optimizations
* **Session Tickets (RFC 5077):** The edge proxy encrypts TLS session parameters into a ticket sent to the client. Upon reconnecting, the client presents the session ticket, skipping asymmetric key negotiation (**1-RTT or 0-RTT Handshake**).
* **OCSP Stapling:** The edge proxy periodically queries the Certificate Authority (CA) for Certificate Revocation List (CRL) status and "staples" the time-stamped CA signature to the initial TLS handshake, saving the client an extra DNS/HTTP lookup.
* **ALPN (Application-Layer Protocol Negotiation):** Allows client and proxy to negotiate target application protocols (e.g. `h2` vs `http/1.1`) directly inside the TLS ClientHello/ServerHello extension.

---

## 2. HTTP Protocol Evolution & Multiplexing

```
HTTP/1.1  ---> 1 Request per TCP Connection (Head-of-Line Blocking per connection)
               [ Req 1 ] ──> [ Resp 1 ] | [ Req 2 ] ──> [ Resp 2 ]

HTTP/2    ---> Binary Framing Layer: Multiplexes N Streams over 1 TCP Connection!
               [ Stream 1 Frame ] [ Stream 3 Frame ] [ Stream 1 Frame ] [ Stream 2 Frame ]

HTTP/3    ---> QUIC over UDP: Independent Streams (Zero TCP Head-of-Line Blocking!)
```

---

## 3. The gRPC Load Balancing Problem

gRPC uses **HTTP/2 as its underlying transport protocol**, opening a single long-lived TCP connection per client.

```
[ gRPC Client ] ─── (Single Long-Lived TCP Connection) ───> [ L4 Load Balancer ]
                                                                   |
                                                                   | Routes ALL TCP Packets
                                                                   v
                                                            [ Backend Node A ]
                                                            (100% Load!)
                                                            [ Backend Node B ]
                                                            (0% Load!)
```

### 3.1 Why Layer 4 Load Balancers Break gRPC
* An L4 load balancer operates at the TCP layer. It routes the initial TCP handshake to **Backend Node A**.
* Because HTTP/2 keeps this single TCP connection open indefinitely, **all subsequent gRPC RPC calls flow exclusively to Node A**, causing massive load imbalance while Node B remains idle!

### 3.2 Resolution: Layer 7 Frame-Level Load Balancing
Layer 7 proxies (Envoy, NGINX) parse HTTP/2 **HEADERS and DATA binary frames**.

```
[ gRPC Client ] ─── HTTP/2 TCP ───> [ L7 Envoy Proxy ] ─── RPC Call 1 ───> [ Backend Node A ]
                                          |
                                          └──────────────── RPC Call 2 ───> [ Backend Node B ]
```

The L7 proxy inspects individual HTTP/2 streams and load balances **individual gRPC RPC calls** across all backend nodes dynamically over a pool of connections.

---

## 4. Staff Engineer Protocol SLA Rules
1. **Never Use Layer 4 Load Balancers for gRPC Services:** Always deploy Layer 7 proxies (Envoy or NGINX with `grpc_pass`) in front of gRPC microservices to ensure frame-level request distribution.
2. **Enable OCSP Stapling & Session Tickets:** Configure `ssl_stapling on;` and `ssl_session_tickets on;` on edge proxies to reduce TLS handshake latencies by up to 50%.
3. **Use ECDSA Certificates Over RSA:** ECDSA keys (e.g. `secp256r1`) provide equivalent security to 3072-bit RSA keys with significantly faster TLS handshake signatures and lower CPU utilization.
