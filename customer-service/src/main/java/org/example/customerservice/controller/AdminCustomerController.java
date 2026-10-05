package org.example.customerservice.controller;

import jakarta.validation.Valid;
import org.example.customerservice.dto.request.CustomerStatusRequestDTO;
import org.example.customerservice.dto.response.CustomerProfileResponseDTO;
import org.example.customerservice.dto.response.CustomerResponseDTO;
import org.example.customerservice.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final CustomerService customerService;

    public AdminCustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    @GetMapping
    public Flux<CustomerResponseDTO> getAllCustomers() {

        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerProfileResponseDTO>>
    getCustomerById(
            @PathVariable Long customerId) {

        return customerService
                .getCustomerById(customerId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{customerId}/status")
    public Mono<ResponseEntity<CustomerResponseDTO>>
    updateCustomerStatus(
            @PathVariable Long customerId,

            @Valid
            @RequestBody
            CustomerStatusRequestDTO request) {

        return customerService
                .updateCustomerStatus(
                        customerId,
                        request
                )
                .map(ResponseEntity::ok);
    }
}
