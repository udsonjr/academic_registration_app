package br.com.techne.lyceum.academic.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body =
                Map.of(
                        "timestamp",
                        Instant.now().toString(),
                        "status",
                        401,
                        "error",
                        "Unauthorized",
                        "code",
                        "UNAUTHORIZED",
                        "message",
                        "Authentication is required",
                        "path",
                        request.getRequestURI(),
                        "traceId",
                        UUID.randomUUID().toString().substring(0, 8),
                        "details",
                        List.of());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
