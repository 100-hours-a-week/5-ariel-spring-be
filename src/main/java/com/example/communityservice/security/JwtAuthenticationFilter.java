package com.example.communityservice.security;

import com.example.communityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component // 이 클래스가 Spring의 구성 요소(빈)임을 나타냅니다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider; // JWT 토큰을 제공하는 클래스

    @Autowired
    private UserDetailsService userDetailsService; // 사용자 세부 정보를 로드하는 서비스

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청에서 JWT 토큰을 가져옴
        String token = tokenProvider.getJwtFromRequest(request);

        // 토큰이 유효한지 확인
        if (token != null && tokenProvider.validateToken(token)) {
            // 토큰에서 사용자 이름을 가져옴
            String username = tokenProvider.getUsernameFromJWT(token);

            // 사용자 이름으로 사용자 세부 정보를 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails != null) {
                // 사용자 세부 정보를 기반으로 인증 객체 생성
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // SecurityContextHolder에 인증 객체를 설정하여 사용자 인증 상태를 유지
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 필터 체인을 통해 요청을 계속 처리
        filterChain.doFilter(request, response);
    }
}
