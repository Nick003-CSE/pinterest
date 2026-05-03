package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pinterestOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080/api/v1");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.pinterest.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("support@pinterest.com");
        contact.setName("Pinterest API Support");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Pinterest Clone API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API documentation for Pinterest Clone application. " +
                        "This API provides endpoints for managing pins, boards, users, " +
                        "followers, invitations, business profiles, and more.")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

