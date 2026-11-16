package org.shelterconnect.api.platform.web;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.shelterconnect.api.platform.web.dto.ErrorResponses;
import org.shelterconnect.api.platform.web.dto.CommonResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        CommonResponse<Void> response;
        if (status.is5xxServerError()) {
            log.error("Request processing failed", exception);
            response = ErrorResponses.body(ErrorCode.INTERNAL_SERVER_ERROR);
        } else {
            response = switch (status.value()) {
                case 400 -> ErrorResponses.body(
                        exception instanceof MethodArgumentNotValidException
                                || exception instanceof HandlerMethodValidationException
                                ? ErrorCode.INVALID_INPUT_VALUE : ErrorCode.BAD_REQUEST);
                case 404 -> ErrorResponses.body(ErrorCode.NOT_FOUND);
                case 405 -> ErrorResponses.body(ErrorCode.METHOD_NOT_ALLOWED);
                default -> CommonResponse.failure("HTTP_" + status.value(), "요청을 처리할 수 없습니다.");
            };
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
        return ErrorResponses.from(errorCode);
    }
}
