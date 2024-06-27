package com.example.communityservice.repository;

import com.example.communityservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentIdAndPost_PostId(Long commentId, Long postId);
    List<Comment> findByPost_PostId(Long postId);
}
