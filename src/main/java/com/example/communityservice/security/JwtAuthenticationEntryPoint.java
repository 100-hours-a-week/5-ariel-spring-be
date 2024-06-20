package com.example.communityservice.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component // 이 클래스가 Spring의 구성 요소(빈)임을 나타냅니다.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 이 메서드는 인증이 실패했을 때 호출됩니다. 즉, 보호된 리소스에 접근하려고 할 때 인증되지 않은 사용자가 접근을 시도할 때 호출됩니다.
        // 응답 상태를 401(Unauthorized)로 설정하고 에러 메시지를 보냅니다.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
