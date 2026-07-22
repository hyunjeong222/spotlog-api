package com.spring.spotlog_api.global.handler;

import com.spring.spotlog_api.global.common.ErrorResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 필드 우선순위 (주요 필드만 관리)
    private static final List<String> fieldPriority = List.of(
            "name", "email", "password", "businessNumber"
    );

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // @Valid 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        // fieldPriority에 있는 필드 우선순위대로 처리
        for (String field : fieldPriority) {
            List<FieldError> errorsForField = fieldErrors.stream()
                    .filter(err -> err.getField().equals(field))
                    .collect(Collectors.toList());

            if (!errorsForField.isEmpty()) {
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

        // fieldPriority에 없는 필드 → 첫 번째 오류 메시지 그대로 반환
        String message = fieldErrors.isEmpty()
                ? ErrorCode.INVALID_INPUT_VALUE.getMessage()
                : fieldErrors.get(0).getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    // DB 제약 조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException e) {
        String message = e.getRootCause() != null
                ? e.getRootCause().getMessage()
                : "";

        if (message.contains("uk_member_email")) {
            return toResponse(ErrorCode.DUPLICATE_EMAIL);
        }

        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    // enum 값 오류
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        Class<?> requiredType = e.getRequiredType();

        if (requiredType != null && requiredType.isEnum()) {
            String allowedValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            String message = "올바르지 않은 값입니다. 가능한 값: " + allowedValues;
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
        }

        return toResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 권한 부족 (@PreAuthorize 실패)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e) {
        return toResponse(ErrorCode.FORBIDDEN);
    }

    // 예상하지 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}