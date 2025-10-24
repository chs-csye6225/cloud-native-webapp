# Packer 版本要求
packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# 資料來源：查找最新的 Ubuntu 24.04 LTS AMI
data "amazon-ami" "ubuntu" {
  filters = {
    name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"
    root-device-type    = "ebs"
    virtualization-type = "hvm"
  }
  most_recent = true
  owners      = ["099720109477"] # Canonical 官方 Account ID
  region      = var.aws_region
}

# Source 定義：如何建立臨時 EC2 instance
source "amazon-ebs" "webapp" {
  # AMI 設定
  ami_name        = "${var.ami_name_prefix}-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
  ami_description = "AMI for CSYE6225 Web Application"
  ami_users       = [var.demo_account_id] # 自動分享到 DEMO 帳號

  # Instance 設定
  instance_type = var.instance_type
  region        = var.aws_region
  source_ami    = data.amazon-ami.ubuntu.id
  ssh_username  = var.ssh_username

  # AMI 啟動權限設定
  ami_regions = [var.aws_region]

  # EBS Volume 設定
  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
    delete_on_termination = true
  }

  # Tags
  tags = {
    Name        = "${var.ami_name_prefix}-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
    Environment = "dev"
    Application = "webapp"
  }
}

# Build 定義：執行安裝和配置步驟
build {
  sources = ["source.amazon-ebs.webapp"]

  # 1. 更新系統套件
  provisioner "shell" {
    inline = [
      "echo 'Waiting for cloud-init to complete...'",
      "sudo cloud-init status --wait",
      "echo 'Updating system packages...'",
      "sudo apt-get update",
      "sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold'"
    ]
  }

  # 2. 安裝必要軟體
  provisioner "shell" {
    inline = [
      "echo 'Installing Java 21...'",
      "sudo apt-get install -y openjdk-21-jdk",
      "echo 'Installing PostgreSQL...'",
      "sudo apt-get install -y postgresql postgresql-contrib"
    ]
  }

  # 3. 建立應用程式使用者和群組
  provisioner "shell" {
    inline = [
      "echo 'Creating csye6225 user and group...'",
      "sudo groupadd csye6225",
      "sudo useradd -r -g csye6225 -s /usr/sbin/nologin csye6225",
      "echo 'Creating application directory...'",
      "sudo mkdir -p /opt/csye6225",
      "sudo chown csye6225:csye6225 /opt/csye6225"
    ]
  }

  # 4. 複製應用程式 JAR 檔案
  provisioner "file" {
    source      = "../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp.jar"
  }

  # 5. 複製 systemd service 檔案
  provisioner "file" {
    source      = "../systemd/csye6225.service"
    destination = "/tmp/csye6225.service"
  }

  # 6. 移動檔案到正確位置並設定權限
  provisioner "shell" {
    inline = [
      "echo 'Moving application files...'",
      "sudo mv /tmp/webapp.jar /opt/csye6225/webapp.jar",
      "sudo chown csye6225:csye6225 /opt/csye6225/webapp.jar",
      "sudo chmod 500 /opt/csye6225/webapp.jar",
      "echo 'Setting up systemd service...'",
      "sudo mv /tmp/csye6225.service /etc/systemd/system/csye6225.service",
      "sudo chmod 644 /etc/systemd/system/csye6225.service"
    ]
  }

  # 7. 設定 PostgreSQL
  provisioner "shell" {
    inline = [
      "echo 'Configuring PostgreSQL...'",
      "sudo systemctl start postgresql",
      "sudo systemctl enable postgresql",
      "echo 'Creating database and user...'",
      "sudo -u postgres psql -c \"CREATE DATABASE csye6225;\"",
      "sudo -u postgres psql -c \"CREATE USER csye6225 WITH PASSWORD '${var.db_password}';\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE csye6225 TO csye6225;\"",
      "sudo -u postgres psql -d csye6225 -c \"GRANT ALL ON SCHEMA public TO csye6225;\"",
      "sudo -u postgres psql -d csye6225 -c \"ALTER DATABASE csye6225 OWNER TO csye6225;\""
    ]
  }

  # 8. 啟用 systemd service
  provisioner "shell" {
    inline = [
      "echo 'Enabling systemd service...'",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable csye6225.service"
    ]
  }

  # 9. 清理（不安裝 git 等開發工具）
  provisioner "shell" {
    inline = [
      "echo 'Cleaning up...'",
      "sudo apt-get clean",
      "sudo rm -rf /var/lib/apt/lists/*"
    ]
  }
}