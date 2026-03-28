package com.vbforge.jwtspringjpa.exception;

public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException(String username) {

        super("User already exists: " + username);
    }

}
