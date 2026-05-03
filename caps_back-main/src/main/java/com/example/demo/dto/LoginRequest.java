package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class    LoginRequest {

    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|org|in)$",
            message = "Email must be valid and end with .com, .org, or .in")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

}

