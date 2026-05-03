package com.example.demo.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.ErrorResponse;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationErrors_shouldReturnBadRequest_withFieldErrors() {
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Field 1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field 2 is invalid");

        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        org.mockito.Mockito.when(bindingResult.getFieldErrors())
                .thenReturn(java.util.Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).contains("Field 1 is required", "Field 2 is invalid");
    }

    @Test
    void handleBusinessValidation_shouldReturnBadRequest() {
        BusinessValidationException ex = new BusinessValidationException("Invalid operation");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid operation");
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleConflict_shouldReturnConflict() {
        ConflictException ex = new ConflictException("Resource already exists");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Resource already exists");
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleInvalidCredentials_shouldReturnUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleNotFound_shouldReturnNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleNoHandlerFound_shouldReturnNotFound() throws Exception {
        NoHandlerFoundException ex = org.mockito.Mockito.mock(NoHandlerFoundException.class);
        org.mockito.Mockito.when(ex.getRequestURL()).thenReturn("/api/invalid/endpoint");

        ResponseEntity<ErrorResponse> response = handler.handleNoHandlerFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("/api/invalid/endpoint");
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleGeneric_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error occurred");
        assertThat(response.getBody().getDetails()).hasSize(1);
        assertThat(response.getBody().getDetails().get(0)).isEqualTo("Unexpected error");
    }

    @Test
    void handleGeneric_shouldHandleExceptionWithEmptyMessage() {
        Exception ex = new RuntimeException("");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error occurred");
        assertThat(response.getBody().getDetails()).hasSize(1);
    }

    @Test
    void handleValidationErrors_shouldHandleEmptyFieldErrors() {
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);

        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        org.mockito.Mockito.when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getDetails()).isEmpty();
    }
}

