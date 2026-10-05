package org.example.insurancepolicyservice.exception;

// Handles errors raised from Router, Handler, Service, Repository, and WebClient.
// It converts exceptions into a consistent JSON response with the correct HTTP status.

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.insurancepolicyservice.dto.response.ErrorResponseDTO;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(-2)
public class FunctionalExceptionHandler
        implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public FunctionalExceptionHandler(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable exception) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        HttpStatus status =
                determineStatus(exception);

        String message =
                determineMessage(
                        exception,
                        status
                );

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        byte[] responseBytes;

        try {

            responseBytes =
                    objectMapper.writeValueAsBytes(
                            errorResponse
                    );

        } catch (JsonProcessingException jsonException) {

            responseBytes =
                    """
                    {
                      "status": 500,
                      "error": "Internal Server Error",
                      "message": "Error response could not be created"
                    }
                    """
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );
        }

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        DataBuffer dataBuffer =
                exchange.getResponse()
                        .bufferFactory()
                        .wrap(responseBytes);

        return exchange.getResponse()
                .writeWith(
                        Mono.just(dataBuffer)
                );
    }

    private HttpStatus determineStatus(
            Throwable exception) {

        if (exception
                instanceof InsuranceProductNotFoundException
                || exception
                instanceof ProductCoverageNotFoundException
                || exception
                instanceof ProductExclusionNotFoundException
                || exception
                instanceof PolicyApplicationNotFoundException
                || exception
                instanceof PolicyNotFoundException) {

            return HttpStatus.NOT_FOUND;
        }

        if (exception
                instanceof DuplicateInsuranceProductException
                || exception
                instanceof DuplicateProductCoverageException
                || exception
                instanceof DuplicateProductExclusionException
                || exception
                instanceof DuplicatePolicyApplicationException
                || exception
                instanceof InvalidPolicyApplicationStateException) {

            return HttpStatus.CONFLICT;
        }

        if (exception
                instanceof InvalidRequestException
                || exception
                instanceof CustomerValidationException
                || exception
                instanceof AiTwinValidationException
                || exception
                instanceof IllegalArgumentException
                || exception
                instanceof ServerWebInputException
                || exception
                instanceof DecodingException) {

            return HttpStatus.BAD_REQUEST;
        }

        if (exception
                instanceof DownstreamServiceUnavailableException) {

            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (exception
                instanceof ResourceAccessDeniedException) {

            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(
            Throwable exception,
            HttpStatus status) {

        if (status
                == HttpStatus.INTERNAL_SERVER_ERROR) {

            exception.printStackTrace();

            return "An unexpected error occurred";
        }

        if (exception
                instanceof ServerWebInputException
                || exception
                instanceof DecodingException) {

            return "Invalid request body or enum value";
        }

        if (exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return status.getReasonPhrase();
        }

        return exception.getMessage();
    }
}