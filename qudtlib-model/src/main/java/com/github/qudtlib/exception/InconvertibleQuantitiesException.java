package com.github.qudtlib.exception;
/**
 * Indicates that quantities cannot be converted.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class InconvertibleQuantitiesException extends QudtException {
    public InconvertibleQuantitiesException() {}

    public InconvertibleQuantitiesException(String message) {
        super(message);
    }

    public InconvertibleQuantitiesException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconvertibleQuantitiesException(Throwable cause) {
        super(cause);
    }

    public InconvertibleQuantitiesException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
