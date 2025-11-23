package org.itss.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct; // Nếu dùng Spring Boot cũ (<3.0) thì đổi thành javax.annotation.PostConstruct

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component // 1. Đánh dấu để Spring quản lý (Bean)
public class JwtUtil {

    @Value("${jwt.secret}") // 2. Lấy key từ file properties
    private String secretString;

    @Value("${jwt.expiration:86400000}") // Lấy thời gian hết hạn (mặc định 86400000 nếu không tìm thấy)
    private long expirationTime;

    private SecretKey key;

    // 3. Hàm này chạy tự động sau khi Spring inject xong giá trị vào secretString
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // Sử dụng biến expirationTime
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            // Token không hợp lệ hoặc hết hạn
            return null;
        }
    }
}