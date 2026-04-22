resource "random_password" "db_password" {
  length  = 32
  special = false
}

resource "google_sql_database_instance" "postgres" {
  name             = "loete-db"
  database_version = "POSTGRES_16"
  region           = var.region
  project          = var.project_id

  settings {
    tier              = "db-f1-micro"
    disk_autoresize   = true
    disk_size         = 10
    availability_type = "ZONAL"

    backup_configuration {
      enabled    = true
      start_time = "03:00"
      backup_retention_settings {
        retained_backups = 7
      }
    }

    maintenance_window {
      day          = 3
      hour         = 3
      update_track = "stable"
    }
  }

  deletion_protection = false
}

resource "google_sql_database" "loete" {
  name     = "loete"
  instance = google_sql_database_instance.postgres.name
  project  = var.project_id
}

resource "google_sql_user" "loete" {
  name     = "loete"
  instance = google_sql_database_instance.postgres.name
  password = random_password.db_password.result
  project  = var.project_id
}
