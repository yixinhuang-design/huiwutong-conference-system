package com.conference.common.exception;

import com.conference.common.result.Result;
import com.conference.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: {}, 错误码: {}, 消息: {}", 
                request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常 - @Valid 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * HTTP方法不支持异常（如 GET 调用仅支持 POST 的接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                  HttpServletRequest request) {
        log.warn("请求方法不支持 - URI: {}, method: {}, 支持方法: {}",
                request.getRequestURI(), e.getMethod(), e.getSupportedHttpMethods());
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "请求方法不支持，请使用正确的HTTP方法");
    }
    
    /**
     * 404 - 路径不存在（NoHandlerFoundException）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.debug("路径不存在: {} {}", request.getMethod(), request.getRequestURI());
        return Result.error(HttpStatus.NOT_FOUND.value(), "接口不存在: " + request.getRequestURI());
    }

    /**
     * 404 - 静态资源不存在（NoResourceFoundException）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        log.debug("资源不存在: {} {}", request.getMethod(), request.getRequestURI());
        return Result.error(HttpStatus.NOT_FOUND.value(), "资源不存在: " + request.getRequestURI());
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - URI: {}", request.getRequestURI(), e);
        return Result.error(ResultCode.ERROR.getCode(), "系统繁忙，请稍后重试");
    }
    
    /**
     * 未知异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URI: {}", request.getRequestURI(), e);
        return Result.error(ResultCode.ERROR.getCode(), "系统异常，请联系管理员");
    }
}
