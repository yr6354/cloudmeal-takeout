package com.cloudmeal.takeout.handler;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import com.cloudmeal.takeout.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 全局异常处理器单元测试。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("业务异常转换为对应错误码，且保留自定义文案")
    void businessException() {
        Result<Void> result = handler.handleBusinessException(
                new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单当前状态为【已完成】，不允许变更为【已取消】"));

        assertEquals(ErrorCode.ORDER_STATUS_ERROR.getCode(), result.getCode());
        assertEquals("订单当前状态为【已完成】，不允许变更为【已取消】", result.getMsg());
    }

    @Test
    @DisplayName("唯一键冲突被翻译为「xxx 已存在」")
    void duplicateKey() {
        SQLIntegrityConstraintViolationException cause = new SQLIntegrityConstraintViolationException(
                "Duplicate entry 'admin' for key 'employee.idx_username'");

        Result<Void> result = handler.handleDuplicateKey(new DuplicateKeyException("insert failed", cause));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_EXIST.getCode(), result.getCode());
        assertEquals("admin 已存在", result.getMsg());
    }

    @Test
    @DisplayName("必填列为空时给出可定位的字段提示，而不是笼统的未知错误")
    void dataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("insert failed",
                new SQLIntegrityConstraintViolationException("Column 'sex' cannot be null"));

        Result<Void> result = handler.handleDataIntegrityViolation(ex);

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("字段 sex 不能为空", result.getMsg());
    }

    @Test
    @DisplayName("未知异常兜底为通用错误，不向前端泄露内部细节")
    void unexpectedException() {
        Result<Void> result = handler.handleUnexpectedException(new IllegalStateException("数据库连接串 ..."));

        assertEquals(ErrorCode.UNKNOWN_ERROR.getCode(), result.getCode());
        assertEquals("未知错误", result.getMsg());
    }
}
