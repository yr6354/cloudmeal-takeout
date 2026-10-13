package com.cloudmeal.takeout.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类（基于 jjwt 0.12.x）。
 */
public final class JwtUtil {

    /** HS256 要求密钥长度不少于 256 bit（32 字节） */
    private static final int MIN_SECRET_BYTES = 32;

    private JwtUtil() {
    }

    /**
     * 生成 JWT。
     *
     * @param secretKey 签名密钥，长度必须 ≥ 32 字节
     * @param ttlMillis 有效期（毫秒）
     * @param claims    自定义负载
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SecretKey key = toSecretKey(secretKey);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 校验并解析 JWT，签名不合法或已过期都会抛出 {@link io.jsonwebtoken.JwtException}。
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .verifyWith(toSecretKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey toSecretKey(String secretKey) {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "JWT 密钥长度不足：HS256 要求至少 " + MIN_SECRET_BYTES + " 字节，请通过环境变量配置更长的密钥");
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
