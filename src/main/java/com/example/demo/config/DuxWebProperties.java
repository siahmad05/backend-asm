package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "duxweb")
public record DuxWebProperties(
        String apiBaseUrl,
        String stageApiBaseUrl,
        boolean trustAllSsl,
        Keycloak keycloak
) {
    public record Keycloak(
            String authBaseUrl,
            String realm,
            String clientId,
            String clientSecret
    ) {
    }
}
