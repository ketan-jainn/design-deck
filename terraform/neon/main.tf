terraform {
  required_version = ">= 1.14.0"

  required_providers {
    neon = {
      source  = "kislerdm/neon"
      version = "~> 0.18.0"
    }
  }
}

provider "neon" {
  # The provider reads NEON_API_KEY from the environment.
}

variable "neon_project_id" {
  description = "ID of the existing Neon project to import into this state."
  type        = string
}

# Import the project already created in Neon instead of creating another one.
import {
  to = neon_project.design_deck
  id = var.neon_project_id
}

resource "neon_project" "design_deck" {
  # The provider defaults to 24 hours; Neon Free permits at most six hours.
  history_retention_seconds = 21600

  lifecycle {
    prevent_destroy = true
  }
}

output "neon_project_id" {
  description = "ID of the managed Neon project."
  value       = neon_project.design_deck.id
}
