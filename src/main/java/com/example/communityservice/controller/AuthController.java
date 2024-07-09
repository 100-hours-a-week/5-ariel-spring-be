package com.example.communityservice.controller;

import com.example.communityservice.dto.UserDTO;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.UserRepository;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.FileStorageService;
import com.example.communityservice.service.JwtBlacklistService;
import com.example.communityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @GetMapping("/user-info")
    public ResponseEntity<UserDTO> getUserInfo(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUsername(token);
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserDTO userDTO = new UserDTO();
                userDTO.setUserId(user.getUserId());
                userDTO.setEmail(user.getEmail());
                userDTO.setNickname(user.getNickname());
                userDTO.setProfilePicture(user.getProfilePicture());
                return ResponseEntity.ok(userDTO);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/get-profile-image")
    public ResponseEntity<?> getProfileImage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String profileImagePath = "http://localhost:8080/uploads/" + user.getProfilePicture();
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

                String jwt = jwtTokenProvider.generateToken(authentication);
                return ResponseEntity.ok(jwt);
            } else {
                System.out.println("Password mismatch");
            }
        } else {
            System.out.println("User not found with email: " + user.getEmail());
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            jwtBlacklistService.blacklistToken(token);
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(400).body("Invalid token");
        }
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Map<String, String> response = new HashMap<>();

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.put("message", "Unauthorized access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = jwtTokenProvider.getUsername(token);
        try {
            boolean userDeleted = userService.deleteUserByEmail(email);
            if (userDeleted) {
                jwtBlacklistService.blacklistToken(token);
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Failed to withdraw user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
