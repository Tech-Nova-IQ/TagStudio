package com.technova.tagstudio.exception;

public class TagStudioException extends RuntimeException {
    public TagStudioException(String message) {
        super(message);
    }

    public TagStudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
