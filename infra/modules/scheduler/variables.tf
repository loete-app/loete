variable "project_id" {
  type        = string
  description = "GCP project ID"
}

variable "region" {
  type        = string
  description = "Cloud Scheduler region (must match supported region; europe-west1 is supported)"
}

variable "backend_url" {
  type        = string
  description = "Public URL of the backend Cloud Run service (no trailing slash)"
}

variable "audience" {
  type        = string
  description = "OIDC audience claim Cloud Scheduler will set on its token; must match app.scheduler.audience in the backend"
  default     = "loete-scheduler"
}

variable "github_sa_email" {
  type        = string
  description = "GitHub Actions deployer SA email; needs actAs on the scheduler SA to create jobs that mint OIDC tokens as it"
}
