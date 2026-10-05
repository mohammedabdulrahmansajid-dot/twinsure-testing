package org.example.insurancepolicyservice.exception;

public class CustomerValidationException
        extends RuntimeException {

    public CustomerValidationException(
            String message) {

        super(message);
    }
}