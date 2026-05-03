package com.example.demo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.OtpInitResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("Pass@1234");
        request.setConfirmPassword("Pass@1234");
        request.setFullName("Test User");
        request.setPhoneNumber("1234567890");

        AuthResponse response = new AuthResponse();
        response.setMessage("Registration successful");
        response.setUserId(1L);
        response.setUsername("testuser");
        response.setFullName("Test User");
        response.setPhoneNumber("1234567890");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc()
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldHandleMissingRequiredFields() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        // Missing username, password, etc.

        mockMvc()
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnOkAndMessage() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful");
        response.setUserId(1L);
        response.setUsername("testuser");
        response.setFullName("Test User");

        when(authService.login(any())).thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "Pass@1234"
                }
                """;

        mockMvc()
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(authService).login(any());
    }

    @Test
    void login_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldHandleMissingFields() throws Exception {
        String body = """
                {
                  "email": "test@example.com"
                }
                """;

        mockMvc()
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPasswordResetOtp_shouldReturnOtpInBody() throws Exception {
        OtpInitResponse otpResponse =
                new OtpInitResponse("OTP generated successfully", "123456");
        when(authService.startPasswordReset(any())).thenReturn(otpResponse);

        String body = """
                {
                  "phoneNumber": "1234567890"
                }
                """;

        mockMvc()
                .perform(post("/auth/forgot-password/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("OTP generated successfully")))
                .andExpect(jsonPath("$.otp").value("123456"));

        verify(authService).startPasswordReset(any());
    }

    @Test
    void requestPasswordResetOtp_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(post("/auth/forgot-password/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPasswordResetOtp_shouldHandleMissingPhoneNumber() throws Exception {
        String body = "{}";

        mockMvc()
                .perform(post("/auth/forgot-password/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyResetOtp_shouldReturnSuccess() throws Exception {
        doNothing().when(authService).verifyResetOtp(any());

        String body = """
                {
                  "phoneNumber": "1234567890",
                  "otp": "123456"
                }
                """;

        mockMvc()
                .perform(post("/auth/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP verified successfully"));

        verify(authService).verifyResetOtp(any());
    }

    @Test
    void verifyResetOtp_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(post("/auth/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(authService).resetPassword(any());

        String body = """
                {
                  "phoneNumber": "1234567890",
                  "otp": "123456",
                  "newPassword": "Pass@1234",
                  "confirmPassword": "Pass@1234"
                }
                """;

        mockMvc()
                .perform(post("/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(authService).resetPassword(any());
    }

    @Test
    void resetPassword_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(post("/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldHandleMissingFields() throws Exception {
        String body = """
                {
                  "phoneNumber": "1234567890"
                }
                """;

        mockMvc()
                .perform(post("/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
