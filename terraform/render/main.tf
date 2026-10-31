terraform {
  required_providers {
    render = {
      source  = "render-oss/render"
      version = "~> 1.9.1"
    }
  }
}

# RENDER_API_KEY and RENDER_OWNER_ID are read from the environment.
provider "render" {
  skip_deploy_after_service_update = true
}

variable "render_region" {
  description = "Render region; choose one close to the Neon database."
  type        = string
  default     = "oregon"
}

variable "render_database_url" {
  description = "Neon pooled JDBC URL, including sslmode=require."
  type        = string
  sensitive   = true
}

variable "render_flyway_url" {
  description = "Neon direct JDBC URL for Flyway migrations."
  type        = string
  sensitive   = true
}

variable "render_database_username" {
  type      = string
  sensitive = true
}

variable "render_database_password" {
  type      = string
  sensitive = true
}

variable "render_redis_url" {
  description = "Hosted Redis URL, using rediss:// when TLS is required."
  type        = string
  sensitive   = true
}

variable "render_jwt_secret" {
  description = "A unique, cryptographically random JWT signing secret."
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.render_jwt_secret) >= 32
    error_message = "The JWT secret must contain at least 32 characters."
  }
}

variable "render_frontend_url" {
  description = "Public frontend origin, e.g. https://design-deck-ui.vercel.app."
  type        = string
}

variable "render_bazaarlink_api_key" {
  description = "Optional BazaarLink API key."
  type        = string
  sensitive   = true
  default     = ""
}

resource "render_web_service" "web" {
  name   = "design-deck-backend"
  plan   = "free"
  region = var.render_region

  runtime_source = {
    docker = {
      repo_url            = "https://github.com/ketan-jainn/design-deck"
      branch              = "main"
      auto_deploy_trigger = "off"
      context             = "./apps/api"
      dockerfile_path     = "./apps/api/Dockerfile"
    }
  }

  env_vars = {
    DATABASE_URL       = { value = var.render_database_url }
    DATABASE_USERNAME  = { value = var.render_database_username }
    DATABASE_PASSWORD  = { value = var.render_database_password }
    SPRING_FLYWAY_URL  = { value = var.render_flyway_url }
    REDIS_URL          = { value = var.render_redis_url }
    JWT_SECRET         = { value = var.render_jwt_secret }
    FRONTEND_URL       = { value = var.render_frontend_url }
    BAZAARLINK_API_KEY = { value = var.render_bazaarlink_api_key }
    JAVA_TOOL_OPTIONS  = { value = "-Xms64m -Xmx256m -XX:+UseSerialGC" }
  }
}

output "backend_url" {
  description = "Public URL of the backend service."
  value       = render_web_service.web.url
}

output "backend_service_id" {
  description = "Render service ID for GitHub Actions."
  value       = render_web_service.web.id
}
