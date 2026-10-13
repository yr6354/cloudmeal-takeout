package com.cloudmeal.takeout.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构。
 *
 * <p>成功固定为 {@code code = 1}；失败返回具体业务错误码，便于前端做差异化处理。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 1：成功；其它：失败（见 {@link ErrorCode}） */
    private Integer code;
    /** 提示信息，成功时通常为空 */
    private String msg;
    /** 业务数据 */
    private T data;

    public static <T> Result<T> success() {
        return build(1, null, null);
    }

    public static <T> Result<T> success(T data) {
        return build(1, null, data);
    }

    public static <T> Result<T> error(String msg) {
        return build(0, msg, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return build(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> error(ErrorCode errorCode, String msg) {
        return build(errorCode.getCode(), msg, null);
    }

    /** 仅供服务端判断，不进入响应体（对外契约固定为 code / msg / data） */
    @JsonIgnore
    public boolean isSuccess() {
        return code != null && code == 1;
    }

    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }
}
