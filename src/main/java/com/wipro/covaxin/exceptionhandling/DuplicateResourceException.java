package com.wipro.covaxin.exceptionhandling;

public class DuplicateResourceException extends Exception{
    public DuplicateResourceException() {
        super();
    }
    public DuplicateResourceException(final String message) {
        super(message);
    }
}
