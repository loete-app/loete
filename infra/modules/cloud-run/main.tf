# ── Backend (Spring Boot, port 8080) ─────────────────

resource "google_cloud_run_v2_service" "backend" {
  name     = "loete-backend"
  location = var.region
  project  = var.project_id

  template {
    service_account = var.service_account_email
    timeout         = "300s"

    scaling {
      min_instance_count = var.backend_min_instances
      max_instance_count = var.backend_max_instances
    }

    containers {
      image = var.backend_image

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "1Gi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      startup_probe {
        http_get {
          path = "/api/actuator/health"
          port = 8080
        }
        initial_delay_seconds = 15
        period_seconds        = 5
        failure_threshold     = 30
      }

      liveness_probe {
        http_get {
          path = "/api/actuator/health"
          port = 8080
        }
        period_seconds    = 30
        failure_threshold = 3
      }

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }

      env {
        name = "DATABASE_URL"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["database-url"]
            version = "latest"
          }
        }
      }

      env {
        name = "DATABASE_USERNAME"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["database-username"]
            version = "latest"
          }
        }
      }

      env {
        name = "DATABASE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["database-password"]
            version = "latest"
          }
        }
      }

      env {
        name = "JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["jwt-secret"]
            version = "latest"
          }
        }
      }

      env {
        name = "TICKETMASTER_API_KEY"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["ticketmaster-api-key"]
            version = "latest"
          }
        }
      }

      env {
        name = "CORS_ORIGINS"
        value_source {
          secret_key_ref {
            secret  = var.secret_ids["cors-origins"]
            version = "latest"
          }
        }
      }

      volume_mounts {
        name       = "cloudsql"
        mount_path = "/cloudsql"
      }
    }

    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [var.cloud_sql_connection_name]
      }
    }
  }

  lifecycle {
    # Image tag is updated out-of-band by the deploy workflow; tfvars-driven
    # labels and client metadata change on every apply and would cause churn.
    ignore_changes = [
      client,
      client_version,
      template[0].labels,
      template[0].containers[0].image,
    ]
  }
}

# ── Frontend (Angular SSR, port 4000) ─────────────────

resource "google_cloud_run_v2_service" "frontend" {
  name     = "loete-frontend"
  location = var.region
  project  = var.project_id

  template {
    service_account = var.service_account_email
    timeout         = "300s"

    scaling {
      min_instance_count = var.frontend_min_instances
      max_instance_count = var.frontend_max_instances
    }

    containers {
      image = var.frontend_image

      ports {
        container_port = 4000
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      startup_probe {
        http_get {
          path = "/"
          port = 4000
        }
        initial_delay_seconds = 5
        period_seconds        = 5
        failure_threshold     = 20
      }

      liveness_probe {
        http_get {
          path = "/"
          port = 4000
        }
        period_seconds = 30
      }

      env {
        name  = "API_URL"
        value = "${google_cloud_run_v2_service.backend.uri}/api"
      }
    }
  }

  lifecycle {
    ignore_changes = [
      client,
      client_version,
      template[0].labels,
      template[0].containers[0].image,
    ]
  }
}

# Both services publicly invokable
resource "google_cloud_run_v2_service_iam_member" "backend_public" {
  name     = google_cloud_run_v2_service.backend.name
  location = var.region
  project  = var.project_id
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_v2_service_iam_member" "frontend_public" {
  name     = google_cloud_run_v2_service.frontend.name
  location = var.region
  project  = var.project_id
  role     = "roles/run.invoker"
  member   = "allUsers"
}
