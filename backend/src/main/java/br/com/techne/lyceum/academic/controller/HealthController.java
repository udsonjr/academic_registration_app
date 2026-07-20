package br.com.techne.lyceum.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Verificação de disponibilidade da API")
public class HealthController {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Status do backend", description = "Retorna uma mensagem indicando que a API está no ar")
    public String health() {
        return "Backend funcionando";
    }
}
