package org.shelterconnect.api.platform.web;

import org.junit.jupiter.api.Test;
import org.shelterconnect.api.platform.web.dto.ErrorResponses;
import org.shelterconnect.api.platform.web.exceptions.ExternalServiceException;

import static org.junit.jupiter.api.Assertions.*;

class NullContractTests {

    @Test
    void externalServiceFailureRequiresAndPreservesCause() {
        assertThrows(NullPointerException.class, () -> new ExternalServiceException(null));
        var cause = new IllegalStateException("외부 서비스 호출 실패");
        assertSame(cause, new ExternalServiceException(cause).getCause());
    }

    @Test
    void errorResponseRequiresExplicitCode() {
        assertThrows(NullPointerException.class, () -> ErrorResponses.from(null));
        assertThrows(NullPointerException.class, () -> ErrorResponses.body(null));
    }
}
