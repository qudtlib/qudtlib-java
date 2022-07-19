package com.github.qudtlib.exception;

/**
 * Base excetpion for QUDTLib.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QudtException extends RuntimeException {
    public QudtException() {}

    public QudtException(String message) {
        super(message);
    }

    public QudtException(String message, Throwable cause) {
        super(message, cause);
    }

    public QudtException(Throwable cause) {
        super(cause);
    }

    public QudtException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
