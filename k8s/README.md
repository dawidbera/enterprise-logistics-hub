# Kubernetes Orchestration (EGLH)

This directory contains manifests for deploying EGLH to a Kubernetes cluster. The configuration covers **100% of the CKAD** exam scope and is optimized for production-like environments (k3s, OpenShift).

## Structure
- `config/`: Namespace setup, ResourceQuotas, shared ConfigMaps.
- `infrastructure/`: Persistent components (PostgreSQL, RabbitMQ).
- `apps/`: Application deployments (API, Worker), Ingress, and scaling (HPA).
- `security/`: NetworkPolicies and ServiceAccounts.
- `jobs/`: Maintenance CronJobs.

## Deployment Order
The `deploy-k3s.sh` script automates this, but the manual order is:

1.  **Configuration & Namespace**: `kubectl apply -f k8s/config/`
2.  **Security**: `kubectl apply -f k8s/security/`
3.  **Infrastructure**: `kubectl apply -f k8s/infrastructure/`
4.  **Applications**: `kubectl apply -f k8s/apps/`
5.  **Jobs**: `kubectl apply -f k8s/jobs/`

## CKAD Features Implemented

### 1. Workload Scheduling & State
- **StatefulSets**: PostgreSQL and RabbitMQ use stable network identities and PVCs.
- **Init Containers**: Apps wait for infrastructure availability using `nc` checks.
- **HPA**: `route-worker` scales based on CPU utilization (verified by Stress Test).

### 2. Configuration & Security
- **Namespace Isolation**: All resources are deployed in the `logistics` namespace.
- **Resource Management**: `ResourceQuota` limits total project resources; `LimitRange` sets default container limits.
- **Network Policies**: Traffic to Postgres/RabbitMQ is strictly limited to internal app components.
- **Service Accounts**: `logistics-api` runs with a dedicated identity.
- **Security Context**: All containers run as non-root users.

### 3. Services & Networking
- **Ingress**: The API is exposed via `eglh.local` (Port 80).
- **Headless Service**: Used for RabbitMQ StatefulSet DNS resolution.

### 4. Observability & Maintenance
- **Probes**: Liveness/Readiness probes configured with safe timeouts.
- **CronJob**: A scheduled job (`db-cleanup`) runs every hour to remove completed orders.

## Verification Results

### Stress Test (50 orders)
- **Execution**: Sent 50 orders in a burst.
- **Outcome**: HPA successfully scaled `route-worker` replicas. All orders reached `COMPLETED` status.

### Chaos Test (Pod Deletion)
- **Execution**: Deleted `rabbitmq-0` pod during active processing.
- **Outcome**: Thanks to `StatefulSet` and PVC, the new pod recovered all messages. No orders were lost.

## Local Testing (k3s)
Use the provided `deploy-k3s.sh` script to automate the entire lifecycle:
```bash
./deploy-k3s.sh
```
Check the status in the new namespace:
```bash
kubectl get all -n logistics
```