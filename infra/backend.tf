terraform {
  backend "gcs" {
    bucket = "loete-tofu-state"
    prefix = "terraform/state"
  }
}
