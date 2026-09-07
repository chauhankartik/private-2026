# Chapter 6: Security, Enterprise Security (SIEM) & RBAC

Enterprise security in Splunk encompasses role-based access control, index-level data segregation, transport encryption, and SIEM security automation via Splunk Enterprise Security (ES).

---

## 1. Splunk Enterprise Security (ES) & SIEM Architecture

**Splunk Enterprise Security (ES)** is a premium SIEM solution providing threat detection, incident management, and risk attribution across multi-cloud infrastructure.

```
[ Multi-Cloud Security Logs ] (Firewalls, EDR, CloudTrail, IAM)
              |
              v
[ Common Information Model (CIM) Normalization ] (Maps fields to standard schema)
              |
              v
[ Correlation Searches ] (Runs continuous security queries)
              |
              +---> Generates Notable Events (index=notable) ---> SOC Triage
              |
              v
[ Risk-Based Alerting (RBA) Engine ]
  ├── Assigns Risk Scores to Assets (IPs, Hostnames) & Identities (Users)
  └── Triggers High-Priority Incident Page only when Aggregate Risk Threshold > 100
```

### 1.1 Common Information Model (CIM)
Normalizes disparate vendor fields into standard data models. For example, firewall logs (`src_ip`), proxy logs (`client_ip`), and AWS CloudTrail (`sourceIPAddress`) are all normalized into the standard CIM field **`src`**.

### 1.2 Risk-Based Alerting (RBA)
Rather than paging SOC analysts on individual low-fidelity alerts, RBA appends risk events to `index=risk`. When an asset or identity accumulates multiple risk events (e.g. Suspicious Email + PowerShell Execution + Outbound Port 4444 Connection) exceeding a risk threshold, RBA generates a single high-fidelity **Notable Event**.

---

## 2. Role-Based Access Control (RBAC) & Index Segregation

Splunk enforces granular security through Roles mapped to Capabilities and Index Restrictions.

```ini
# authorize.conf - Role Configuration Example
[role_pci_analyst]
importRoles = user
grantableRoles = user
srchIndexesAllowed = pci_compliance;payment_gateway
srchIndexesDefault = pci_compliance
srchFilter = dest_port=443 OR dest_port=8443
capability::search = enabled
```

* **`srchIndexesAllowed`:** Explicitly restricts which storage indexes this role is authorized to query.
* **`srchFilter`:** Dynamically appends mandatory security filter predicates to **every search query** executed by users in this role.

---

## 3. Data Pipeline Encryption (SSL/TLS)

All inter-component communication channels in Splunk should be encrypted using TLS 1.3 with custom private key infrastructure (PKI).

```ini
# $SPLUNK_HOME/etc/system/local/outputs.conf (Forwarder SSL Config)
[tcpout:primary_indexers]
server = indexer1.example.com:9997, indexer2.example.com:9997
clientCert = $SPLUNK_HOME/etc/auth/myCerts/forwarder.pem
sslPassword = $7$encryptedPassword
sslRootCAPath = $SPLUNK_HOME/etc/auth/myCerts/cacert.pem
sslVerifyServerCert = true
sslCommonNameToCheck = indexer*.example.com
```

---

## 4. Staff Engineer Security Checklist
1. **Enable SSL Server Certificate Verification:** Set `sslVerifyServerCert = true` on all forwarders and inter-component configuration files (`outputs.conf`, `server.conf`). Defaulting to unverified SSL permits Man-in-the-Middle (MitM) log interception.
2. **Apply Index-Level Authorization Restrictions:** Never grant broad `srchIndexesAllowed = *` to general application developer roles. Isolate audit, security, and PCI logs into restricted indexes accessible only by authorized roles.
3. **Audit User Search Activity:** Monitor `index=_audit` to track user search execution queries, exported search artifacts, and privilege escalation attempts.
