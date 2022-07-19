package com.github.qudtlib.exception;

/**
 * Indicates that an expected result was not found.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class NotFoundException extends QudtException {
    public NotFoundException() {}

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
