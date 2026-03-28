package com.vbforge.jwtspringjpa.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {

        super(resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String name) {

        super(resource + " not found with name: " + name);
    }
}
