package org.shelterconnect.api.platform.web.dto;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record CommonResponse<T>(@Nullable T data, @Nullable ErrorDetail error) {

    public CommonResponse {
        if (data != null && error != null) {
            throw new IllegalArgumentException("data and error cannot both be present");
        }
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(Objects.requireNonNull(data, "data; use success() for an empty response"), null);
    }

    public static CommonResponse<Void> success() {
        return new CommonResponse<>(null, null);
    }

    public static CommonResponse<Void> failure(String code, String message) {
        return new CommonResponse<>(null, new ErrorDetail(code, message));
    }

    public record ErrorDetail(String code, String message) {
        public ErrorDetail {
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(message, "message");
            if (code.isBlank() || message.isBlank()) {
                throw new IllegalArgumentException("error code and message must not be blank");
            }
        }
    }
거}
