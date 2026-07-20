package br.com.techne.lyceum.academic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI academicRegistrationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Academic Registration API")
                        .description("REST API for academic enrollment management (Students, Courses, Subjects, Classes and Enrollments).")
                        .version("v1")
                        .contact(new Contact()
                                .name("Techne Lyceum")
                                .url("https://www.techne.com.br")));
    }
}
