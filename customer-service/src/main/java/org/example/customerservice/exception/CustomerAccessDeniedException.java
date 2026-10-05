package org.example.customerservice.exception;

public class CustomerAccessDeniedException
        extends RuntimeException {

    public CustomerAccessDeniedException(String message) {
        super(message);
    }
}