package com.example.communityservice.repository;

import com.example.communityservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p, COUNT(c) as commentsCount FROM Post p LEFT JOIN p.comments c GROUP BY p")
    List<Object[]> findAllPostsWithCommentsCount();

    @Query("SELECT p, COUNT(c) as commentsCount FROM Post p LEFT JOIN p.comments c WHERE p.postId = :postId GROUP BY p")
    Optional<Object[]> findPostWithCommentsCountById(Long postId);
}
