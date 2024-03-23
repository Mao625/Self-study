package com.mcx.springmvc.exception;

public class ContentException extends RuntimeException{
    public ContentException() {
        super();
    }

    public ContentException(String message) {
        super(message);
    }

    public ContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentException(Throwable cause) {
        super(cause);
    }
}
