package com.example.demo.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT (Json Web Token) 생성 및 검증 유틸리티 클래스
 *
 * ── 📚 초보자를 위한 JWT 개념 설명 ──
 *
 * JWT는 "인증 정보를 담은 토큰"입니다.
 * 예를 들어, 호텔에서 체크인하면 "방 열쇠(토큰)"를 받는 것과 같습니다.
 *
 * 1. 로그인 성공 → 서버가 JWT 토큰 발급 (방 열쇠 받기)
 * 2. 이후 요청 시 → 헤더에 토큰을 넣어서 보냄 (방 열쇠 보여주기)
 * 3. 서버가 토큰 검증 → 유효하면 요청 처리 (열쇠 확인 후 입장 허용)
 *
 * JWT 구조: Header.Payload.Signature (점(.)으로 구분된 3부분)
 * - Header: 알고리즘 정보
 * - Payload: 회원 ID, 이름 등 실제 데이터
 * - Signature: 위변조 방지를 위한 서명
 */
@Service
public class JwtUtil {
    private final SecretKey secretKey; // JWT 서명에 사용되는 비밀 키 // 생성한 비밀 키의 타입이 SecretKey타입

    private static final long expirationTime = 1000 * 60 * 60; // 토큰 만료 시간: 1시간

    public JwtUtil(@Value("${jwt.base64Secret}") String base64Secret){ //@Value을 통해 application.yml에서 값 주입
        byte[] decodedkey = Base64.getDecoder().decode(base64Secret);//Base64로 인코딩된 문자열을 디코딩하여 바이트 배열로 반환
        this.secretKey=Keys.hmacShaKeyFor(decodedkey); //
    }

    public String generateJwt(String userId, String username){
        return Jwts.builder()
                .setSubject(userId)
                .claim("name", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateJwt(String jwt){
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt);
        return true;
    }

    public Claims getClaimsFromJwt(String jwt){
        String NoneBearerJwt = jwt;
        if(jwt.startsWith("Bearer ")){
            NoneBearerJwt=jwt.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(NoneBearerJwt)
                .getBody();
    }

}
