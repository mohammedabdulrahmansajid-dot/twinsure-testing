package org.example.customerservice.controller;

import org.example.customerservice.dto.response.CustomerValidationResponseDTO;
import org.example.customerservice.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/customers")
public class InternalCustomerController {

    private final CustomerService customerService;

    public InternalCustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    @GetMapping("/{customerId}/validation")
    public Mono<ResponseEntity<CustomerValidationResponseDTO>>
    validateCustomer(
            @PathVariable Long customerId) {

        return customerService
                .validateCustomer(customerId)
                .map(ResponseEntity::ok);
    }
}
