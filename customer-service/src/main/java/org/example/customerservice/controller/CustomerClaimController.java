package org.example.customerservice.controller;

import org.example.customerservice.dto.response.CustomerClaimSummaryResponseDTO;
import org.example.customerservice.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/customers")
public class CustomerClaimController {

    private final CustomerService customerService;

    public CustomerClaimController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    @GetMapping("/{customerId}/claim-summary")
    public Mono<ResponseEntity<CustomerClaimSummaryResponseDTO>>
    getClaimSummary(
            @PathVariable Long customerId) {

        return customerService
                .getClaimSummary(customerId)
                .map(ResponseEntity::ok);
    }
}