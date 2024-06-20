package com.example.communityservice.util;

import java.util.Base64;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

public class GenerateKey {
    public static void main(String[] args) {
        // HS512 알고리즘에 사용될 비밀 키 생성
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        // 비밀 키를 Base64로 인코딩
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        // 인코딩된 키 출력
        System.out.println(base64Key);
    }
}
