package com.example.communityservice.controller;

import com.example.communityservice.dto.CommentDTO;
import com.example.communityservice.model.Comment;
import com.example.communityservice.model.Post;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.CommentRepository;
import com.example.communityservice.repository.PostRepository;
import com.example.communityservice.repository.UserRepository;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.CommentService;
import com.example.communityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteComment(@RequestBody CommentDTO commentDTO, @AuthenticationPrincipal User currentUser) {
        Optional<Comment> commentOptional = commentRepository.findByCommentIdAndPost_PostId(commentDTO.getCommentId(), commentDTO.getPostId());

        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            commentRepository.delete(comment);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Comment deleted successfully\"}");
        } else {
            return ResponseEntity.status(404).body("{\"success\": false, \"message\": \"Comment not found\"}");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateComment(@RequestBody CommentDTO commentDTO) {
        try {
            boolean isUpdated = commentService.updateComment(commentDTO);
            if (isUpdated) {
                return ResponseEntity.ok().body("{\"success\":true}");
            } else {
                return ResponseEntity.status(404).body("{\"success\":false, \"error\":\"Comment not found\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\":false, \"error\":\"Failed to update comment\"}");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUsername(token);
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                try {
                    Optional<Post> postOptional = postRepository.findById(commentDTO.getPostId());
                    if (postOptional.isPresent()) {
                        Post post = postOptional.get();
                        Comment comment = new Comment();
                        comment.setCommentContent(commentDTO.getContent());
                        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
                        comment.setCreatedAt(now);
                        comment.setUser(userOptional.get());
                        comment.setPost(post);
                        commentRepository.save(comment);

                        return ResponseEntity.ok(Collections.singletonMap("message", "Comment added successfully"));
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Post not found"));
                    }
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error adding comment: " + e.getMessage()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Unauthorized"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Unauthorized"));
        }
    }

}
