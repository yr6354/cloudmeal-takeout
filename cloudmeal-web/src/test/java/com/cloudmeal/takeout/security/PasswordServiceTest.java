package com.cloudmeal.takeout.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 密码服务单元测试：验证 BCrypt 与历史 MD5 的兼容性。
 */
class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder());

    @Test
    @DisplayName("BCrypt 加密后可以校验通过，且同一明文两次加密结果不同（随机盐）")
    void bcryptRoundTrip() {
        String first = passwordService.encode("123456");
        String second = passwordService.encode("123456");

        assertNotEquals(first, second, "BCrypt 应使用随机盐");
        assertTrue(passwordService.matches("123456", first));
        assertTrue(passwordService.matches("123456", second));
        assertFalse(passwordService.matches("123457", first));
    }

    @Test
    @DisplayName("历史 MD5 密码仍可登录，并被识别为待升级")
    void legacyMd5StillWorks() {
        String md5 = DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8));

        assertTrue(passwordService.isLegacy(md5));
        assertTrue(passwordService.matches("123456", md5));
        assertFalse(passwordService.matches("wrong", md5));
    }

    @Test
    @DisplayName("BCrypt 密文不会被误判为待升级")
    void bcryptIsNotLegacy() {
        assertFalse(passwordService.isLegacy(passwordService.encode("123456")));
    }

    @Test
    @DisplayName("空值不会触发异常，统一按不匹配处理")
    void nullSafety() {
        assertFalse(passwordService.matches(null, "anything"));
        assertFalse(passwordService.matches("anything", null));
    }
}
