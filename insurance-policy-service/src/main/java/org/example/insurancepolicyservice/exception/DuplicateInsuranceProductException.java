package org.example.insurancepolicyservice.exception;

public class DuplicateInsuranceProductException
        extends RuntimeException {

    public DuplicateInsuranceProductException(
            String message) {

        super(message);
    }
}