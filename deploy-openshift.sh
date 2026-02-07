#!/bin/bash

# Enterprise Logistics Hub - OpenShift Sandbox Deployment Script
# This script automates the deployment using Helm specifically for OpenShift environments.

set -e # Exit on error

# 1. Validation
if ! command -v oc &> /dev/null; then
    echo "❌ Error: 'oc' CLI is not installed."
    exit 1
fi

if ! oc whoami &> /dev/null; then
    echo "❌ Error: You are not logged into OpenShift. Please use 'oc login' first."
    exit 1
fi

# 2. Configuration
# In OpenShift Sandbox, we usually work in the current active project/namespace
CURRENT_NS=$(oc project -q)
RELEASE_NAME="eglh"

echo "🚀 Starting OpenShift deployment in namespace: $CURRENT_NS"

# 3. Deploy using Helm
echo "☸️ Applying Helm chart with OpenShift profile..."

helm upgrade --install $RELEASE_NAME ./charts/eglh \
  --namespace "$CURRENT_NS" \
  --values ./charts/eglh/values-openshift.yaml \
  --set openshift.enabled=true \
  --set quota.enabled=false

echo "---------------------------------------------------"
echo "✅ Deployment requested successfully!"
echo "⏳ Monitoring progress..."
echo ""
echo "📍 Check Pods: oc get pods"
echo "🛠️ Check Builds: oc get builds"
echo "🌐 API Route URL: http://$(oc get route logistics-api -o jsonpath='{.spec.host}')"
echo "---------------------------------------------------"
echo "💡 Note: If this is the first deployment, the pods might stay in 'Pending' or 'ImagePullBackOff' until the BuildConfigs finish building the images."