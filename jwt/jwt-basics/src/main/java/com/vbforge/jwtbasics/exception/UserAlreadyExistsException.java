package com.vbforge.jwtbasics.exception;

/**
 * Thrown when a registration attempt uses an already-taken username or email.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}