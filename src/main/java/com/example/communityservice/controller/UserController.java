package com.example.communityservice.controller;

import com.example.communityservice.dto.PasswordUpdateRequest;
import com.example.communityservice.dto.UserDTO;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.UserRepository;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.FileStorageService;
import com.example.communityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 사용자 정보 수정
    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @RequestParam("newNickname") String newNickname,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtTokenProvider.getUsername(token);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNickname(newNickname);

            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String fileName = fileStorageService.storeFile(profileImage);
                    user.setProfilePicture(fileName);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile picture: " + e.getMessage());
                }
            }

            userService.save(user);
            return ResponseEntity.ok("Profile updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


    // 비밀번호 변경
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            logger.warn("Unauthorized access attempt");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unauthorized access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = jwtTokenProvider.getUsername(token);
        Optional<User> userOptional = userService.findByEmail(email);
        if (!userOptional.isPresent()) {
            logger.warn("User not found: {}", email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userOptional.get();
        if (!passwordUpdateRequest.getNewPassword().equals(passwordUpdateRequest.getConfirmNewPassword())) {
            logger.warn("Passwords do not match for user: {}", email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Passwords do not match");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String encodedPassword = passwordEncoder.encode(passwordUpdateRequest.getNewPassword());
            user.setPassword(encodedPassword);
            userService.save(user);
            logger.info("Password updated successfully for user: {}", email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating password for user: {}", email, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Server error. Please contact the administrator.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    // 사용자 프로필 사진 가져오기
    @GetMapping("/get-profile-image")
    public ResponseEntity<?> getProfileImage(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getProfilePicture() != null) {
            return ResponseEntity.ok(Map.of("profileImagePath", currentUser.getProfilePicture()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile image not found");
        }
    }
}
