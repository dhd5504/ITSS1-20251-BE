package org.itss.exception;

import org.itss.dto.response.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ❌ Lỗi validate @Valid trong @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Validation error");

        return Result.error(message);
    }

    // ❌ Lỗi validate dạng @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        return Result.error(e.getMessage());
    }

    // ❌ Thiếu tham số request
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingRequestParam(MissingServletRequestParameterException e) {
        return Result.error("Missing parameter: " + e.getParameterName());
    }

    // ❌ JSON parse lỗi
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleInvalidJson(HttpMessageNotReadableException e) {
        return Result.error("Invalid request body");
    }

    // ❌ Lỗi tùy chỉnh từ hệ thống của bạn
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleCustomException(RuntimeException e) {
        log.error("Runtime error: ", e);
        return Result.error(e.getMessage());
    }

    // ❌ Lỗi không xác định
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return Result.error("Internal server error");
    }
}
