output "backend_url" {
  value       = module.cloud_run.backend_url
  description = "Public URL of the Spring Boot backend"
}

output "frontend_url" {
  value       = module.cloud_run.frontend_url
  description = "Public URL of the Angular SSR frontend"
}

output "artifact_registry_url" {
  value       = module.artifact_registry.repository_url
  description = "Docker repository URL for pushing images"
}

output "cloud_sql_connection_name" {
  value       = module.cloud_sql.connection_name
  description = "Cloud SQL connection name (PROJECT:REGION:INSTANCE)"
}

output "github_service_account_email" {
  value       = module.iam.github_sa_email
  description = "Service account email for GitHub Actions to impersonate"
}

output "workload_identity_provider" {
  value       = module.iam.wif_provider_name
  description = "Full WIF provider resource name for GitHub Actions"
}
