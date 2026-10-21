resource "google_artifact_registry_repository" "docker" {
  location      = var.region
  repository_id = "loete"
  format        = "DOCKER"
  project       = var.project_id
}
