package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.config.DuxWebProperties;
import com.example.demo.dto.DetailsDoc2Request;
import com.example.demo.dto.KeycloakTokenResponse;
import com.example.demo.dto.UserByLoginRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DuxWebApiService {

    private final RestClient restClient;
    private final DuxWebProperties properties;
    private final KeycloakTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DuxWebApiService(
            RestClient.Builder restClientBuilder,
            DuxWebProperties properties,
            KeycloakTokenService tokenService
    ) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
        this.tokenService = tokenService;
    }

    public String viewStation(long stationId) {
        KeycloakTokenResponse token = tokenService.getToken();

        return restClient.get()
                .uri(trimTrailingSlash(properties.apiBaseUrl()) + "/api/Station/viewStation/" + stationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);
    }

    public ResponseEntity<String> getDocument(String documentId) {
        KeycloakTokenResponse token = tokenService.getToken();

        return restClient.get()
                .uri(trimTrailingSlash(properties.stageApiBaseUrl()) + "/api/Document/getDocument/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .exchange((request, response) -> {
                    String body = readBody(response);
                    return ResponseEntity
                            .status(response.getStatusCode())
                            .contentType(response.getHeaders().getContentType())
                            .body(body);
                });
    }

    public ResponseEntity<String> userByLogin(UserByLoginRequest requestBody) {
        KeycloakTokenResponse token = tokenService.getToken();

        return restClient.post()
                .uri(trimTrailingSlash(properties.stageApiBaseUrl()) + "/api/Activite/Userbylogin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((request, response) -> {
                    String body = readBody(response);
                    return ResponseEntity
                            .status(response.getStatusCode())
                            .contentType(response.getHeaders().getContentType())
                            .body(body);
                });
    }

    public ResponseEntity<String> detailsDoc2(DetailsDoc2Request requestBody) throws IOException {
        KeycloakTokenResponse token = tokenService.getToken();
        String idTier = getUserIdByLogin(token, requestBody.login());

        URI uri = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(properties.stageApiBaseUrl()))
                .pathSegment(
                        "api",
                        "DetailsDoc2",
                        requestBody.from(),
                        requestBody.to(),
                        idTier,
                        requestBody.repres(),
                        requestBody.codeDoc(),
                        requestBody.idEtat(),
                        Boolean.toString(requestBody.all()),
                        Boolean.toString(requestBody.allDocuments()),
                        detailsDocIdArticle(requestBody.idArticle()),
                        Boolean.toString(requestBody.affichAvanc())
                )
                .build()
                .encode()
                .toUri();

        return restClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(detailsDoc2Body())
                .exchange((request, response) -> {
                    String body = readBody(response);
                    return ResponseEntity
                            .status(response.getStatusCode())
                            .contentType(response.getHeaders().getContentType())
                            .body(body);
                });
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String detailsDocIdArticle(String idArticle) {
        if (idArticle == null || idArticle.isBlank() || "null".equalsIgnoreCase(idArticle)) {
            return "null";
        }

        return idArticle;
    }

    private Map<String, Object> detailsDoc2Body() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("first", 0);
        event.put("rows", 20);
        event.put("sortOrder", 1);
        event.put("filters", Map.of());
        event.put("globalFilter", null);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idDocCommercial", List.of());
        body.put("idTierModal", null);
        body.put("event", event);
        return body;
    }

    private String readBody(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    }

    private String getUserIdByLogin(KeycloakTokenResponse token, String login) throws IOException {
        String body = restClient.post()
                .uri(trimTrailingSlash(properties.stageApiBaseUrl()) + "/api/Activite/Userbylogin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserByLoginRequest("Default", true, login))
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(body);
        JsonNode idNode = root.path("0").path("id");
        if (idNode.isMissingNode() || idNode.isNull() || idNode.asText().isBlank()) {
            throw new IllegalStateException("User id not found for login: " + login);
        }

        return idNode.asText();
    }
}
