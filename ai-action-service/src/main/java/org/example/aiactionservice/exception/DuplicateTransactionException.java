package org.example.aiactionservice.exception;

public class DuplicateTransactionException
        extends RuntimeException {

    public DuplicateTransactionException(String message) {
        super(message);
    }
}