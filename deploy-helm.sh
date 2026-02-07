#!/bin/bash

# Enterprise Logistics Hub - Helm Deployment Script
# This script automates the build and deployment using Helm.

set -e # Exit on error

# Configuration
NAMESPACE="logistics"
RELEASE_NAME="eglh"
API_IMG="logistics-api:latest"
WORKER_IMG="route-worker:latest"

# Determine how to run kubectl/helm
if [ -r /etc/rancher/k3s/k3s.yaml ]; then
    KUBECTL="kubectl"
    HELM="helm"
else
    KUBECTL="sudo kubectl"
    HELM="sudo helm"
fi

echo "🚀 Starting Helm deployment to k3s..."

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

# 4. Deploy using Helm
echo "☸️ Applying Helm chart..."

# Create namespace if it doesn't exist
$KUBECTL create namespace $NAMESPACE --dry-run=client -o yaml | $KUBECTL apply -f -

$HELM upgrade --install $RELEASE_NAME ./charts/eglh 
  --namespace $NAMESPACE 
  --create-namespace

echo "✅ Helm deployment finished!"
echo "📍 Check status with: $KUBECTL get all -n $NAMESPACE"
