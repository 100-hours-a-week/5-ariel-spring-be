package com.example.communityservice.controller;

import com.example.communityservice.dto.CommentDTO;
import com.example.communityservice.dto.PostDTO;
import com.example.communityservice.dto.UserDTO;
import com.example.communityservice.model.Post;
import com.example.communityservice.model.User;
import com.example.communityservice.repository.PostRepository;
import com.example.communityservice.repository.UserRepository;
import com.example.communityservice.security.JwtTokenProvider;
import com.example.communityservice.service.CommentService;
import com.example.communityservice.service.FileStorageService;
import com.example.communityservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // 추가된 부분

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

//    @GetMapping
//    public ResponseEntity<List<PostDTO>> getAllPosts() {
//        List<PostDTO> posts = postService.getAllPosts();
//        return ResponseEntity.ok(posts);
//    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getPosts() {
        List<Object[]> postsWithComments = postRepository.findAllPostsWithCommentsCount();
        List<PostDTO> postDTOs = postsWithComments.stream()
                .map(obj -> {
                    Post post = (Post) obj[0];
                    Long commentsCount = (Long) obj[1];

                    UserDTO userDTO = convertToUserDTO(post.getUser());

                    PostDTO dto = new PostDTO();
                    dto.setPostId(post.getPostId());
                    dto.setTitle(post.getTitle());
                    dto.setLikesCount(post.getLikesCount());
                    dto.setViewsCount(post.getViewsCount());
                    dto.setUser(userDTO); // UserDTO 설정
                    dto.setCommentsCount(commentsCount.intValue()); // 댓글 수 설정
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(postDTOs);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setNickname(user.getNickname());
        userDTO.setProfilePicture(user.getProfilePicture());
        return userDTO;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long postId) {
        logger.debug("Fetching post with ID: {}", postId);
        PostDTO postDTO = postService.getPostById(postId);
        if (postDTO != null) {
            logger.debug("Post found: {}", postDTO);
            logger.debug("Post imagePath: {}, Post content: {}", postDTO.getPostImage(), postDTO.getPostContent());
            return ResponseEntity.ok(postDTO);
        } else {
            logger.debug("Post not found for ID: {}", postId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        comments.forEach(comment -> logger.debug("Comment: {}", comment.getContent())); // 로그에 출력
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId) {
        boolean success = postService.deletePost(postId);
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("success", false));
        }
    }

    @PostMapping("/create-post")
    public ResponseEntity<Map<String, String>> createPost(@RequestParam String title,
                                                          @RequestParam String content,
                                                          @RequestParam(required = false) MultipartFile image,
                                                           HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUsername(token);
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                String imagePath = null;
                if (image != null && !image.isEmpty()) {
                    String storedFileName = fileStorageService.storeFile(image);
                    imagePath = "http://localhost:8080/uploads/" + storedFileName; // 절대 경로로 설정
                }

                boolean success = postService.createPost(title, content, imagePath, email);
                if (success) {
                    return ResponseEntity.ok(Collections.singletonMap("message", "게시글이 성공적으로 등록되었습니다."));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating post"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Unauthorized"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Unauthorized"));
        }
    }


    @PostMapping("/update-post")
    public ResponseEntity<Map<String, Object>> updatePost(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null || !validateToken(token)) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Unauthorized"));
        }

        String email = jwtTokenProvider.getUsername(token);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Unauthorized"));
        }

        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            logger.debug("Image file is not empty. Processing upload.");
            String storedFileName = fileStorageService.storeFile(imageFile);
            imagePath = "http://localhost:8080/uploads/" + storedFileName; // 절대 경로로 설정
            logger.debug("New image uploaded: {}", imagePath);
        } else {
            logger.debug("No new image uploaded for post update.");
        }

        boolean success = postService.updatePost(id, title, content, imagePath);
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Error updating post"));
        }
    }


    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        // Implement your token validation logic here
        return true; // Return true if valid, false otherwise
    }
}
