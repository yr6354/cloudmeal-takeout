package com.cloudmeal.takeout.handler;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import com.cloudmeal.takeout.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>把不同类型的异常统一翻译为 {@link Result}，保证前端拿到的响应结构一致：</p>
 * <ul>
 *   <li>{@link BusinessException}：可预期的业务失败，按 warn 记录，不打印堆栈</li>
 *   <li>参数校验相关异常：汇总字段错误后返回 {@link ErrorCode#PARAM_ERROR}</li>
 *   <li>数据库唯一键冲突：翻译为「xxx 已存在」</li>
 *   <li>兜底 {@link Exception}：按 error 记录完整堆栈，避免把内部细节暴露给前端</li>
 * </ul>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY = Pattern.compile("Duplicate entry '([^']+)'");
    private static final Pattern COLUMN_NOT_NULL = Pattern.compile("Column '([^']+)' cannot be null");

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常 code={} msg={}", ex.getErrorCode().getCode(), ex.getMessage());
        return Result.error(ex.getErrorCode(), ex.getMessage());
    }

    /** @Valid 校验失败（请求体） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.error(ErrorCode.PARAM_ERROR, message);
    }

    /** @Valid 校验失败（表单 / 查询参数绑定） */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.error(ErrorCode.PARAM_ERROR, message);
    }

    /** 方法参数上的约束校验（@Validated + @NotBlank 等） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.error(ErrorCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("缺少必要参数：{}", ex.getParameterName());
        return Result.error(ErrorCode.PARAM_ERROR, "缺少必要参数：" + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配：{}", ex.getName());
        return Result.error(ErrorCode.PARAM_ERROR, "参数类型不匹配：" + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败：{}", ex.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR, "请求体格式不正确");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持：{}", ex.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR, "不支持的请求方法：" + ex.getMethod());
    }

    /** 唯一键冲突：例如账号重复、订单号重复 */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException ex) {
        String raw = ex.getMostSpecificCause().getMessage();
        log.warn("唯一键冲突：{}", raw);
        Matcher matcher = DUPLICATE_ENTRY.matcher(raw == null ? "" : raw);
        if (matcher.find()) {
            return Result.error(ErrorCode.ACCOUNT_ALREADY_EXIST, matcher.group(1) + " 已存在");
        }
        return Result.error(ErrorCode.UNKNOWN_ERROR);
    }

    /** 其它数据完整性约束：例如必填列传了空值、字段超长 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause().getMessage();
        log.warn("数据完整性校验失败：{}", raw);
        Matcher matcher = COLUMN_NOT_NULL.matcher(raw == null ? "" : raw);
        if (matcher.find()) {
            return Result.error(ErrorCode.PARAM_ERROR, "字段 " + matcher.group(1) + " 不能为空");
        }
        return Result.error(ErrorCode.PARAM_ERROR, "提交的数据不符合字段约束");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpectedException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(ErrorCode.UNKNOWN_ERROR);
    }
}
