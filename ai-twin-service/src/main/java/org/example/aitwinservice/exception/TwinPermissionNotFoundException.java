package org.example.aitwinservice.exception;

public class TwinPermissionNotFoundException
        extends RuntimeException {

    public TwinPermissionNotFoundException(
            String message) {

        super(message);
    }
}