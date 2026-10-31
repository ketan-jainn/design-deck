terraform {
  required_providers {
    vercel = {
      source  = "vercel/vercel"
      version = "~>0.3"
    }
  }
}

variable "vercel_team_id" {
  description = "ID of your existing Vercel Hobby team that will own the frontend project."
  type        = string
}

variable "vercel_api_base_url" {
  description = "HTTPS origin of the Render backend used by the production frontend."
  type        = string
  validation {
    condition     = can(regex("^https://[^/[:space:]]+/?$", var.vercel_api_base_url))
    error_message = "Provide the backend HTTPS origin, for example https://your-service.onrender.com."
  }
}

provider "vercel" {
  # Authentication is read from VERCEL_API_TOKEN, not stored in this file.
}

# This creates only a project. It does not create a team or change its plan.
resource "vercel_project" "design_deck_ui" {
  name           = "design-deck-ui"
  framework      = "nextjs"
  root_directory = "apps/web"
  team_id        = var.vercel_team_id
}

resource "vercel_project_environment_variable" "api_base_url" {
  project_id = vercel_project.design_deck_ui.id
  team_id    = var.vercel_team_id
  key        = "API_BASE_URL"
  value      = var.vercel_api_base_url
  target     = ["production"]
}

output "vercel_project_id" {
  description = "Project ID for the GitHub Actions deployment secret."
  value       = vercel_project.design_deck_ui.id
}

output "vercel_team_id" {
  description = "Team ID for the GitHub Actions deployment secret."
  value       = var.vercel_team_id
}
