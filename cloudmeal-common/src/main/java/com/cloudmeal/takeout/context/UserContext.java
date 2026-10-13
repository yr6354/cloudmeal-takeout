package com.cloudmeal.takeout.context;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;

/**
 * 当前登录用户上下文。
 *
 * <p>由鉴权拦截器在请求进入时写入、请求结束时通过 {@link #clear()} 清理，
 * 避免线程复用（线程池）导致的用户身份串号。</p>
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_USER_ID.set(id);
    }

    /** 允许为 null 的场景（如未登录接口）直接返回 null。 */
    public static Long getCurrentId() {
        return CURRENT_USER_ID.get();
    }

    /** 需要登录态的业务统一调用此方法，缺失身份时抛出业务异常而非 NPE。 */
    public static Long requireCurrentId() {
        Long id = CURRENT_USER_ID.get();
        if (id == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        return id;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
