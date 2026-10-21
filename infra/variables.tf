variable "project_id" {
  type        = string
  description = "GCP project ID"
}

variable "region" {
  type        = string
  default     = "europe-west6"
  description = "GCP region (Zurich)"
}

variable "github_repo" {
  type        = string
  description = "GitHub repository in owner/repo format"
}

variable "cors_origins" {
  type        = string
  description = "Comma-separated list of allowed CORS origins for the backend"
  default     = ""
}

# ── Cloud Run scaling ────────────────────────────

variable "backend_min_instances" {
  type        = number
  default     = 0
  description = "Minimum backend instances (0 = scale to zero)"
}

variable "backend_max_instances" {
  type        = number
  default     = 3
  description = "Maximum backend instances"
}

variable "frontend_min_instances" {
  type        = number
  default     = 0
  description = "Minimum frontend instances"
}

variable "frontend_max_instances" {
  type        = number
  default     = 3
  description = "Maximum frontend instances"
}
