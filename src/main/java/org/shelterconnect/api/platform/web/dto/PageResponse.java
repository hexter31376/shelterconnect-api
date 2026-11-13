package org.shelterconnect.api.platform.web.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** 페이지 번호는 0부터 시작하며, 목록에는 엔티티 대신 응답 DTO를 담습니다. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public PageResponse {
        content = List.copyOf(content);
        if (page < 0 || size < 0 || totalElements < 0 || totalPages < 0) {
            throw new IllegalArgumentException("Page metadata cannot be negative");
        }
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.hasNext());
    }
}
