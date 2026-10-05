package org.example.insurancepolicyservice.exception;

public class PolicyNotFoundException
        extends RuntimeException {

    public PolicyNotFoundException(
            String message) {

        super(message);
    }
}