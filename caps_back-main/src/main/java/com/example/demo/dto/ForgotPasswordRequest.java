package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\d{10,15}$",
            message = "Phone number must be 10-15 digits")
    private String phoneNumber;
}


