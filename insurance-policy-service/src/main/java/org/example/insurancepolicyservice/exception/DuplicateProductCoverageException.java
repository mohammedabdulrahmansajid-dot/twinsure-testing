package org.example.insurancepolicyservice.exception;

public class DuplicateProductCoverageException
        extends RuntimeException {

    public DuplicateProductCoverageException(
            String message) {

        super(message);
    }
}