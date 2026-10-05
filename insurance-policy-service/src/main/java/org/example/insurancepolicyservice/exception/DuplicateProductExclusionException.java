package org.example.insurancepolicyservice.exception;

public class DuplicateProductExclusionException
        extends RuntimeException {

    public DuplicateProductExclusionException(
            String message) {

        super(message);
    }
}