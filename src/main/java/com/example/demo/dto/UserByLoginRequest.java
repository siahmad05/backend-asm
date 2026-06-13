package com.example.demo.dto;

public record UserByLoginRequest(
        String version,
        boolean refresh,
        String login
) {
}
