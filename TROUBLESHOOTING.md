# Cloud-Native Troubleshooting Guide (OpenShift Sandbox)

This guide documents the specific challenges and solutions encountered while deploying the EGLH system to a restricted environment like Red Hat OpenShift Sandbox.

## 1. Security Context Constraints (SCC) Errors
**Symptom**: Pods for Postgres or RabbitMQ are not created. `oc describe statefulset` shows: `forbidden: unable to validate against any security context constraint`.

**Reason**: OpenShift's `restricted-v2` SCC does not allow containers to run with hardcoded UIDs (like `999` for Postgres) or specific `fsGroup` values.

**Solution**:
- Remove `runAsUser` and `fsGroup` from `securityContext` in OpenShift mode.
- OpenShift will automatically assign a safe, random UID from the project's allocated range.
- Ensure `runAsNonRoot: true` is maintained.

## 2. Resource Quota Exceeded
**Symptom**: `Failed to create new replica set`. Error: `exceeded quota: for-user-replicas, requested: count/replicasets.apps=1, used: 30, limited: 30`.

**Reason**: Every time you redeploy or restart a build, OpenShift keeps old `ReplicaSets` (history). On a Sandbox, there is a hard limit (usually 30).

**Solution**:
- Clean up old build objects: `oc delete builds --all`
- Clean up unused ReplicaSets: `oc get rs | awk '$2 == 0 {print $1}' | xargs oc delete rs`

## 3. ImagePullBackOff / ErrImagePull
**Symptom**: Pods are `Running` but cannot pull the image.

**Reason**: Standard Deployments try to pull from external registries (Docker Hub). OpenShift uses internal `ImageStreams`.

**Solution**:
- Use the internal name `logistics-api:latest` in the container image field.
- **Crucial**: Add an Image Trigger annotation to the Deployment so OpenShift knows to update the Pod whenever a new build finishes:
  ```yaml
  annotations:
    image.openshift.io/triggers: '[{"from":{"kind":"ImageStreamTag","name":"logistics-api:latest"},"fieldPath":"spec.template.spec.containers[?(@.name=="logistics-api")].image"}]'
  ```

## 4. Build Failures (OutOfMemory / GC Overhead)
**Symptom**: Build fails during Maven compilation with `exit status 3` or `GC overhead limit exceeded`.

**Reason**: Quarkus build process is memory-intensive.

**Solution**:
- Increase memory limits in `BuildConfig`:
  ```yaml
  resources:
    limits:
      memory: 2Gi
  ```

## 5. Invalid Context Directory
**Symptom**: Build fails with `provided context directory does not exist`.

**Reason**: `contextDir` in `BuildConfig` must be relative to the root of the Git repository, not the local folder where you run the command.

**Solution**:
- Verify the path on GitHub. If the project is in the root, use `logistics-api`, not `enterprise-logistics-hub/logistics-api`.
