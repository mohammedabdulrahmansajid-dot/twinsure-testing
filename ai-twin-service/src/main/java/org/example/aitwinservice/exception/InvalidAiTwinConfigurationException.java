package org.example.aitwinservice.exception;

public class InvalidAiTwinConfigurationException
        extends RuntimeException {

    public InvalidAiTwinConfigurationException(
            String message) {

        super(message);
    }
}