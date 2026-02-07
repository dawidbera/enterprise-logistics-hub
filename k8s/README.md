# Kubernetes Orchestration (EGLH)

This directory contains manifests for deploying EGLH to a Kubernetes cluster. The configuration is optimized for local clusters like `k3s` and is compatible with `OpenShift`.

## Structure
- `config/`: Shared ConfigMaps and Secrets.
- `infrastructure/`: Persistent components (PostgreSQL, RabbitMQ).
- `apps/`: Application deployments (API, Worker) and scaling (HPA).

## Deployment Order
Manifests should be applied in the following order:

1.  **Configuration**: `kubectl apply -f k8s/config/`
2.  **Infrastructure**: `kubectl apply -f k8s/infrastructure/`
3.  **Applications**: `kubectl apply -f k8s/apps/`

## Key Kubernetes Features Used

### StatefulSets & Persistence
Both **PostgreSQL** and **RabbitMQ** are deployed as `StatefulSet` with `PersistentVolumeClaim` (PVC). 
*   **Critical Fix**: For RabbitMQ, we use `RABBITMQ_NODENAME: rabbit@localhost` to ensure that data stored on the PVC remains accessible even if the Pod name changes (though StatefulSet keeps it stable, this is a best practice for single-node durability).

### Horizontal Pod Autoscaler (HPA)
The `route-worker` is configured with an HPA to handle CPU-intensive tasks.
- **Min Replicas**: 1
- **Max Replicas**: 5
- **Trigger**: 50% average CPU utilization.
- **Result**: Verified during Stress Test (scaled to multiple replicas at ~93% CPU load).

### Health Probes
All components use `Liveness` and `Readiness` probes.
- **Stability Note**: Timeouts were increased to **10s** and initial delays to **120s** for RabbitMQ to prevent restart loops during heavy initialization or disk I/O.

## Verification Results

### Stress Test (50 orders)
- **Execution**: Sent 50 orders in a burst.
- **Outcome**: HPA successfully scaled `route-worker` replicas. All orders reached `COMPLETED` status.

### Chaos Test (Pod Deletion)
- **Execution**: Deleted `rabbitmq-0` pod during active processing.
- **Outcome**: Thanks to `StatefulSet` and PVC, the new pod recovered all messages. No orders were lost.

### Security Context
To ensure compatibility with OpenShift's `restricted-v2` SCC:
- All containers run as **non-root**.
- Specific UIDs are assigned (185 for Quarkus, 1001 for RabbitMQ, 999 for Postgres).

## Local Testing (k3s)
Use the provided `deploy-k3s.sh` script to automate building images and loading them into the k3s internal registry.
