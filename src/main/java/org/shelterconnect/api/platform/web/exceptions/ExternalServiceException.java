package org.shelterconnect.api.platform.web.exceptions;

import java.util.Objects;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(Throwable cause) {
        super("External service request failed", Objects.requireNonNull(cause, "cause"));
    }
}
