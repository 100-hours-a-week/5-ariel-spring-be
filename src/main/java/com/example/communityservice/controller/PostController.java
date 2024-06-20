package com.example.communityservice.controller;

import com.example.communityservice.dto.PostDTO;
import com.example.communityservice.model.Post;
import com.example.communityservice.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post != null) {
            return ResponseEntity.ok(post);
        } else {
            return ResponseEntity.status(404).body(null);
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

    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePost(@RequestParam Long postId) {
        boolean success = postService.deletePost(postId);
        if (success) {
            return ResponseEntity.ok("Post deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Post not found");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePost(@RequestParam Long postId,
                                             @RequestParam String title,
                                             @RequestParam String content,
                                             @RequestParam(required = false) MultipartFile imageFile) {
        boolean success = postService.updatePost(postId, title, content, imageFile);
        if (success) {
            return ResponseEntity.ok("Post updated successfully");
        } else {
            return ResponseEntity.status(500).body("Error updating post");
        }
    }
}
