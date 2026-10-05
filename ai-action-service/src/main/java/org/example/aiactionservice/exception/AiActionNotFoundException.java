package org.example.aiactionservice.exception;

public class AiActionNotFoundException
        extends RuntimeException {

    public AiActionNotFoundException(String message) {
        super(message);
    }
}