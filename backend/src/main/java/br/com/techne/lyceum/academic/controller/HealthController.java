package br.com.techne.lyceum.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "API health check")
public class HealthController {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            summary = "Backend status",
            description = "Returns a message indicating that the API is running")
    public String health() {
        return "Backend is running";
    }
}
