package org.example.claimsservice.service;

// Tests formal claim creation and Customer ownership rules.
// Repositories are mocked so these tests run without Spring, H2,
// Eureka, Gateway, or any other TwinSure microservice.

import org.example.claimsservice.dto.request.CreateClaimRequestDTO;
import org.example.claimsservice.enums.ClaimStatus;
import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.enums.IncidentType;
import org.example.claimsservice.exception.DuplicateClaimException;
import org.example.claimsservice.exception.IncidentNotFoundException;
import org.example.claimsservice.exception.InvalidClaimStateException;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.model.Claim;
import org.example.claimsservice.model.Incident;
import org.example.claimsservice.repo.ClaimDecisionRepo;
import org.example.claimsservice.repo.ClaimDocumentRepo;
import org.example.claimsservice.repo.ClaimRepo;
import org.example.claimsservice.repo.IncidentRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.reactive.function.client.WebClient;
class ClaimServiceTest {

    private ClaimRepo claimRepo;
    private IncidentRepo incidentRepo;
    private ClaimDocumentRepo documentRepo;
    private ClaimDecisionRepo decisionRepo;
    private ClaimService claimService;
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    void setUp() {

        claimRepo =
                Mockito.mock(
                        ClaimRepo.class
                );

        incidentRepo =
                Mockito.mock(
                        IncidentRepo.class
                );

        documentRepo =
                Mockito.mock(
                        ClaimDocumentRepo.class
                );

        decisionRepo =
                Mockito.mock(
                        ClaimDecisionRepo.class
                );

        webClientBuilder =
                Mockito.mock(
                        WebClient.Builder.class,
                        Mockito.RETURNS_DEEP_STUBS
                );

        claimService =
                new ClaimService(
                        claimRepo,
                        incidentRepo,
                        documentRepo,
                        decisionRepo,
                        webClientBuilder
                );
    }

    @Test
    void shouldCreateClaimFromReportedIncident() {

        Incident incident =
                createIncident(
                        IncidentStatus.REPORTED,
                        "7500.00"
                );

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        when(
                incidentRepo.findByIncidentIdAndCustomerId(
                        1L,
                        1L
                )
        )
                .thenReturn(
                        Mono.just(incident)
                );

        when(
                claimRepo.existsByIncidentId(
                        1L
                )
        )
                .thenReturn(
                        Mono.just(false)
                );

        when(
                claimRepo.save(
                        any(Claim.class)
                )
        )
                .thenAnswer(invocation -> {

                    Claim claim =
                            invocation.getArgument(0);

                    claim.setClaimId(
                            10L
                    );

                    return Mono.just(claim);
                });

        when(
                incidentRepo.save(
                        incident
                )
        )
                .thenReturn(
                        Mono.just(incident)
                );

        StepVerifier.create(
                        claimService.createClaim(
                                1L,
                                request
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            10L,
                            response.claimId()
                    );

                    assertEquals(
                            1L,
                            response.incidentId()
                    );

                    assertEquals(
                            1L,
                            response.customerId()
                    );

                    assertEquals(
                            1L,
                            response.twinId()
                    );

                    assertEquals(
                            1L,
                            response.policyId()
                    );

                    assertEquals(
                            2L,
                            response.actionId()
                    );

                    assertBigDecimalEquals(
                            "7000.00",
                            response.claimedAmount()
                    );

                    assertEquals(
                            ClaimStatus.SUBMITTED,
                            response.status()
                    );

                    assertNull(
                            response.approvedAmount()
                    );

                    assertNull(
                            response.deductibleApplied()
                    );

                    assertNull(
                            response.assignedAdjusterId()
                    );

                    assertNotNull(
                            response.claimNumber()
                    );

                    assertNotNull(
                            response.submittedAt()
                    );

                    assertNotNull(
                            response.updatedAt()
                    );

                    assertEquals(
                            IncidentStatus.CLAIM_CREATED,
                            incident.getStatus()
                    );
                })
                .verifyComplete();

        verify(claimRepo)
                .save(
                        any(Claim.class)
                );

        verify(incidentRepo)
                .save(incident);
    }

    @Test
    void shouldRejectDuplicateClaimForIncident() {

        Incident incident =
                createIncident(
                        IncidentStatus.REPORTED,
                        "7500.00"
                );

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        when(
                incidentRepo.findByIncidentIdAndCustomerId(
                        1L,
                        1L
                )
        )
                .thenReturn(
                        Mono.just(incident)
                );

        when(
                claimRepo.existsByIncidentId(
                        1L
                )
        )
                .thenReturn(
                        Mono.just(true)
                );

        StepVerifier.create(
                        claimService.createClaim(
                                1L,
                                request
                        )
                )
                .expectErrorMatches(exception ->
                        exception
                                instanceof DuplicateClaimException
                                && exception.getMessage()
                                .contains(
                                        "already been created"
                                )
                )
                .verify();

        verify(
                claimRepo,
                never()
        )
                .save(
                        any(Claim.class)
                );

        verify(
                incidentRepo,
                never()
        )
                .save(
                        any(Incident.class)
                );
    }

    @Test
    void shouldRejectClaimAmountAboveReportedLoss() {

        Incident incident =
                createIncident(
                        IncidentStatus.REPORTED,
                        "5000.00"
                );

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        when(
                incidentRepo.findByIncidentIdAndCustomerId(
                        1L,
                        1L
                )
        )
                .thenReturn(
                        Mono.just(incident)
                );

        StepVerifier.create(
                        claimService.createClaim(
                                1L,
                                request
                        )
                )
                .expectErrorMatches(exception ->
                        exception
                                instanceof InvalidRequestException
                                && exception.getMessage()
                                .contains(
                                        "cannot be greater"
                                )
                )
                .verify();

        verify(
                claimRepo,
                never()
        )
                .existsByIncidentId(
                        1L
                );

        verify(
                claimRepo,
                never()
        )
                .save(
                        any(Claim.class)
                );
    }

    @Test
    void shouldRejectIncidentThatIsNotReported() {

        Incident incident =
                createIncident(
                        IncidentStatus.CLAIM_CREATED,
                        "7500.00"
                );

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        when(
                incidentRepo.findByIncidentIdAndCustomerId(
                        1L,
                        1L
                )
        )
                .thenReturn(
                        Mono.just(incident)
                );

        StepVerifier.create(
                        claimService.createClaim(
                                1L,
                                request
                        )
                )
                .expectErrorMatches(exception ->
                        exception
                                instanceof InvalidClaimStateException
                                && exception.getMessage()
                                .contains(
                                        "REPORTED status"
                                )
                )
                .verify();

        verify(
                claimRepo,
                never()
        )
                .existsByIncidentId(
                        1L
                );

        verify(
                claimRepo,
                never()
        )
                .save(
                        any(Claim.class)
                );
    }

    @Test
    void shouldReturnNotFoundForUnownedIncident() {

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        when(
                incidentRepo.findByIncidentIdAndCustomerId(
                        1L,
                        2L
                )
        )
                .thenReturn(
                        Mono.empty()
                );

        StepVerifier.create(
                        claimService.createClaim(
                                2L,
                                request
                        )
                )
                .expectErrorMatches(exception ->
                        exception
                                instanceof IncidentNotFoundException
                                && exception.getMessage()
                                .equals(
                                        "Incident not found"
                                )
                )
                .verify();

        verify(
                claimRepo,
                never()
        )
                .save(
                        any(Claim.class)
                );
    }

    @Test
    void shouldRejectMissingCustomerProfile() {

        CreateClaimRequestDTO request =
                new CreateClaimRequestDTO(
                        1L,
                        new BigDecimal("7000.00")
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> claimService.createClaim(
                                null,
                                request
                        )
                );

        assertEquals(
                "Customer profile is not linked. "
                        + "Create a Customer profile "
                        + "and log in again.",
                exception.getMessage()
        );

        verify(
                incidentRepo,
                never()
        )
                .findByIncidentIdAndCustomerId(
                        any(Long.class),
                        any(Long.class)
                );
    }

    private Incident createIncident(
            IncidentStatus status,
            String lossAmount) {

        Incident incident =
                new Incident();

        incident.setIncidentId(
                1L
        );

        incident.setCustomerId(
                1L
        );

        incident.setTwinId(
                1L
        );

        incident.setPolicyId(
                1L
        );

        incident.setActionId(
                2L
        );

        incident.setIncidentType(
                IncidentType.LIMIT_EXCEEDED
        );

        incident.setDescription(
                "The AI Twin exceeded the configured purchase limit"
        );

        incident.setLossAmount(
                new BigDecimal(lossAmount)
        );

        incident.setActionOccurredAt(
                LocalDateTime.now()
                        .minusDays(1)
        );

        incident.setReportedAt(
                LocalDateTime.now()
        );

        incident.setStatus(
                status
        );

        return incident;
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual) {

        assertEquals(
                0,
                new BigDecimal(expected)
                        .compareTo(actual)
        );
    }
}