package io.github.qudtlib.exception;

public class IncompleteDataException extends QudtException {
    public IncompleteDataException() {}

    public IncompleteDataException(String message) {
        super(message);
    }

    public IncompleteDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteDataException(Throwable cause) {
        super(cause);
    }

    public IncompleteDataException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
