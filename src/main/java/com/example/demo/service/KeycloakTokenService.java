package com.example.demo.service;

import com.example.demo.config.DuxWebProperties;
import com.example.demo.dto.KeycloakTokenResponse;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class KeycloakTokenService {

    private final RestClient restClient;
    private final DuxWebProperties properties;
    private KeycloakTokenResponse cachedToken;
    private Instant cachedTokenExpiresAt = Instant.EPOCH;

    public KeycloakTokenService(RestClient.Builder restClientBuilder, DuxWebProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public synchronized KeycloakTokenResponse getToken() {
        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiresAt)) {
            return cachedToken;
        }

        DuxWebProperties.Keycloak keycloak = properties.keycloak();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", keycloak.clientId());
        form.add("client_secret", keycloak.clientSecret());

        cachedToken = restClient.post()
                .uri(tokenUrl(keycloak))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
        cachedTokenExpiresAt = Instant.now().plusSeconds(tokenCacheSeconds(cachedToken));
        return cachedToken;
    }

    public boolean isCurrentAccessToken(String accessToken) {
        return accessToken != null && accessToken.equals(getToken().accessToken());
    }

    private long tokenCacheSeconds(KeycloakTokenResponse token) {
        if (token == null || token.expiresIn() == null) {
            return 0;
        }

        return Math.max(0, token.expiresIn() - 30);
    }

    private String tokenUrl(DuxWebProperties.Keycloak keycloak) {
        return trimTrailingSlash(keycloak.authBaseUrl())
                + "/realms/"
                + keycloak.realm()
                + "/protocol/openid-connect/token";
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
