package org.example.insurancepolicyservice.exception;

public class AiTwinValidationException
        extends RuntimeException {

    public AiTwinValidationException(
            String message) {

        super(message);
    }
}