package com.example.learningplatform.util;

import com.example.learningplatform.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 固定安全密钥（长度 >= 32 字节）
    private static final String SECRET = "aigc-learning-platform-secret-key-20260115!";

    // token 有效期：1 天
    private static final long EXPIRE = 1000 * 60 * 60 * 24;

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT token
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole()) // 存储角色
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 token 获取用户名
     */
    public String getUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 token 获取角色
     */
    public String getRole(String token) {
        try {
            return getClaims(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 token
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * 判断 token 是否过期
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * 解析 token
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}