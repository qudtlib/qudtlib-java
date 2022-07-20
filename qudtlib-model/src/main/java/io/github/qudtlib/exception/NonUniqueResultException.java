package io.github.qudtlib.exception;

/**
 * Indicates that a unique result was expected, but multiple found.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class NonUniqueResultException extends QudtException {
    public NonUniqueResultException() {}

    public NonUniqueResultException(String message) {
        super(message);
    }

    public NonUniqueResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueResultException(Throwable cause) {
        super(cause);
    }

    public NonUniqueResultException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
