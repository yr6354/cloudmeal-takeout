package com.cloudmeal.takeout.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 密码校验与加密。
 *
 * <p>历史数据使用 MD5（教程实现），这里升级为 BCrypt：
 * 新密码一律 BCrypt 存储；老账号首次登录校验通过后自动重新加密回写，实现平滑迁移。</p>
 */
@Component
@RequiredArgsConstructor
public class PasswordService {

    /** MD5 十六进制摘要恒为 32 位 */
    private static final Pattern LEGACY_MD5 = Pattern.compile("^[a-fA-F0-9]{32}$");

    private final PasswordEncoder passwordEncoder;

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * @return 明文与存储值是否匹配
     */
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isLegacy(storedPassword)) {
            return storedPassword.equalsIgnoreCase(md5(rawPassword));
        }
        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    /** 是否为需要升级的历史加密值 */
    public boolean isLegacy(String storedPassword) {
        return LEGACY_MD5.matcher(storedPassword).matches();
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
