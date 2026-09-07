# Chapter 6: Security, RBAC, Admission Control & NetworkPolicies

Securing a Kubernetes cluster requires defense-in-depth across API access control, pod runtime boundaries, dynamic admission policy engines, and network isolation.

---

## 1. Authentication & RBAC Authorization

Every request entering `kube-apiserver` must pass through sequential Authentication and Authorization modules.

```
Request ---> [ 1. Authentication ] ---> [ 2. Authorization (RBAC) ] ---> [ 3. Admission Control ]
```

### 1.1 Authentication (AuthN)
Identifies the subject (`User`, `Group`, or `ServiceAccount`):
* **X.509 Client Certificates:** Used for cluster admin access (`CN=username, O=group`).
* **ServiceAccount JWT Tokens:** Projected volume tokens injected into Pods at `/var/run/secrets/kubernetes.io/serviceaccount/token`. Signed using RS256 with OIDC discovery support.
* **OIDC Providers:** Integration with enterprise identity providers (Okta, Keycloak, Dex).

### 1.2 Role-Based Access Control (RBAC)
Authorization is granted by binding Roles to Subjects via RoleBindings.

```
Namespace Scope:   [ Role ]          <--- RoleBinding --->        [ Subject ]
                                                               (User / ServiceAccount)
Cluster Scope:     [ ClusterRole ]   <--- ClusterRoleBinding ---> [ Subject ]
```

**RBAC Rule Structure:**
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: production
rules:
- apiGroups: [""]
  resources: ["pods", "services"]
  verbs: ["get", "list", "watch"]
```

---

## 2. Dynamic Admission Control Pipeline

Admission Controllers inspect, mutate, and validate object manifests prior to persistence in `etcd`.

```
kube-apiserver Pipeline
  |
  v (Post-Authentication & Authorization)
[ Mutating Admission Webhooks ]    ===> Modifies object (e.g. injects sidecar container)
  |
  v
[ Schema & Field Validation ]      ===> Verifies OpenAPI spec correctness
  |
  v
[ Validating Admission Webhooks ]  ===> Approves or rejects object (OPA / Kyverno)
  |
  v
[ etcd Storage ]
```

### 2.1 Webhook Policy Engines (OPA Gatekeeper & Kyverno)
* **Validating Webhooks:** Enforce enterprise compliance rules (e.g., rejecting images using the `:latest` tag, requiring `resources.limits` on all containers).
* **Mutating Webhooks:** Automate manifest enrichment (e.g., injecting secrets, adding default annotations).

---

## 3. Pod Security Standards (PSS) & Admission (PSA)

Replaces legacy PodSecurityPolicies with 3 built-in security profiles configured via namespace labels:

```yaml
metadata:
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/enforce-version: v1.30
```

| Security Profile | Intended Target | Restrictions Enforced |
| :--- | :--- | :--- |
| **`Privileged`** | System infrastructure (CNI, CSI drivers) | Unrestricted; allows root access, host namespaces (`hostNetwork`), and host path mounts. |
| **`Baseline`** | Standard microservices | Prevents known privilege escalations (blocks `hostPath`, `hostNetwork`, `hostPID`). |
| **`Restricted`** | Hardened tenant applications | Enforces non-root execution (`runAsNonRoot: true`), drops all capabilities (`capabilities.drop: ["ALL"]`), disallows privilege escalation. |

---

## 4. NetworkPolicies (Layer 3 / 4 Traffic Isolation)

By default, Kubernetes networking operates in a **Default-Allow** state (any Pod can communicate with any other Pod across any namespace).

**NetworkPolicies** enforce isolation using label selectors.

```
[ Default State ]            Pod A ---------------------------> Pod B (Allowed)

[ NetworkPolicy Applied ]   Pod A ---> [ Ingress Filter ] ---> Pod B (Denied unless label matches)
```

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: isolate-payment-db
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: payment-db
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: payment-api
    ports:
    - protocol: TCP
      port: 5432
```

---

## 5. Staff Engineer Security Checklist
1. **Apply Default-Deny NetworkPolicies:** Apply a `Default-Deny-All` ingress and egress NetworkPolicy to every tenant namespace, explicitly whitelisting authorized communication paths.
2. **Disable Automount ServiceAccount Tokens:** Set `automountServiceAccountToken: false` on PodSpecs that do not need to query `kube-apiserver` to mitigate token theft risks.
3. **Enforce `Restricted` PSA Profile:** Enable `pod-security.kubernetes.io/enforce: restricted` on all application namespaces to enforce container isolation at admission time.
