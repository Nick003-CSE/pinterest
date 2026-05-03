package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.OtpInitResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.entity.Follower;
import com.example.demo.entity.FollowerId;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest baseRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setUsername("testuser");
        req.setFullName("Test User");
        req.setPhoneNumber("1234567890");
        req.setPassword("Pass@1234");
        req.setConfirmPassword("Pass@1234");
        return req;
    }

    @BeforeEach
    void setup() {
        lenient().when(passwordEncoder.encode(any())).thenReturn("encoded-password");
    }

    @Test
    void register_shouldThrow_whenPasswordsDoNotMatch() {
        RegisterRequest request = baseRegisterRequest();
        request.setConfirmPassword("Different@123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = baseRegisterRequest();
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email is already in use");
    }

    @Test
    void register_shouldThrow_whenUsernameAlreadyExists() {
        RegisterRequest request = baseRegisterRequest();
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username is already in use");
    }

    @Test
    void register_shouldSaveUserAndInitializeFollowers_onSuccess() {
        RegisterRequest request = baseRegisterRequest();

        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userAccountRepository.count()).thenReturn(0L);

        UserAccount saved = new UserAccount();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        saved.setUsername(request.getUsername());
        saved.setFullName(request.getFullName());

        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);

        // Seed users for initializeDefaultFollowers
        UserAccount seed1 = new UserAccount();
        seed1.setId(2L);
        seed1.setUsername("markzuckerberg");

        UserAccount seed2 = new UserAccount();
        seed2.setId(3L);
        seed2.setUsername("janedoe");

        when(userAccountRepository.existsById(1L)).thenReturn(true);
        when(userAccountRepository.findByUsername("markzuckerberg")).thenReturn(Optional.of(seed1));
        when(userAccountRepository.findByUsername("janedoe")).thenReturn(Optional.of(seed2));
        when(userAccountRepository.findByUsername("elonmusk")).thenReturn(Optional.empty());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(saved));

        AuthResponse mapped = new AuthResponse();
        mapped.setUserId(1L);
        when(modelMapper.map(any(UserAccount.class), eq(AuthResponse.class))).thenReturn(mapped);

        AuthResponse response = authService.register(request);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMessage()).contains("Registration successful");
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void startPasswordReset_shouldThrow_whenPhoneNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setPhoneNumber("9999999999");

        when(userAccountRepository.findByPhoneNumber(request.getPhoneNumber()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.startPasswordReset(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No account found with this phone number");
    }

    @Test
    void startPasswordReset_shouldPersistOtpAndReturnIt() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setPhoneNumber("1234567890");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);

        OtpInitResponse response = authService.startPasswordReset(request);

        verify(userAccountRepository).save(userCaptor.capture());
        UserAccount saved = userCaptor.getValue();

        assertThat(saved.getResetOtp()).isNotNull();
        assertThat(saved.getResetOtp().length()).isEqualTo(6);
        assertThat(saved.getResetOtpExpiresAt()).isAfter(Instant.now());

        assertThat(response).isNotNull();
        assertThat(response.getOtp()).isEqualTo(saved.getResetOtp());
        assertThat(response.getMessage()).contains("OTP generated successfully");
    }

    @Test
    void resetPassword_shouldThrow_whenPasswordsDoNotMatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("123456");
        request.setNewPassword("Pass@1234");
        request.setConfirmPassword("Different@123");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void resetPassword_shouldThrow_whenOtpInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("111111");
        request.setNewPassword("Pass@1234");
        request.setConfirmPassword("Pass@1234");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");
        user.setResetOtp("222222");
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300));

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid or expired OTP");

        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void resetPassword_shouldEncodePasswordAndClearOtp_onSuccess() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("123456");
        request.setNewPassword("Pass@1234");
        request.setConfirmPassword("Pass@1234");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");
        user.setResetOtp("123456");
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300));

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        authService.resetPassword(request);

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());

        UserAccount saved = userCaptor.getValue();
        verify(passwordEncoder).encode(eq("Pass@1234"));
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.getResetOtp()).isNull();
        assertThat(saved.getResetOtpExpiresAt()).isNull();
    }

    @Test
    void resetPassword_shouldThrow_whenPhoneNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPhoneNumber("0000000000");
        request.setOtp("123456");
        request.setNewPassword("Pass@1234");
        request.setConfirmPassword("Pass@1234");

        when(userAccountRepository.findByPhoneNumber("0000000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No account found with this phone number");
    }

    @Test
    void resetPassword_shouldThrow_whenOtpExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("123456");
        request.setNewPassword("Pass@1234");
        request.setConfirmPassword("Pass@1234");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");
        user.setResetOtp("123456");
        user.setResetOtpExpiresAt(Instant.now().minusSeconds(10));

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verifyResetOtp_shouldThrow_whenPhoneNotFound() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("0000000000");
        request.setOtp("123456");

        when(userAccountRepository.findByPhoneNumber("0000000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyResetOtp(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No account found with this phone number");
    }

    @Test
    void verifyResetOtp_shouldThrow_whenOtpInvalidOrExpired() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("111111");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");
        user.setResetOtp("222222");
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300));

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyResetOtp(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verifyResetOtp_shouldPass_whenOtpValid() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("1234567890");
        request.setOtp("123456");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setPhoneNumber("1234567890");
        user.setResetOtp("123456");
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300));

        when(userAccountRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(user));

        authService.verifyResetOtp(request);
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("Pass@1234");

        when(userAccountRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrow_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded-password");

        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldReturnAuthResponse_onSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Pass@1234");

        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("encoded-password");

        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Pass@1234", "encoded-password")).thenReturn(true);

        // initializeDefaultFollowers dependencies
        when(userAccountRepository.existsById(1L)).thenReturn(true);

        UserAccount seed1 = new UserAccount();
        seed1.setId(2L);
        seed1.setUsername("markzuckerberg");

        when(userAccountRepository.findByUsername("markzuckerberg")).thenReturn(Optional.of(seed1));
        when(userAccountRepository.findByUsername("janedoe")).thenReturn(Optional.empty());
        when(userAccountRepository.findByUsername("elonmusk")).thenReturn(Optional.empty());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));

        when(followerRepository.existsById(any(FollowerId.class))).thenReturn(false);

        AuthResponse mapped = new AuthResponse();
        mapped.setUserId(1L);
        when(modelMapper.map(any(UserAccount.class), eq(AuthResponse.class))).thenReturn(mapped);

        AuthResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMessage()).contains("Login successful");
    }

    @Test
    void initializeDefaultFollowers_shouldThrow_whenUserNotFound() {
        when(userAccountRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> authService.initializeDefaultFollowers(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void initializeDefaultFollowers_shouldThrow_whenNoSeedUsers() {
        when(userAccountRepository.existsById(1L)).thenReturn(true);
        when(userAccountRepository.findByUsername("markzuckerberg")).thenReturn(Optional.empty());
        when(userAccountRepository.findByUsername("janedoe")).thenReturn(Optional.empty());
        when(userAccountRepository.findByUsername("elonmusk")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.initializeDefaultFollowers(1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("No seed users found");
    }

    @Test
    void initializeDefaultFollowers_shouldCreateBidirectionalRelations_whenSeedUsersExist() {
        Long userId = 1L;

        when(userAccountRepository.existsById(userId)).thenReturn(true);

        UserAccount seedUser = new UserAccount();
        seedUser.setId(2L);
        seedUser.setUsername("markzuckerberg");

        when(userAccountRepository.findByUsername("markzuckerberg")).thenReturn(Optional.of(seedUser));
        when(userAccountRepository.findByUsername("janedoe")).thenReturn(Optional.empty());
        when(userAccountRepository.findByUsername("elonmusk")).thenReturn(Optional.empty());

        UserAccount mainUser = new UserAccount();
        mainUser.setId(userId);

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(mainUser));
        when(followerRepository.existsById(any(FollowerId.class))).thenReturn(false);

        authService.initializeDefaultFollowers(userId);

        verify(followerRepository, org.mockito.Mockito.atLeastOnce()).save(any(Follower.class));
    }
}


