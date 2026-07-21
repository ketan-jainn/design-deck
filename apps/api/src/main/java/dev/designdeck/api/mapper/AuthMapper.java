package dev.designdeck.api.mapper;

import dev.designdeck.api.dto.profile.ProfileDto;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
  public ProfileDto toProfileDto(AppUser user, Profile profile) {
    return new ProfileDto(user.getEmail(), profile.getDisplayName(), profile.getDailyGoal(), profile.getStreakCount());
  }
}
