package com.jxc.config;

import com.jxc.common.BusinessException;
import com.jxc.common.Result;
import com.jxc.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 * 覆盖业务异常、参数校验异常、参数类型不匹配、JSON 解析错误等常见场景
 *
 * @author jxc
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     * 业务代码抛出 BusinessException,直接返回其 code 和 msg
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: uri={}, code={}, msg={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常(@Valid 校验失败,请求体)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(";"));
        log.warn("参数校验失败: {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 绑定异常(@Valid 校验失败,表单参数)
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(";"));
        log.warn("参数绑定失败: {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 参数类型不匹配(如把 "abc" 传给 Long id)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("参数「%s」类型不匹配,期望: %s,实际: %s",
                e.getName(),
                e.getRequiredType() == null ? "未知" : e.getRequiredType().getSimpleName(),
                e.getValue());
        log.warn("参数类型不匹配: {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * JSON 请求体解析失败(格式错误、字段类型错误等)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("JSON 解析失败: {}", e.getMostSpecificCause().getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误,请检查 JSON 格式");
    }

    /**
     * 非法参数异常(如 Assert.notNull 抛出)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 其他未捕获异常(兜底,返回 500)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: uri={}", request.getRequestURI(), e);
        return Result.fail("系统异常,请联系管理员");
    }
}
