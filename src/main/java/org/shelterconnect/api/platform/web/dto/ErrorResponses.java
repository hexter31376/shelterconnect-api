package org.shelterconnect.api.platform.web.dto;

import java.util.Objects;

import org.shelterconnect.api.platform.web.ErrorCode;
import org.springframework.http.ResponseEntity;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    public static ResponseEntity<Object> from(ErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "에러 코드는 null일 수 없습니다.");
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(body(errorCode));
    }
    public static CommonResponse<Void> body(ErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "에러 코드는 null일 수 없습니다.");
        return CommonResponse.failure(errorCode.getCode(), errorCode.getMessage());
    }
}
