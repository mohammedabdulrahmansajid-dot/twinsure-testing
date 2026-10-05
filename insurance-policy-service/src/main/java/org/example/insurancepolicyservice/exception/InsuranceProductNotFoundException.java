package org.example.insurancepolicyservice.exception;

public class InsuranceProductNotFoundException
        extends RuntimeException {

    public InsuranceProductNotFoundException(
            String message) {

        super(message);
    }
}