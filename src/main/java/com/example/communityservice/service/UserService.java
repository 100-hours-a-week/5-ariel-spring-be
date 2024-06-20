package com.example.communityservice.service;

import com.example.communityservice.model.User;
import com.example.communityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service // 이 클래스가 스프링 서비스 빈임을 나타냅니다.
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // UserRepository 빈을 주입받습니다.

    @Autowired
    private PasswordEncoder passwordEncoder; // PasswordEncoder 빈을 주입받습니다.

    // 사용자 정보를 저장하는 메서드
    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호를 암호화합니다.
        userRepository.save(user); // 사용자 정보를 데이터베이스에 저장합니다.
    }

    // 이메일로 사용자를 찾는 메서드
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email); // 이메일을 이용해 사용자를 찾습니다.
    }

    // 사용자의 비밀번호를 확인하는 메서드
    public boolean checkPassword(User user, String rawPassword) {
        System.out.println(rawPassword + user.getPassword());
        return passwordEncoder.matches(rawPassword, user.getPassword()); // 입력된 비밀번호와 저장된 암호화된 비밀번호를 비교합니다.
    }

    // 모든 사용자 정보를 가져오는 메서드
    public List<User> getAllUsers() {
        return userRepository.findAll(); // 모든 사용자 정보를 데이터베이스에서 가져옵니다.
    }

    // UserDetailsService 인터페이스의 메서드를 구현하여 사용자 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)); // 이메일로 사용자를 찾지 못하면 예외를 던집니다.
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>()); // UserDetails 객체를 반환합니다.
    }

    // 사용자를 등록하는 메서드
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호를 암호화합니다.
        return userRepository.save(user); // 사용자 정보를 데이터베이스에 저장합니다.
    }
}
