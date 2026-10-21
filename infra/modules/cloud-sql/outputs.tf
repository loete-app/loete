output "connection_name" {
  value = google_sql_database_instance.postgres.connection_name
}

output "database" {
  value = google_sql_database.loete.name
}

output "user" {
  value = google_sql_user.loete.name
}

output "password" {
  value     = random_password.db_password.result
  sensitive = true
}
