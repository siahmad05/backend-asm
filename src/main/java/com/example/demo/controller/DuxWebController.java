package com.example.demo.controller;

import com.example.demo.dto.DetailsDoc2Request;
import com.example.demo.dto.KeycloakTokenResponse;
import com.example.demo.dto.UserByLoginRequest;
import com.example.demo.service.DuxWebApiService;
import java.io.IOException;
import com.example.demo.service.KeycloakTokenService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/duxweb")
public class DuxWebController {

    private final KeycloakTokenService tokenService;
    private final DuxWebApiService apiService;

    public DuxWebController(KeycloakTokenService tokenService, DuxWebApiService apiService) {
        this.tokenService = tokenService;
        this.apiService = apiService;
    }

    @GetMapping("/token")
    public KeycloakTokenResponse getToken() {
        return tokenService.getToken();
    }

    @GetMapping(value = "/stations/{stationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String viewStation(@PathVariable long stationId) {
        return apiService.viewStation(stationId);
    }

    @GetMapping(value = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocument(@PathVariable String documentId) {
        return apiService.getDocument(documentId);
    }

    @PostMapping(value = "/users/by-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> userByLogin(@RequestBody UserByLoginRequest request) {
        return apiService.userByLogin(request);
    }

    @PostMapping(value = "/details-doc2", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> detailsDoc2(@RequestBody DetailsDoc2Request request) throws IOException {
        return apiService.detailsDoc2(request);
    }
}
