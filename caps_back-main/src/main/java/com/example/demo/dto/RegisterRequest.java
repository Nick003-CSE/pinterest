package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|org|in)$",
            message = "Email must be valid and end with .com, .org, or .in")
    private String email;

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-z0-9._-]{3,20}$",
            message = "Username must be 3-20 chars, lowercase letters, digits, or ._-")
    private String username;

    @NotBlank(message = "Full name is required")
    @Pattern(
            regexp = "^[A-Za-z ]{3,50}$",
            message = "Full name must be 3-50 characters and alphabetic")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\d{10,15}$",
            message = "Phone number must be 10-15 digits")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "Password must contain lowercase, uppercase, digit, and special character")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

}

