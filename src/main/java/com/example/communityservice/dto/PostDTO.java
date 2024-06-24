package com.example.communityservice.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class PostDTO {
    private Long postId;
    private String title;
    private String postContent;
    private String postImage;
    private Date createdAt;
    private String authorNickname;
    private String authorProfilePicture;
    private String authorEmail;  // 추가된 부분
    private int likesCount;
    private int commentsCount;
    private int viewsCount;
    private UserDTO user;

    public PostDTO() {
    }

    public PostDTO(Long postId, String title, String postContent, String postImage, Date createdAt, String authorNickname, String authorProfilePicture, String authorEmail, int commentsCount, int viewsCount) {
        this.postId = postId;
        this.title = title;
        this.postContent = postContent;
        this.postImage = postImage;
        this.createdAt = createdAt;
        this.authorNickname = authorNickname;
        this.authorProfilePicture = authorProfilePicture;
        this.authorEmail = authorEmail;  // 추가된 부분
        this.commentsCount = commentsCount;
        this.viewsCount = viewsCount;
    }

    // Getters and setters

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
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

    public String getAuthorEmail() {  // 추가된 부분
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {  // 추가된 부분
        this.authorEmail = authorEmail;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
