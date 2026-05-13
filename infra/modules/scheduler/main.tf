# Service account Cloud Scheduler uses when minting the OIDC token it sends to the backend.
# No run.invoker grant is needed because loete-backend is already publicly invokable
# (allUsers has roles/run.invoker); the backend verifies the OIDC token in-app.
resource "google_service_account" "scheduler" {
  project      = var.project_id
  account_id   = "loete-scheduler-invoker"
  display_name = "Cloud Scheduler invoker for Löte backend jobs"
}

# Daily Ticketmaster sync — 03:00 Europe/Zurich.
# attempt_deadline is short because the handler returns 202 immediately;
# the actual work runs async in the backend on the jobExecutor.
# retry_count = 0 — at-most-once. Work is idempotent; missed run self-heals next night.
resource "google_cloud_scheduler_job" "ticketmaster_sync" {
  project          = var.project_id
  region           = var.region
  name             = "loete-ticketmaster-sync"
  description      = "Triggers the Ticketmaster event sync in the Löte backend"
  schedule         = "0 3 * * *"
  time_zone        = "Europe/Zurich"
  attempt_deadline = "30s"

  retry_config {
    retry_count = 0
  }

  http_target {
    http_method = "POST"
    uri         = "${var.backend_url}/api/internal/jobs/ticketmaster-sync"

    oidc_token {
      service_account_email = google_service_account.scheduler.email
      audience              = var.audience
    }
  }
}

# Daily embedding generation — 03:30 Europe/Zurich (after the sync).
resource "google_cloud_scheduler_job" "embeddings" {
  project          = var.project_id
  region           = var.region
  name             = "loete-embeddings"
  description      = "Triggers the embedding generation in the Löte backend"
  schedule         = "30 3 * * *"
  time_zone        = "Europe/Zurich"
  attempt_deadline = "30s"

  retry_config {
    retry_count = 0
  }

  http_target {
    http_method = "POST"
    uri         = "${var.backend_url}/api/internal/jobs/embeddings"

    oidc_token {
      service_account_email = google_service_account.scheduler.email
      audience              = var.audience
    }
  }
}
