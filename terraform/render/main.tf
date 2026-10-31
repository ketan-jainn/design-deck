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
  default     = "singapore"
}

variable "render_database_url" {
  description = "Neon pooled JDBC URL with sslmode=require or verify-full."
  type        = string
  sensitive   = true
  validation {
    condition     = can(regex("^jdbc:postgresql://[^/]+/[^?]+\\?([^#]*&)?sslmode=(require|verify-ca|verify-full)(&|$)", var.render_database_url))
    error_message = "Provide a PostgreSQL JDBC URL with sslmode=require, verify-ca, or verify-full."
  }
}

variable "render_database_username" {
  type      = string
  sensitive = true
}

variable "render_database_password" {
  type      = string
  sensitive = true
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

variable "render_bazaarlink_model" {
  description = "BazaarLink model used for AI grading."
  type        = string
  default     = "qwen/qwen3.7-flash:free"
}

resource "render_keyvalue" "cache" {
  name              = "design-deck-key-value"
  plan              = "free"
  region            = var.render_region
  max_memory_policy = "noeviction"
  persistence_mode  = "off"
  ip_allow_list     = []
}

resource "render_web_service" "web" {
  name              = "design-deck-backend"
  plan              = "free"
  region            = var.render_region
  health_check_path = "/actuator/health/liveness"

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
    REDIS_URL          = { value = render_keyvalue.cache.connection_info.internal_connection_string }
    JWT_SECRET         = { value = var.render_jwt_secret }
    FRONTEND_URL       = { value = var.render_frontend_url }
    BAZAARLINK_API_KEY = { value = var.render_bazaarlink_api_key }
    BAZAARLINK_MODEL   = { value = var.render_bazaarlink_model }
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
