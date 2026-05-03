package com.example.demo.service;

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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final FollowerRepository followerRepository;
    private final Random random = new Random();

    // Predefined seed usernames for default followers/following
    private static final List<String> DEFAULT_USERNAMES = Arrays.asList(
            "markzuckerberg", "janedoe", "elonmusk"
    );

    // Round-robin profile picture URLs (8 total)
    private static final List<String> PROFILE_PICTURE_URLS = Arrays.asList(
            "https://d2v5dzhdg4zhx3.cloudfront.net/web-assets/images/storypages/short/linkedin-profile-picture-maker/dummy_image/thumb/004.webp",
            "https://img.freepik.com/premium-photo/captivating-black-white-linkedin-profile-picture-fitness-writerjournalist_983420-47941.jpg?w=2000",
            "https://cdn.openart.ai/stable_diffusion/0f26305f30636c01ed2b3ab7ed4117079f7b0796_2000x2000.webp",
            "https://i.pinimg.com/originals/07/33/ba/0733ba760b29378474dea0fdbcb97107.png",
            "https://i.pinimg.com/736x/ee/34/34/ee34347211a3d0744667a86096949da8.jpg",
            "https://imgv3.fotor.com/images/gallery/a-man-profile-picture-with-blue-and-green-background-made-by-LinkedIn-Profile-Picture-Maker.jpg",
            "https://assets-global.website-files.com/639ff8596ae419fae300b099/641017314cc67fbb88c517a7_good-linkedin-profile-photo-right-expression-1000x1000.jpeg",
            "https://i.pinimg.com/originals/67/28/eb/6728ebffa5cbcb6e05ec5403b8e4e835.png"
    );

    @Autowired
    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper,
            FollowerRepository followerRepository) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.followerRepository = followerRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessValidationException("Passwords do not match");
        }

        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already in use");
        }

        UserAccount user = new UserAccount();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Assign profile picture in round-robin fashion
        long totalUsers = userAccountRepository.count();
        int pictureIndex = (int) (totalUsers % PROFILE_PICTURE_URLS.size());
        user.setAvatarUrl(PROFILE_PICTURE_URLS.get(pictureIndex));

        UserAccount saved = userAccountRepository.save(user);

        // Initialize predefined followers and following for new user
        initializeDefaultFollowers(saved.getId());

        AuthResponse response = modelMapper.map(saved, AuthResponse.class);
        response.setMessage("Registration successful");
        return response;
    }

    /**
     * Starts the forgot-password flow by generating and storing an OTP
     * against the user account. For demo purposes we return the OTP so
     * the frontend can show it instead of integrating SMS.
     */
    @Transactional
    public OtpInitResponse startPasswordReset(ForgotPasswordRequest request) {
        UserAccount user =
                userAccountRepository
                        .findByPhoneNumber(request.getPhoneNumber())
                        .orElseThrow(() -> new ResourceNotFoundException("No account found with this phone number"));

        String otp = String.format("%06d", random.nextInt(1_000_000));
        user.setResetOtp(otp);
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300)); // 5 minutes
        userAccountRepository.save(user);

        return new OtpInitResponse(
                "OTP generated successfully. For demo purposes, it is returned in the response.", otp);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user =
                userAccountRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Initialize default followers/following if user doesn't have any
        initializeDefaultFollowers(user.getId());

        AuthResponse response = modelMapper.map(user, AuthResponse.class);
        response.setMessage("Login successful");
        return response;
    }

    @Transactional(readOnly = true)
    public void verifyResetOtp(VerifyOtpRequest request) {
        UserAccount user =
                userAccountRepository
                        .findByPhoneNumber(request.getPhoneNumber())
                        .orElseThrow(() -> new ResourceNotFoundException("No account found with this phone number"));

        if (user.getResetOtp() == null
                || user.getResetOtpExpiresAt() == null
                || Instant.now().isAfter(user.getResetOtpExpiresAt())
                || !user.getResetOtp().equals(request.getOtp())) {
            throw new BusinessValidationException("Invalid or expired OTP");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessValidationException("Passwords do not match");
        }

        UserAccount user =
                userAccountRepository
                        .findByPhoneNumber(request.getPhoneNumber())
                        .orElseThrow(() -> new ResourceNotFoundException("No account found with this phone number"));

        if (user.getResetOtp() == null
                || user.getResetOtpExpiresAt() == null
                || Instant.now().isAfter(user.getResetOtpExpiresAt())
                || !user.getResetOtp().equals(request.getOtp())) {
            throw new BusinessValidationException("Invalid or expired OTP");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtp(null);
        user.setResetOtpExpiresAt(null);
        userAccountRepository.save(user);
    }

    /**
     * Initializes predefined followers and following relationships for a user.
     * This ensures every user has some default connections when they sign in.
     * Can be called manually for existing users.
     */
    @Transactional
    public void initializeDefaultFollowers(Long userId) {
        // Verify user exists
        if (!userAccountRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        
        // Get all predefined seed users
        List<UserAccount> defaultUsers = DEFAULT_USERNAMES.stream()
                .map(username -> userAccountRepository.findByUsername(username))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        if (defaultUsers.isEmpty()) {
            throw new BusinessValidationException("No seed users found in database. Please ensure seed users (markzuckerberg, janedoe, elonmusk) exist.");
        }

        // Create bidirectional relationships:
        // 1. User follows seed users (appears in following list)
        // 2. Seed users follow the user (appears in followers list)
        for (UserAccount seedUser : defaultUsers) {
            if (seedUser.getId().equals(userId)) {
                continue; // Skip self
            }

            // User follows seed user
            FollowerId followingId = new FollowerId(userId, seedUser.getId());
            if (!followerRepository.existsById(followingId)) {
                Follower following = new Follower();
                following.setId(followingId);
                following.setFollower(userAccountRepository.findById(userId).orElseThrow());
                following.setFollowing(seedUser);
                followerRepository.save(following);
            }

            // Seed user follows the user (so they appear in followers list)
            FollowerId followerId = new FollowerId(seedUser.getId(), userId);
            if (!followerRepository.existsById(followerId)) {
                Follower follower = new Follower();
                follower.setId(followerId);
                follower.setFollower(seedUser);
                follower.setFollowing(userAccountRepository.findById(userId).orElseThrow());
                followerRepository.save(follower);
            }
        }
    }
}

