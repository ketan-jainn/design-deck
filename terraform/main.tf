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

output "vercel_project_id" {
  description = "Project ID for the GitHub Actions deployment secret."
  value       = vercel_project.design_deck_ui.id
}

output "vercel_team_id" {
  description = "Team ID for the GitHub Actions deployment secret."
  value       = var.vercel_team_id
}
