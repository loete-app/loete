locals {
  # Placeholder secrets — set manually via gcloud / GCP Console after apply.
  placeholder_secrets = {
    "jwt-secret"           = "CHANGE_ME"
    "ticketmaster-api-key" = "CHANGE_ME"
  }

  # Computed secrets — owned by Terraform.
  computed_secrets = {
    "database-url"      = var.database_url
    "database-username" = var.database_username
    "database-password" = var.database_password
    "cors-origins"      = var.cors_origins != "" ? var.cors_origins : "*"
  }

  all_secrets = merge(local.placeholder_secrets, local.computed_secrets)
}

resource "google_secret_manager_secret" "secrets" {
  for_each  = local.all_secrets
  secret_id = "loete-${each.key}"
  project   = var.project_id

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "placeholder" {
  for_each    = local.placeholder_secrets
  secret      = google_secret_manager_secret.secrets[each.key].id
  secret_data = each.value

  lifecycle {
    ignore_changes = [secret_data, enabled]
  }
}

resource "google_secret_manager_secret_version" "computed" {
  for_each    = local.computed_secrets
  secret      = google_secret_manager_secret.secrets[each.key].id
  secret_data = each.value
}
