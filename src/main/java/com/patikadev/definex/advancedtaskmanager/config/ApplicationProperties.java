package com.patikadev.definex.advancedtaskmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private Security security = new Security();
    private File file = new File();

    @Getter
    @Setter
    public static class Security {
        private String tokenSecret;
        private long tokenExpirationMs;
        private long tokenRefreshExpirationMs;
        private String jwtIssuer;
        private String jwtAudience;
    }

    @Getter
    @Setter
    public static class File {
        private String uploadDir;
        private String taskAttachmentsDir;
        private String tempDir;
        private long maxFileSize;
    }
} 