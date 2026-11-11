package org.shelterconnect.api.platform.web.dto;

import java.util.List;

/** 조회 계층이 다음 커서 토큰을 제공하며, null이면 다음 페이지가 없습니다. */
public record CursorResponse<T>(List<T> content, String nextCursor, boolean hasNext) {

    public CursorResponse {
        content = List.copyOf(content);
        if (nextCursor != null && nextCursor.isBlank()) {
            throw new IllegalArgumentException("nextCursor cannot be blank");
        }
        if (hasNext != (nextCursor != null)) {
            throw new IllegalArgumentException("nextCursor must be present exactly when hasNext is true");
        }
        if (hasNext && content.isEmpty()) {
            throw new IllegalArgumentException("A page with a next cursor cannot be empty");
        }
    }

    public static <T> CursorResponse<T> of(List<T> content, String nextCursor) {
        return new CursorResponse<>(content, nextCursor, nextCursor != null);
    }
}
