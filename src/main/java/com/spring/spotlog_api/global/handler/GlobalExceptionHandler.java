package com.spring.spotlog_api.global.handler;

import com.spring.spotlog_api.global.common.ErrorResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 필드 우선순위 (앞에 있을수록 먼저 처리)
    private static final List<String> fieldPriority = List.of(
            "name", "email", "password"
    );

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // Validation 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        // 필드 우선순위대로 순회
        for (String field : fieldPriority) {
            List<FieldError> errorsForField = fieldErrors.stream()
                    .filter(err -> err.getField().equals(field))
                    .collect(Collectors.toList());

            if (!errorsForField.isEmpty()) {
                // 어노테이션 우선순위: NotBlank → Size → Pattern
                errorsForField.sort(Comparator.comparingInt(err -> {
                    String code = err.getCode();
                    if (code != null && code.contains("NotBlank")) return 1;
                    if (code != null && code.contains("Size")) return 2;
                    if (code != null && code.contains("Pattern")) return 3;
                    return 99;
                }));

                String message = errorsForField.get(0).getDefaultMessage();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}