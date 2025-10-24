# AWS Region
variable "aws_region" {
  type    = string
  default = "us-east-1"
}

# Source AMI - Ubuntu 24.04 LTS
variable "source_ami" {
  type    = string
  default = "ami-0e2c8caa4b6378d8c" # Ubuntu 24.04 LTS in us-east-1
}

# Instance type for building
variable "instance_type" {
  type    = string
  default = "t2.micro"
}

# SSH username for Ubuntu
variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

# AMI name prefix
variable "ami_name_prefix" {
  type    = string
  default = "csye6225"
}

# DEMO AWS Account ID (需要你填入)
variable "demo_account_id" {
  type    = string
  default = "130214421575" # 稍後填入你的 DEMO 帳號 ID
}

# 資料庫密碼
variable "db_password" {
  type    = string
  default = "csye6225_db_pass"
}