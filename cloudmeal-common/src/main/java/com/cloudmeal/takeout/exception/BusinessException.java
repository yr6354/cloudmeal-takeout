package com.cloudmeal.takeout.exception;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>全工程唯一的业务异常类型：携带 {@link ErrorCode}，由全局异常处理器统一转换为响应体，
 * 避免为每种业务场景各建一个异常类。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 使用错误码的语义，但覆盖提示文案（例如拼接具体字段值）。 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
