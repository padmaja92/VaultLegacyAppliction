# CloudFormation Templates for VaultLegacy

This directory contains AWS CloudFormation templates for deploying VaultLegacy infrastructure.

## ec2-vpc-stack.yaml

Creates a complete VPC with an EC2 instance for hosting the VaultLegacy application.

### What it creates:
- **VPC** (10.0.0.0/16)
- **Public Subnet** (10.0.1.0/24)
- **Internet Gateway**
- **Route Table** with route to Internet Gateway
- **Security Group** (SSH, HTTP, HTTPS, port 8080)
- **IAM Role** with S3 full access
- **EC2 Instance** with Java 21, Docker, Git pre-installed

### Prerequisites:
1. AWS CLI installed and configured
2. An existing EC2 key pair (or create one)

### Create Key Pair (if needed):
```bash
aws ec2 create-key-pair \
  --key-name VaultLegacyKeyPair \
  --query 'KeyMaterial' \
  --output text \
  --region us-east-2 > VaultLegacyKeyPair.pem

# Set proper permissions
chmod 400 VaultLegacyKeyPair.pem  # On Linux/Mac
icacls VaultLegacyKeyPair.pem /inheritance:r /grant:r "%username%:R"  # On Windows
```

### Deploy the Stack:

**Using AWS CLI:**
```bash
aws cloudformation create-stack \
  --stack-name VaultLegacyStack \
  --template-body file://aws/cloudformation/ec2-vpc-stack.yaml \
  --parameters ParameterKey=KeyName,ParameterValue=VaultLegacyKeyPair \
               ParameterKey=InstanceType,ParameterValue=t2.micro \
               ParameterKey=SSHLocation,ParameterValue=0.0.0.0/0 \
  --capabilities CAPABILITY_IAM \
  --region us-east-2
```

**Using AWS Console:**
1. Go to CloudFormation in AWS Console
2. Click "Create stack" → "With new resources"
3. Upload `ec2-vpc-stack.yaml`
4. Fill in parameters:
   - Stack name: `VaultLegacyStack`
   - KeyName: Your key pair name
   - InstanceType: `t2.micro` (default)
   - SSHLocation: `0.0.0.0/0` (or your IP)
5. Click through and create

### Check Stack Status:
```bash
# Check creation status
aws cloudformation describe-stacks \
  --stack-name VaultLegacyStack \
  --query 'Stacks[0].StackStatus' \
  --region us-east-2

# Wait for stack creation to complete
aws cloudformation wait stack-create-complete \
  --stack-name VaultLegacyStack \
  --region us-east-2
```

### Get Stack Outputs:
```bash
# Get all outputs
aws cloudformation describe-stacks \
  --stack-name VaultLegacyStack \
  --query 'Stacks[0].Outputs' \
  --region us-east-2

# Get specific output (e.g., Public IP)
aws cloudformation describe-stacks \
  --stack-name VaultLegacyStack \
  --query 'Stacks[0].Outputs[?OutputKey==`PublicIP`].OutputValue' \
  --output text \
  --region us-east-2
```

### Connect to EC2 Instance:
```bash
# Get SSH command from outputs
aws cloudformation describe-stacks \
  --stack-name VaultLegacyStack \
  --query 'Stacks[0].Outputs[?OutputKey==`SSHCommand`].OutputValue' \
  --output text \
  --region us-east-2

# Or manually:
ssh -i VaultLegacyKeyPair.pem ec2-user@<PUBLIC_IP>
```

### Deploy Your Application:
Once connected to EC2:
```bash
# Clone your repository
cd /opt/vaultlegacy
git clone https://github.com/yourusername/VaultLegacyBankingProject.git
cd VaultLegacyBankingProject

# Build the application
./mvnw clean package

# Run the application
java -jar target/vault-legacy-banking-1.0-SNAPSHOT.jar
```

Or use Docker:
```bash
# Build Docker image
docker build -t vault-legacy-app .

# Run container
docker run -d -p 8080:8080 vault-legacy-app
```

### Access Application:
```bash
# Get application URL from CloudFormation outputs
aws cloudformation describe-stacks \
  --stack-name VaultLegacyStack \
  --query 'Stacks[0].Outputs[?OutputKey==`ApplicationURL`].OutputValue' \
  --output text \
  --region us-east-2

# Open in browser: http://<PUBLIC_IP>:8080
```

### Update Stack:
```bash
aws cloudformation update-stack \
  --stack-name VaultLegacyStack \
  --template-body file://aws/cloudformation/ec2-vpc-stack.yaml \
  --parameters ParameterKey=KeyName,UsePreviousValue=true \
               ParameterKey=InstanceType,ParameterValue=t2.small \
  --capabilities CAPABILITY_IAM \
  --region us-east-2
```

### Delete Stack (cleanup):
```bash
aws cloudformation delete-stack \
  --stack-name VaultLegacyStack \
  --region us-east-2

# Wait for deletion to complete
aws cloudformation wait stack-delete-complete \
  --stack-name VaultLegacyStack \
  --region us-east-2
```

### Estimated Monthly Costs:
- EC2 t2.micro: ~$8.50/month (free tier eligible for 12 months)
- EBS Storage (8GB): ~$0.80/month
- Data Transfer: ~$1-5/month (varies)
- Total: ~$10-15/month (or free with free tier)

### Security Notes:
- Default SSH access is from anywhere (0.0.0.0/0). For production, restrict to your IP.
- Change `SSHLocation` parameter to your IP: `YOUR_IP/32`
- The instance has S3 full access via IAM role (for VaultBucket uploads)
- Update security groups as needed for your application

### Troubleshooting:
```bash
# Check stack events
aws cloudformation describe-stack-events \
  --stack-name VaultLegacyStack \
  --region us-east-2

# Check EC2 instance status
aws ec2 describe-instance-status \
  --instance-ids <INSTANCE_ID> \
  --region us-east-2

# View instance system log
aws ec2 get-console-output \
  --instance-id <INSTANCE_ID> \
  --region us-east-2
```
