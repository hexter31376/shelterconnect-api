package org.shelterconnect.api.platform.web;

import org.shelterconnect.api.platform.web.dto.ErrorResponses;
import org.shelterconnect.api.platform.web.exceptions.ExternalServiceException;
import org.shelterconnect.api.platform.web.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 도메인에 종속되지 않는 명시적 애플리케이션 예외를 처리합니다. */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RestControllerAdvice
public class ApplicationExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionAdvice.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> resourceNotFound() {
        return ErrorResponses.from(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> externalServiceFailed(ExternalServiceException exception) {
        log.error("External service request failed", exception);
        return ErrorResponses.from(ErrorCode.EXTERNAL_SERVER_ERROR);
    }
}
