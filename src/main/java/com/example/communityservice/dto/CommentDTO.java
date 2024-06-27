package com.example.communityservice.dto;

import com.example.communityservice.model.Comment;

import java.time.LocalDateTime;

public class CommentDTO {

    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private String authorNickname;
    private String authorProfilePicture;
    private String authorEmail;
    private Long postId;

    public CommentDTO() {
    }

    public CommentDTO(Comment comment) {
        this.commentId = comment.getCommentId();
        this.content = comment.getCommentContent();
        this.createdAt = comment.getCreatedAt();
        this.authorNickname = comment.getUser().getNickname();
        this.authorProfilePicture = comment.getUser().getProfilePicture();
        this.authorEmail = comment.getUser().getEmail();
        this.postId = comment.getPost().getPostId();
    }

    // Getters and setters

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorProfilePicture() {
        return authorProfilePicture;
    }

    public void setAuthorProfilePicture(String authorProfilePicture) {
        this.authorProfilePicture = authorProfilePicture;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
