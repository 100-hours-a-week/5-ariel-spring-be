package com.example.communityservice.controller;

import com.example.communityservice.dto.CommentDTO;
import com.example.communityservice.model.Comment;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.CommentRepository;
import com.example.communityservice.service.CommentService;
import com.example.communityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addComment(@RequestBody CommentDTO commentDTO) {
        try {
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            CommentDTO newComment = commentService.addComment(commentDTO, email);
            return ResponseEntity.ok(newComment);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\":false, \"error\":\"Failed to add comment\"}");
        }
    }
}
