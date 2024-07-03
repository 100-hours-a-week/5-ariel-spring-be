package com.example.communityservice.controller;

import com.example.communityservice.dto.UserDTO;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.UserRepository;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.FileStorageService;
import com.example.communityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
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
    private UserRepository userRepository;

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
