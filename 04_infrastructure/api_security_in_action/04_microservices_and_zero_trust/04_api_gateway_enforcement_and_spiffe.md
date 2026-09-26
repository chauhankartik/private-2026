# 04. API Gateway Enforcement, Token Exchange & Zero Trust SPIFFE/SPIRE

This chapter explores microservices security patterns, transitioning from perimeter security to a strict **Zero Trust Architecture (ZTA)** using API Gateways, RFC 8693 Token Exchange, SPIFFE/SPIRE identity attestation, and Envoy/OPA sidecar policy enforcement.

---

## 🏛️ Perimeter vs Zero Trust Microservice Architecture

```mermaid
flowchart TD
    subgraph TraditionalPerimeter ["Legacy Castle-and-Moat (Vulnerable)"]
        EdgeWAF["Edge Gateway (AuthN Done Here)"]
        EdgeWAF -->|Plain HTTP / Unauthenticated| MicroA["Service A"]
        MicroA -->|Trust Internal Network| MicroB["Service B"]
        MicroB -->|Trust Internal Network| DB1[(Internal DB)]
    end

    subgraph ZeroTrustMesh ["Modern Zero Trust Architecture (NIST SP 800-207)"]
        GW["API Gateway (Token Verification & Exchange)"]
        
        subgraph PodA ["Microservice Pod A"]
            EnvoyA["Envoy Sidecar (SPIFFE mTLS)"]
            SvcA["Service A Container"]
        end
        
        subgraph PodB ["Microservice Pod B"]
            EnvoyB["Envoy Sidecar + OPA Engine"]
            SvcB["Service B Container"]
        end

        SPIRE["SPIRE Server (Workload Identity Provider)"]

        GW -->|RFC 8693 Scoped Token + mTLS| EnvoyA
        EnvoyA <--->|SPIFFE ID mTLS Check| EnvoyB
        EnvoyA --- SvcA
        EnvoyB --- SvcB
        SPIRE -.->|Issue X.509 SVID| EnvoyA
        SPIRE -.->|Issue X.509 SVID| EnvoyB
    end
```

---

## 🆔 SPIFFE/SPIRE Workload Attestation Flow

**SPIFFE** defines a standard for securely identifying software workloads across heterogeneous environments using URIs (e.g. `spiffe://cluster.local/ns/default/sa/payment-service`). **SPIRE** is the production reference implementation that node agents use to attest node/container identity.

```mermaid
sequenceDiagram
    autonumber
    participant Kernel as Linux Kernel / K8s API
    participant Agent as SPIRE Agent (DaemonSet)
    participant Workload as Microservice Workload
    participant Server as SPIRE Server (Root CA)

    Workload->>Agent: Request SVID over Unix Domain Socket (`/tmp/spire-agent/public/api.sock`)
    Agent->>Kernel: Query Process PID / Cgroup / K8s Pod ServiceAccount
    Kernel-->>Agent: Process Metadata (UID: 1000, Pod: payment-service-7f4d, SA: payment-sa)
    
    Note over Agent: Compare Process Metadata against SPIRE Workload Registration Entries
    Agent->>Server: Request X.509 SVID for `spiffe://example.org/ns/prod/sa/payment-sa`
    Server-->>Agent: Issued Short-Lived X.509 SVID (Valid for 1 hour)
    Agent-->>Workload: Deliver Certificate + Private Key in Memory (Zero Disk Storage)
```

---

## 🔀 RFC 8693 OAuth 2.0 Token Exchange

When an end-user sends a high-privilege access token to an API Gateway, the Gateway should **NOT** forward the original user token directly to downstream microservices. Doing so violates the principle of least privilege and enables token replay attacks across internal services.

Instead, the API Gateway exchanges the user token for a **Downstream Scoped Token** via RFC 8693.

```http
POST /oauth/v2/token HTTP/1.1
Host: auth.company.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic QVBJR2F0ZXdheUNsaWVudElEOlNlY3JldA==

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Atoken-exchange
&subject_token=eyJhbGciOiJSUzI1NiIs... (Original User Access Token)
&subject_token_type=urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Aaccess_token
&requested_token_type=urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Aaccess_token
&audience=urn:company:service-order-fulfillment
&scope=orders:write
```

### 🐍 Python API Gateway RFC 8693 Token Exchange Implementation

```python
import requests
from typing import Dict, Any

class TokenExchangeGatewayClient:
    """Production Gateway Token Exchange Client (RFC 8693)."""

    def __init__(self, token_endpoint: str, client_id: str, client_secret: str):
        self.token_endpoint = token_endpoint
        self.client_id = client_id
        self.client_secret = client_secret

    def exchange_user_token_for_service_token(self, user_access_token: str, target_audience: str, scope: str) -> Dict[str, Any]:
        """
        Exchanges an inbound user token for a short-lived, audience-scoped token for microservice communication.
        """
        payload = {
            "grant_type": "urn:ietf:params:oauth:grant-type:token-exchange",
            "subject_token": user_access_token,
            "subject_token_type": "urn:ietf:params:oauth:token-type:access_token",
            "requested_token_type": "urn:ietf:params:oauth:token-type:access_token",
            "audience": target_audience,
            "scope": scope
        }

        response = requests.post(
            self.token_endpoint,
            data=payload,
            auth=(self.client_id, self.client_secret),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            timeout=5
        )

        if response.status_code != 200:
            raise RuntimeError(f"Token exchange failed ({response.status_code}): {response.text}")

        return response.json() # Returns {"access_token": "...", "issued_token_type": "...", "expires_in": 300}
```

---

## 🛡️ Sidecar Authorization: Envoy Proxy + OPA (Open Policy Agent)

Decouple authorization policy logic from microservice code by running Open Policy Agent (OPA) alongside Envoy as an `ext_authz` sidecar filter.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Service A (mTLS)
    participant Envoy as Envoy Sidecar (Service B)
    participant OPA as Open Policy Agent Engine
    participant SvcB as Service B App Container

    Client->>Envoy: HTTP POST /api/v1/payments (Headers + SPIFFE Cert + Scoped JWT)
    Envoy->>OPA: Check Authorization (gRPC CheckRequest payload)
    
    Note over OPA: Evaluate Rego Policy against SPIFFE ID + JWT Scopes + HTTP Method
    OPA-->>Envoy: Authorization Decision (Allowed: true)
    
    Envoy->>SvcB: Forward HTTP Request to localhost:8080
    SvcB-->>Envoy: 200 OK Response
    Envoy-->>Client: 200 OK Response
```

### 📜 Open Policy Agent (OPA) Rego Policy (`policy.rego`)

```rego
package envoy.http.authorization

import future.keywords.in

default allowed = false

# Allow request if all conditions pass
allowed {
    is_valid_spiffe_identity
    is_valid_http_method
    has_required_scope
}

# 1. Assert Client Peer Certificate SPIFFE ID matches authorized caller list
is_valid_spiffe_identity {
    client_spiffe_id := input.attributes.source.principal
    client_spiffe_id == "spiffe://company.org/ns/prod/sa/order-service-sa"
}

# 2. Allow POST methods to payments endpoint
is_valid_http_method {
    input.attributes.request.http.method == "POST"
    input.attributes.request.http.path == "/api/v1/payments"
}

# 3. Verify JWT scope in request header
has_required_scope {
    auth_header := input.attributes.request.http.headers.authorization
    startswith(auth_header, "Bearer ")
    token := substring(auth_header, 7, -1)
    [_, payload, _] := io.jwt.decode(token)
    "payments:write" in split(payload.scope, " ")
}
```
