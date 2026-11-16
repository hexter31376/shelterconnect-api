package org.shelterconnect.api.platform.web.exceptions;

import java.util.Objects;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(Throwable cause) {
        super("외부 서비스 요청에 실패했습니다.", Objects.requireNonNull(cause, "외부 서비스 호출 실패의 원인 예외는 null일 수 없습니다."));
    }
}
