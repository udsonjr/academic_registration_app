package br.com.techne.lyceum.academic.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserDTO user) {}
