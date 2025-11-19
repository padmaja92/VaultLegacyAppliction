# AWS EKS Deployment Prerequisites and Setup Guide

## Prerequisites

### 1. AWS Account and CLI Setup
- [ ] Active AWS Account
- [ ] AWS CLI installed
```powershell
# Install AWS CLI on Windows
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi
```
- [ ] Configure AWS credentials
```bash
aws configure
# You'll need:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region (e.g., us-east-1)
# - Default output format (json)
```

### 2. Required Tools
- [ ] kubectl - Kubernetes command-line tool
```powershell
# Install kubectl on Windows
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
# Move to a directory in your PATH
```

- [ ] eksctl - EKS cluster management tool
```powershell
# Install eksctl on Windows using chocolatey
choco install eksctl
```

- [ ] Helm - Kubernetes package manager
```powershell
# Install Helm on Windows using chocolatey
choco install kubernetes-helm
```

### 3. Docker Image Requirements
- [x] Docker image pushed to repository: docker.io/padmaja92/vault-legacy-app-repo:latest
- [x] Image is multi-architecture compatible (if needed)

## EKS Cluster Creation

### 1. Create EKS Cluster Configuration
```yaml
# eks-cluster.yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig
metadata:
  name: vault-legacy-cluster
  region: us-east-1  # Change to your preferred region
  version: "1.28"    # Kubernetes version

# Managed node group
nodeGroups:
  - name: ng-1
    instanceType: t3.medium
    desiredCapacity: 2
    minSize: 2
    maxSize: 4
    volumeSize: 20
    ssh:
      allow: false

# Enable cluster logging
cloudWatch:
  clusterLogging:
    enableTypes: ["api", "audit", "authenticator", "controllerManager", "scheduler"]
```

### 2. Required IAM Roles and Policies
- EKS Cluster Role
- Node Group Role
- Load Balancer Controller Role
- Cluster Autoscaler Role (optional)

### 3. Network Requirements
- VPC with public and private subnets
- Internet Gateway
- NAT Gateway
- Security Groups

## Post-Cluster Setup

### 1. Install AWS Load Balancer Controller
```bash
# Add helm repository
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=vault-legacy-cluster \
  --set serviceAccount.create=true \
  --set serviceAccount.name=aws-load-balancer-controller
```

### 2. Update Ingress Configuration for AWS
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: vault-legacy-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  rules:
    - host: www.auravaultlegacy.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: vault-legacy-app-service
                port:
                  number: 80
```

## Estimated Costs (Monthly)
- EKS Cluster: $73.00
- 2 t3.medium nodes: ~$60.00
- Load Balancer: ~$16.00
- NAT Gateway: ~$32.00
- Data Transfer: Varies based on usage
- Total Base Cost: ~$181.00/month

## Security Considerations
1. Network Security
   - Use private subnets for worker nodes
   - Implement security groups
   - Enable VPC flow logs

2. Access Control
   - Use IAM roles for service accounts
   - Implement RBAC
   - Regular rotation of credentials

3. Monitoring
   - Enable CloudWatch logging
   - Set up CloudWatch alarms
   - Configure cluster metrics

## High Availability Setup
- Deploy across multiple Availability Zones
- Use node groups with minimum 2 nodes
- Configure horizontal pod autoscaling
- Implement liveness and readiness probes (already in your deployment)

## Next Steps After Setup
1. Deploy application components:
```bash
kubectl apply -f k8s/
```

2. Verify deployments:
```bash
kubectl get pods,svc,ingress
```

3. Configure DNS:
   - Get ALB DNS name
   - Update your domain's DNS records

4. Monitor application:
   - Check pod status
   - Monitor CloudWatch logs
   - Set up alerting

## Troubleshooting Guide
1. Check pod status:
```bash
kubectl get pods
kubectl describe pod <pod-name>
```

2. Check logs:
```bash
kubectl logs <pod-name>
```

3. Common issues:
   - Image pull errors
   - Resource constraints
   - Network connectivity
   - Permission issues

## Backup and Disaster Recovery
1. Use AWS Backup
2. Regular snapshot schedule
3. Cross-region replication (if needed)
4. Document recovery procedures