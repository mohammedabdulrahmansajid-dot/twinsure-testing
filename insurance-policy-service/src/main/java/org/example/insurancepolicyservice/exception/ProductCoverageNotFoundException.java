package org.example.insurancepolicyservice.exception;

public class ProductCoverageNotFoundException
        extends RuntimeException {

    public ProductCoverageNotFoundException(
            String message) {

        super(message);
    }
}