package org.example.customerservice.controller;

import jakarta.validation.Valid;
import org.example.customerservice.dto.request.CreateCustomerProfileRequestDTO;
import org.example.customerservice.dto.request.UpdateCustomerProfileRequestDTO;
import org.example.customerservice.dto.response.CustomerProfileResponseDTO;
import org.example.customerservice.filter.JwtFilter;
import org.example.customerservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    @PostMapping("/profile")
    public Mono<ResponseEntity<CustomerProfileResponseDTO>>
    createProfile(
            Authentication authentication,

            @Valid
            @RequestBody
            CreateCustomerProfileRequestDTO request) {

        JwtFilter.JwtUserDetails jwtDetails =
                getJwtDetails(authentication);

        return customerService
                .createProfile(
                        jwtDetails.userId(),
                        jwtDetails.token(),
                        request
                )
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/profile")
    public Mono<ResponseEntity<CustomerProfileResponseDTO>>
    getOwnProfile(
            Authentication authentication) {

        JwtFilter.JwtUserDetails jwtDetails =
                getJwtDetails(authentication);

        return customerService
                .getOwnProfile(jwtDetails.userId())
                .map(ResponseEntity::ok);
    }

    @PutMapping("/profile")
    public Mono<ResponseEntity<CustomerProfileResponseDTO>>
    updateOwnProfile(
            Authentication authentication,

            @Valid
            @RequestBody
            UpdateCustomerProfileRequestDTO request) {

        JwtFilter.JwtUserDetails jwtDetails =
                getJwtDetails(authentication);

        return customerService
                .updateOwnProfile(
                        jwtDetails.userId(),
                        request
                )
                .map(ResponseEntity::ok);
    }

    private JwtFilter.JwtUserDetails getJwtDetails(
            Authentication authentication) {

        return (JwtFilter.JwtUserDetails)
                authentication.getDetails();
    }
}