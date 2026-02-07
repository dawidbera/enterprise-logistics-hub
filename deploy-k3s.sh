#!/bin/bash

# Enterprise Logistics Hub - k3s Deployment Script
# This script automates the build, image loading, and deployment to a local k3s cluster.

set -e # Exit on error

# Configuration
NAMESPACE="logistics"
API_IMG="logistics-api:latest"
WORKER_IMG="route-worker:latest"

# Determine how to run kubectl
if [ -r /etc/rancher/k3s/k3s.yaml ]; then
    KUBECTL="kubectl"
else
    KUBECTL="sudo kubectl"
fi

echo "🚀 Starting deployment to k3s..."

# 1. Build Java Applications
echo "📦 Building JAR files..."
(cd logistics-api && ./mvnw package -DskipTests)
(cd route-worker && ./mvnw package -DskipTests)

# 2. Build Docker Images
echo "🐳 Building Docker images..."
docker build -t $API_IMG -f logistics-api/src/main/docker/Dockerfile.jvm logistics-api/
docker build -t $WORKER_IMG -f route-worker/src/main/docker/Dockerfile.jvm route-worker/

# 3. Load Images into k3s
echo "🚚 Importing images into k3s..."
docker save $API_IMG | sudo k3s ctr images import -
docker save $WORKER_IMG | sudo k3s ctr images import -

# 4. Apply Kubernetes Manifests
echo "☸️ Applying Kubernetes manifests using $KUBECTL..."

echo "--- Setting up Namespace and Policies ---"
$KUBECTL apply -f k8s/config/namespace-setup.yaml
$KUBECTL apply -f k8s/config/common-config.yaml
$KUBECTL apply -f k8s/security/

echo "--- Deploying Infrastructure (Postgres & RabbitMQ) ---"
$KUBECTL apply -f k8s/infrastructure/

echo "⏳ Waiting for infrastructure to be ready in $NAMESPACE..."
$KUBECTL wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=120s
$KUBECTL wait --for=condition=ready pod -l app=rabbitmq -n $NAMESPACE --timeout=120s

echo "--- Deploying Applications (API & Worker) ---"
$KUBECTL apply -f k8s/apps/

echo "--- Setting up Maintenance Jobs ---"
$KUBECTL apply -f k8s/jobs/

echo "✅ Deployment finished!"
echo "📍 Check status with: $KUBECTL get pods -n $NAMESPACE"
echo "🌐 API Ingress Host: eglh.local"
