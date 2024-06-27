package com.example.communityservice.service;

import com.example.communityservice.dto.CommentDTO;
import com.example.communityservice.model.Comment;
import com.example.communityservice.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPost_PostId(postId);
        return comments.stream().map(CommentDTO::new).collect(Collectors.toList());
    }
}
