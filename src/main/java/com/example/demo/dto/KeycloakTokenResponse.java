package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("refresh_expires_in") Long refreshExpiresIn,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("not-before-policy") Long notBeforePolicy,
        @JsonProperty("scope") String scope
) {
}
