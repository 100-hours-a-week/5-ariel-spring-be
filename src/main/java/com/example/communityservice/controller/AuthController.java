package com.example.communityservice.controller;

import com.example.communityservice.model.User;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.FileStorageService;
import com.example.communityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/get-profile-image")
    public ResponseEntity<?> getProfileImage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String profileImagePath = "http://localhost:8080/" + user.getProfilePicture();
            return ResponseEntity.ok().body(Map.of("profileImagePath", profileImagePath));
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        Optional<User> existingUserOptional = userService.findByEmail(user.getEmail());
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            if (userService.checkPassword(existingUser, user.getPassword())) {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String jwt = tokenProvider.generateToken(authentication);
                return ResponseEntity.ok(jwt);
            } else {
                System.out.println("Password mismatch");
            }
        } else {
            System.out.println("User not found with email: " + user.getEmail());
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("nickname") String nickname,
            @RequestParam("profilePicture") MultipartFile profilePicture) {
        try {
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Passwords do not match");
            }

            String fileName = fileStorageService.storeFile(profilePicture);

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setNickname(nickname);
            user.setProfilePicture(fileName);

            userService.save(user);

            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error registering user: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/list-of-posts")
    public ResponseEntity<String> listOfPosts() {
        return ResponseEntity.ok("List of posts page");
    }
}
