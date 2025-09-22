package com.ibra.simple_full_stack.exception;

public class PhotoUploadException extends RuntimeException {
    public PhotoUploadException(String message) {
        super(message);
    }
    
    public PhotoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
