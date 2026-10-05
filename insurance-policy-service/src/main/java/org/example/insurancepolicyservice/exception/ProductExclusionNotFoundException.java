package org.example.insurancepolicyservice.exception;

public class ProductExclusionNotFoundException
        extends RuntimeException {

    public ProductExclusionNotFoundException(
            String message) {

        super(message);
    }
}