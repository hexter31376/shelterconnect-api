package org.shelterconnect.api.platform.web.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonResponseTests {

    @Test
    void distinguishesEmptySuccessFromMissingPayload() {
        assertThrows(NullPointerException.class, () -> CommonResponse.success(null));
        assertNull(CommonResponse.success().data());
        assertNull(CommonResponse.success().error());
        assertEquals("ok", CommonResponse.success("ok").data());
    }

    @Test
    void rejectsMissingOrBlankErrorDetails() {
        assertThrows(NullPointerException.class, () -> CommonResponse.failure(null, "message"));
        assertThrows(NullPointerException.class, () -> CommonResponse.failure("G001", null));
        assertThrows(IllegalArgumentException.class, () -> CommonResponse.failure(" ", "message"));
        assertThrows(IllegalArgumentException.class, () -> CommonResponse.failure("G001", " "));
    }

    @Test
    void rejectsSimultaneousSuccessAndFailure() {
        assertThrows(IllegalArgumentException.class, () -> new CommonResponse<>("ok",
                new CommonResponse.ErrorDetail("G001", "message")));
    }

    @Test
    void preservesFailureShape() {
        var response = CommonResponse.failure("G001", "message");
        assertNull(response.data());
        assertEquals(new CommonResponse.ErrorDetail("G001", "message"), response.error());
    }
}
