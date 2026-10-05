package org.example.customerservice.exception;

public class CustomerProfileAlreadyExistsException
        extends RuntimeException {

    public CustomerProfileAlreadyExistsException(
            String message) {

        super(message);
    }
}