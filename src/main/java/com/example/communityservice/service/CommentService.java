package com.example.communityservice.service;

import com.example.communityservice.dto.CommentDTO;
import com.example.communityservice.model.Comment;
import com.example.communityservice.model.Post;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.CommentRepository;
import com.example.communityservice.repository.PostRepository;
import com.example.communityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPost_PostId(postId);
        return comments.stream().map(CommentDTO::new).collect(Collectors.toList());
    }

    public boolean deleteComment(Long commentId, Long postId) {
        Optional<Comment> commentOpt = commentRepository.findByCommentIdAndPost_PostId(commentId, postId);
        if (commentOpt.isPresent()) {
            commentRepository.delete(commentOpt.get());
            return true;
        }
        return false;
    }

    public boolean updateComment(CommentDTO commentDTO) {
        Optional<Comment> commentOpt = commentRepository.findByCommentIdAndPost_PostId(commentDTO.getCommentId(), commentDTO.getPostId());
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setCommentContent(commentDTO.getContent());
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    public CommentDTO addComment(CommentDTO commentDTO, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(commentDTO.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setCommentContent(commentDTO.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setPost(post);
        comment.setUser(user);
        comment = commentRepository.save(comment);

        CommentDTO newCommentDTO = new CommentDTO(comment);
        newCommentDTO.setAuthorEmail(user.getEmail());
        newCommentDTO.setAuthorNickname(user.getNickname());
        newCommentDTO.setAuthorProfilePicture(user.getProfilePicture());
        return newCommentDTO;
    }
}
