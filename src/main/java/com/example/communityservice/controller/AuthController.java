package com.example.communityservice.controller;

import com.example.communityservice.model.User;
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

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user, HttpSession session) {
        Optional<User> existingUserOptional = userService.findByEmail(user.getEmail());
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            if (userService.checkPassword(existingUser, user.getPassword())) {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                session.setAttribute("loggedInUser", user.getEmail());

                return ResponseEntity.ok("Login successful");
            }
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
}
