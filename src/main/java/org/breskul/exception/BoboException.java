package org.breskul.exception;

public class BoboException extends RuntimeException{

    public BoboException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoboException(String message) {
        super(message);
    }
}
