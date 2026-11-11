package org.shelterconnect.api.platform.web.dto;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class PaginationResponseTests {

    @Test
    void wrapsPageMetadataAndMappedContent() {
        var page = new PageImpl<>(List.of(1, 2), PageRequest.of(0, 2), 5)
                .map(value -> "item-" + value);
        var response = CommonResponse.page(page);

        assertNull(response.error());
        assertEquals(List.of("item-1", "item-2"), response.data().content());
        assertEquals(0, response.data().page());
        assertEquals(2, response.data().size());
        assertEquals(5, response.data().totalElements());
        assertEquals(3, response.data().totalPages());
        assertTrue(response.data().hasNext());
    }

    @Test
    void handlesLastAndEmptyPages() {
        var last = CommonResponse.page(new PageImpl<>(List.of(5), PageRequest.of(2, 2), 5));
        assertFalse(last.data().hasNext());
        assertEquals(2, last.data().page());

        var empty = CommonResponse.page(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
        assertTrue(empty.data().content().isEmpty());
        assertEquals(0, empty.data().totalPages());
        assertFalse(empty.data().hasNext());
    }

    @Test
    void cursorIndicatesContinuationOnlyWhenTokenExists() {
        var next = CommonResponse.cursor(List.of("item"), "opaque-token");
        assertTrue(next.data().hasNext());
        assertEquals("opaque-token", next.data().nextCursor());
        assertNull(next.error());

        var last = CommonResponse.cursor(List.of("item"), null);
        assertFalse(last.data().hasNext());
        assertNull(last.data().nextCursor());
        assertFalse(CommonResponse.cursor(List.of(), null).data().hasNext());
    }

    @Test
    void rejectsInvalidCursorStates() {
        assertThrows(IllegalArgumentException.class, () -> CursorResponse.of(List.of(1), " "));
        assertThrows(IllegalArgumentException.class, () -> CursorResponse.of(List.of(), "next"));
        assertThrows(IllegalArgumentException.class, () -> new CursorResponse<>(List.of(1), null, true));
        assertThrows(IllegalArgumentException.class, () -> new CursorResponse<>(List.of(1), "next", false));
    }

    @Test
    void copiesContentToPreventMutationAfterConstruction() {
        var source = new ArrayList<>(List.of("item"));
        var cursor = CursorResponse.of(source, null);
        var page = new PageResponse<>(source, 0, 10, 1, 1, false);
        source.clear();
        assertEquals(List.of("item"), cursor.content());
        assertEquals(List.of("item"), page.content());
        assertThrows(UnsupportedOperationException.class, () -> cursor.content().clear());
        assertThrows(UnsupportedOperationException.class, () -> page.content().clear());
    }

    @Test
    void serializesPaginationInsideCommonResponseData() {
        var mapper = JsonMapper.builder().build();
        var page = mapper.readTree(mapper.writeValueAsString(CommonResponse.page(
                new PageImpl<>(List.of("item"), PageRequest.of(0, 10), 1))));
        assertEquals("item", page.path("data").path("content").get(0).stringValue());
        assertEquals(1, page.path("data").path("totalElements").asInt());
        assertFalse(page.path("data").has("pageable"));
        assertTrue(page.path("error").isNull());

        var cursor = mapper.readTree(mapper.writeValueAsString(CommonResponse.cursor(List.of("item"), null)));
        assertTrue(cursor.path("data").path("nextCursor").isNull());
        assertFalse(cursor.path("data").path("hasNext").asBoolean());
    }
}
