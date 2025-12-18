package com.thentrees.gymhealthtech.config;

import com.thentrees.gymhealthtech.enums.UserStatus;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.repository.UserProfileRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

  private final PasswordEncoder passwordEncoder;

  @Bean
  ApplicationRunner applicationRunner(UserRepository userRepository, UserProfileRepository profileRepository) {
    log.info("Initializing application.....");
    return args -> {
      if (userRepository.findByEmailOrPhone("admin@gymmate.com").isEmpty()) {
        log.info("Creating default user.....");

        User admin = new User();
        admin.setStatus(UserStatus.ACTIVE);
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setEmailVerified(true);
        admin.setIsDeleted(false);
        admin.setEmail("admin@gymmate.com");
        admin.setProfileCompleted(true);

        UserProfile profile = new UserProfile();
        profile.setUser(admin);
        profile.setFullName("Admin User");
        admin.setProfile(profile);

        profileRepository.save(profile);
        userRepository.save(admin);
        log.warn("admin user has been created with default password: admin, please change it");
      }
      log.info("Application initialization completed .....");
    };
  }
}
