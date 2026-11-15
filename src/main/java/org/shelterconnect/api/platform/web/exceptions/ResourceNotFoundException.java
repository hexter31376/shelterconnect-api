package org.shelterconnect.api.platform.web.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Requested resource was not found");
    }
}
