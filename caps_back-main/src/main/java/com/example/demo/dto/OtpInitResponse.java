package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpInitResponse {

    private String message;
    /**
     * For demo purposes only, we return the OTP in the response
     * so that the frontend can display it instead of integrating SMS.
     */
    private String otp;
}


