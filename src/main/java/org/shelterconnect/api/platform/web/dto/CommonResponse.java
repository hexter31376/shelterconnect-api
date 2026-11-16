package org.shelterconnect.api.platform.web.dto;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record CommonResponse<T>(@Nullable T data, @Nullable ErrorDetail error) {

    public CommonResponse {
        if (data != null && error != null) {
            throw new IllegalArgumentException("응답 데이터와 에러 정보는 동시에 존재할 수 없습니다.");
        }
    }

    public static <T> CommonResponse<T> success(T data) {
        Objects.requireNonNull(data, "응답 데이터는 null일 수 없습니다. 데이터 없는 성공 응답은 success()를 사용하세요.");
        return new CommonResponse<>(data, null);
    }

    public static CommonResponse<Void> success() {
        return new CommonResponse<>(null, null);
    }

    public static CommonResponse<Void> failure(String code, String message) {
        return new CommonResponse<>(null, new ErrorDetail(code, message));
    }

    public record ErrorDetail(String code, String message) {
        public ErrorDetail {
            Objects.requireNonNull(code, "에러 코드는 null일 수 없습니다.");
            Objects.requireNonNull(message, "에러 메시지는 null일 수 없습니다.");
            if (code.isBlank()) {
                throw new IllegalArgumentException("에러 코드는 빈 문자열이거나 공백만 포함할 수 없습니다.");
            }
            if (message.isBlank()) {
                throw new IllegalArgumentException("에러 메시지는 빈 문자열이거나 공백만 포함할 수 없습니다.");
            }
        }
    }
}
