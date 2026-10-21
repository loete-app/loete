variable "project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "backend_image" {
  type = string
}

variable "frontend_image" {
  type = string
}

variable "cloud_sql_connection_name" {
  type = string
}

variable "service_account_email" {
  type = string
}

variable "secret_ids" {
  type = map(string)
}

variable "backend_min_instances" {
  type    = number
  default = 0
}

variable "backend_max_instances" {
  type    = number
  default = 3
}

variable "frontend_min_instances" {
  type    = number
  default = 0
}

variable "frontend_max_instances" {
  type    = number
  default = 3
}
