package com.example.communityservice.service;

import com.example.communityservice.dto.PostDTO;
import com.example.communityservice.dto.UserDTO;
import com.example.communityservice.model.Post;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.PostRepository;
import com.example.communityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PostDTO getPostById(Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            User user = post.getUser();
            return new PostDTO(post.getPostId(), post.getTitle(), post.getPostContent(), post.getPostImage(), post.getCreatedAt(), user.getNickname(), user.getProfilePicture(), user.getEmail(), post.getCommentsCount(), post.getViewsCount());
        } else {
            return null;
        }
    }


    private PostDTO convertToDto(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setPostId(post.getPostId());
        postDTO.setTitle(post.getTitle());
        postDTO.setPostContent(post.getPostContent());
        postDTO.setPostImage(post.getPostImage());
        postDTO.setCreatedAt(post.getCreatedAt());
        postDTO.setLikesCount(post.getLikesCount());
        postDTO.setCommentsCount(post.getCommentsCount());
        postDTO.setViewsCount(post.getViewsCount());

        User user = post.getUser();
        postDTO.setAuthorNickname(user.getNickname());
        postDTO.setAuthorProfilePicture(user.getProfilePicture());

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setEmail(user.getEmail());
        userDTO.setNickname(user.getNickname());
        userDTO.setProfilePicture(user.getProfilePicture());

        postDTO.setUser(userDTO);

        return postDTO;
    }

    public boolean createPost(String title, String content, MultipartFile imageFile, String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String imagePath = imageFile != null ? fileStorageService.storeFile(imageFile) : null;

            Post post = new Post();
            post.setTitle(title);
            post.setPostContent(content);
            post.setPostImage(imagePath);
            post.setUser(user);

            postRepository.save(post);
            return true;
        }
        return false;
    }

    public boolean deletePost(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent()) {
            postRepository.delete(post.get());
            return true;
        }
        return false;
    }

    public boolean updatePost(Long postId, String title, String content, MultipartFile imageFile) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            post.setTitle(title);
            post.setPostContent(content);

            if (imageFile != null) {
                String imagePath = fileStorageService.storeFile(imageFile);
                post.setPostImage(imagePath);
            }

            postRepository.save(post);
            return true;
        }
        return false;
    }
}