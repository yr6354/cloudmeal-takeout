package com.cloudmeal.takeout.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JWT 工具类单元测试。
 */
class JwtUtilTest {

    /** HS256 要求至少 32 字节，这里模拟生产环境的密钥长度 */
    private static final String SECRET = "cloudmeal-admin-secret-0123456789abcdefghijklmn";

    @Test
    @DisplayName("生成的令牌可以被同一密钥解析，负载保持一致")
    void createAndParse() {
        String token = JwtUtil.createJWT(SECRET, 60_000L, Map.of("empId", 42L));

        Claims claims = JwtUtil.parseJWT(SECRET, token);

        assertEquals(42L, ((Number) claims.get("empId")).longValue());
    }

    @Test
    @DisplayName("换一个密钥解析会因签名不匹配而失败")
    void rejectWrongSecret() {
        String token = JwtUtil.createJWT(SECRET, 60_000L, Map.of("empId", 1L));
        String otherSecret = "another-secret-0123456789abcdefghijklmnopqrstuv";

        assertThrows(JwtException.class, () -> JwtUtil.parseJWT(otherSecret, token));
    }

    @Test
    @DisplayName("过期令牌无法通过校验")
    void rejectExpiredToken() {
        String expired = JwtUtil.createJWT(SECRET, -1_000L, Map.of("empId", 1L));

        assertThrows(JwtException.class, () -> JwtUtil.parseJWT(SECRET, expired));
    }

    @Test
    @DisplayName("密钥过短时快速失败，避免使用不安全的签名强度")
    void rejectWeakSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> JwtUtil.createJWT("itcast", 60_000L, Map.of()));
    }
}
