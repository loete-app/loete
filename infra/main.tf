module "cloud_sql" {
  source = "./modules/cloud-sql"

  project_id = var.project_id
  region     = var.region

  depends_on = [google_project_service.apis]
}

module "artifact_registry" {
  source = "./modules/artifact-registry"

  project_id = var.project_id
  region     = var.region

  depends_on = [google_project_service.apis]
}

module "secrets" {
  source = "./modules/secrets"

  project_id = var.project_id

  database_url      = "jdbc:postgresql:///${module.cloud_sql.database}?host=/cloudsql/${module.cloud_sql.connection_name}"
  database_username = module.cloud_sql.user
  database_password = module.cloud_sql.password
  cors_origins      = var.cors_origins

  depends_on = [google_project_service.apis]
}

module "iam" {
  source = "./modules/iam"

  project_id  = var.project_id
  github_repo = var.github_repo
  secret_ids  = module.secrets.secret_ids

  depends_on = [google_project_service.apis]
}

module "cloud_run" {
  source = "./modules/cloud-run"

  project_id = var.project_id
  region     = var.region

  backend_image  = "${module.artifact_registry.repository_url}/backend:latest"
  frontend_image = "${module.artifact_registry.repository_url}/frontend:latest"

  cloud_sql_connection_name = module.cloud_sql.connection_name
  service_account_email     = module.iam.cloud_run_sa_email
  secret_ids                = module.secrets.secret_ids

  backend_min_instances  = var.backend_min_instances
  backend_max_instances  = var.backend_max_instances
  frontend_min_instances = var.frontend_min_instances
  frontend_max_instances = var.frontend_max_instances

  depends_on = [google_project_service.apis, module.iam]
}
