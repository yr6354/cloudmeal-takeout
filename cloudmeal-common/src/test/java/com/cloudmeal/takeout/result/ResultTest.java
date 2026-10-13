package com.cloudmeal.takeout.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一响应结构单元测试：确保前端约定的 code 语义不被破坏。
 */
class ResultTest {

    @Test
    @DisplayName("成功响应固定返回 code = 1")
    void success() {
        Result<String> result = Result.success("data");

        assertEquals(1, result.getCode());
        assertEquals("data", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("业务失败返回错误码与可读提示")
    void error() {
        Result<Void> result = Result.error(ErrorCode.ORDER_NOT_FOUND);

        assertEquals(ErrorCode.ORDER_NOT_FOUND.getCode(), result.getCode());
        assertEquals("订单不存在", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("允许用错误码语义覆盖提示文案")
    void errorWithCustomMessage() {
        Result<Void> result = Result.error(ErrorCode.PARAM_ERROR, "手机号格式不正确");

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("手机号格式不正确", result.getMsg());
    }
}
