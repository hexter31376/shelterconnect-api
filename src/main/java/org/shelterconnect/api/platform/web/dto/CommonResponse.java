package org.shelterconnect.api.platform.web.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record CommonResponse<T>(T data, ErrorDetail error) {

    public CommonResponse {
        if (data != null && error != null) {
            throw new IllegalArgumentException("data and error cannot both be present");
        }
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(data, null);
    }

    public static CommonResponse<Void> success() {
        return new CommonResponse<>(null, null);
    }

    public static CommonResponse<Void> failure(String code, String message) {
        return new CommonResponse<>(null, new ErrorDetail(code, message));
    }

    public record ErrorDetail(String code, String message) {
    }
}
