output "cloud_run_sa_email" {
  value = google_service_account.cloud_run.email
}

output "github_sa_email" {
  value = google_service_account.github.email
}

output "wif_provider_name" {
  value = google_iam_workload_identity_pool_provider.github.name
}
