package com.cloudmeal.takeout.interceptor;

import com.cloudmeal.takeout.context.UserContext;
import com.cloudmeal.takeout.result.ErrorCode;
import com.cloudmeal.takeout.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 鉴权拦截器。
 *
 * <p>管理端与用户端逻辑完全一致，只是密钥、请求头、身份声明不同，
 * 因此由配置类以不同参数实例化两个对象，而不是维护两份重复代码。</p>
 */
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final String secretKey;
    private final String tokenHeaderName;
    private final String claimName;

    public JwtAuthInterceptor(String secretKey, String tokenHeaderName, String claimName) {
        this.secretKey = secretKey;
        this.tokenHeaderName = tokenHeaderName;
        this.claimName = claimName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(tokenHeaderName);
        if (token == null || token.isBlank()) {
            writeUnauthorized(response);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            UserContext.setCurrentId(Long.valueOf(claims.get(claimName).toString()));
            return true;
        } catch (Exception ex) {
            log.warn("令牌校验失败：{}", ex.getMessage());
            writeUnauthorized(response);
            return false;
        }
    }

    /**
     * 请求结束后一定清理线程上下文，避免线程池复用造成身份串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\":%d,\"msg\":\"%s\",\"data\":null}",
                ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMessage()));
    }
}
