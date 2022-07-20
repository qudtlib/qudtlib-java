package io.github.qudtlib.exception;

/**
 * Indicates that something went wrong during intialization of the QUDTLib system.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QudtInitializationException extends QudtException {
    public QudtInitializationException() {}

    public QudtInitializationException(String message) {
        super(message);
    }

    public QudtInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QudtInitializationException(Throwable cause) {
        super(cause);
    }

    public QudtInitializationException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
