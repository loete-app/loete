variable "project_id" {
  type = string
}

variable "github_repo" {
  type        = string
  description = "GitHub repository in owner/repo format"
}

variable "secret_ids" {
  type        = map(string)
  description = "Secret IDs that the Cloud Run service account needs access to"
}
