package org.shelterconnect.api.platform.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shelterconnect.api.platform.web.dto.CommonResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("Business operation failed: {}", errorCode.getCode(), exception);
        }
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(CommonResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorCode errorCode = switch (status.value()) {
            case 400 -> exception instanceof MethodArgumentNotValidException
                    || exception instanceof HandlerMethodValidationException
                    ? ErrorCode.INVALID_INPUT_VALUE : ErrorCode.BAD_REQUEST;
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            default -> null;
        };
        CommonResponse<Void> response;
        if (status.is5xxServerError()) {
            log.error("Request processing failed", exception);
            response = CommonResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        } else if (errorCode != null) {
            response = CommonResponse.failure(errorCode.getCode(), errorCode.getMessage());
        } else {
            response = CommonResponse.failure("HTTP_" + status.value(), "요청을 처리할 수 없습니다.");
        }
        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception) throws Exception {
        // 인증 및 접근 거부 예외는 Spring Security가 처리하도록 다시 던집니다.
        if (exception instanceof AccessDeniedException || exception instanceof AuthenticationException) {
            throw exception;
        }
        log.error("Unexpected server error", exception);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(CommonResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }
}
