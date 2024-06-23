package com.example.communityservice.controller;

import com.example.communityservice.dto.PostDTO;
import com.example.communityservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // 추가된 부분

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long postId) {
        logger.debug("Fetching post with ID: {}", postId);
        PostDTO postDTO = postService.getPostById(postId);
        if (postDTO != null) {
            logger.debug("Post found: {}", postDTO);
            logger.debug("Post imagePath: {}, Post content: {}", postDTO.getPostImage(), postDTO.getPostContent());
            return ResponseEntity.ok(postDTO);
        } else {
            logger.debug("Post not found for ID: {}", postId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPost(@RequestParam String title,
                                             @RequestParam String content,
                                             @RequestParam(required = false) MultipartFile imageFile,
                                             HttpSession session) {
        String loggedInUserEmail = (String) session.getAttribute("loggedInUser");
        boolean success = postService.createPost(title, content, imageFile, loggedInUserEmail);
        if (success) {
            return ResponseEntity.ok("Post created successfully");
        } else {
            return ResponseEntity.status(500).body("Error creating post");
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId) {
        boolean success = postService.deletePost(postId);
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("success", false));
        }
    }

    @PostMapping("/update-post")
    public ResponseEntity<Map<String, Object>> updatePost(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null || !validateToken(token)) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Unauthorized"));
        }

        boolean success = postService.updatePost(id, title, content, imageFile);
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Error updating post"));
        }
    }


    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        // Implement your token validation logic here
        return true; // Return true if valid, false otherwise
    }
}
