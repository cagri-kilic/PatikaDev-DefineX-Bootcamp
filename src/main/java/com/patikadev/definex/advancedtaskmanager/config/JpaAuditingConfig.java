package com.patikadev.definex.advancedtaskmanager.config;

import com.patikadev.definex.advancedtaskmanager.security.CustomUserDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    private static final String SYSTEM_USER = "system@advancedtaskmanager.com";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(SYSTEM_USER);
            }

            String email = authentication.getName();

            if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails.getUsername());
            }

            return Optional.ofNullable(email).filter(e -> !e.isEmpty()).or(() -> Optional.of(SYSTEM_USER));
        };
    }
}