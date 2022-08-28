package org.breskul.exception;

public class BoboException extends RuntimeException{

    public BoboException(String message) {
        super(message);
    }

    public BoboException(Throwable cause) {
        super(cause);
    }
}
