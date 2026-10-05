package org.example.customerservice.service;

import org.example.customerservice.dto.request.CreateCustomerProfileRequestDTO;
import org.example.customerservice.dto.request.CustomerLinkRequestDTO;
import org.example.customerservice.dto.request.CustomerStatusRequestDTO;
import org.example.customerservice.dto.request.UpdateCustomerProfileRequestDTO;
import org.example.customerservice.dto.response.CustomerClaimSummaryResponseDTO;
import org.example.customerservice.dto.response.CustomerProfileResponseDTO;
import org.example.customerservice.dto.response.CustomerResponseDTO;
import org.example.customerservice.dto.response.CustomerValidationResponseDTO;
import org.example.customerservice.enums.CustomerStatus;
import org.example.customerservice.exception.CustomerNotFoundException;
import org.example.customerservice.exception.CustomerProfileAlreadyExistsException;
import org.example.customerservice.exception.EmailAlreadyExistsException;
import org.example.customerservice.exception.IdentityServiceException;
import org.example.customerservice.model.Customer;
import org.example.customerservice.repo.CustomerRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final WebClient.Builder webClientBuilder;

    public CustomerService(
            CustomerRepo customerRepo,
            WebClient.Builder webClientBuilder) {

        this.customerRepo = customerRepo;
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<CustomerProfileResponseDTO> createProfile(
            Long userId,
            String jwtToken,
            CreateCustomerProfileRequestDTO request) {

        String normalizedEmail =
                request.email().trim().toLowerCase();

        return customerRepo.existsByUserId(userId)

                .flatMap(profileExists -> {

                    if (profileExists) {
                        return Mono.error(
                                new CustomerProfileAlreadyExistsException(
                                        "Customer profile already exists "
                                                + "for user ID: "
                                                + userId
                                )
                        );
                    }

                    return customerRepo
                            .existsByEmail(normalizedEmail);
                })

                .flatMap(emailExists -> {

                    if (emailExists) {
                        return Mono.error(
                                new EmailAlreadyExistsException(
                                        "Email already exists: "
                                                + normalizedEmail
                                )
                        );
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    Customer customer = new Customer();

                    customer.setUserId(userId);

                    customer.setFullName(
                            request.fullName().trim()
                    );

                    customer.setEmail(normalizedEmail);

                    customer.setPhone(
                            request.phone().trim()
                    );

                    customer.setAddress(
                            request.address().trim()
                    );

                    customer.setStatus(
                            CustomerStatus.ACTIVE
                    );

                    customer.setCreatedAt(currentTime);
                    customer.setUpdatedAt(currentTime);

                    return customerRepo.save(customer);
                })

                .flatMap(savedCustomer ->
                        linkCustomerToIdentity(
                                userId,
                                savedCustomer.getCustomerId(),
                                jwtToken
                        )
                                .thenReturn(savedCustomer)

                                .onErrorResume(exception ->
                                        customerRepo
                                                .deleteById(
                                                        savedCustomer
                                                                .getCustomerId()
                                                )
                                                .then(
                                                        Mono.error(
                                                                new IdentityServiceException(
                                                                        "Customer profile could not "
                                                                                + "be linked to the "
                                                                                + "Identity Service"
                                                                )
                                                        )
                                                )
                                )
                )

                .map(this::convertToProfileResponse);
    }

    public Mono<CustomerProfileResponseDTO> getOwnProfile(
            Long userId) {

        return findByUserId(userId)
                .map(this::convertToProfileResponse);
    }

    public Mono<CustomerProfileResponseDTO> updateOwnProfile(
            Long userId,
            UpdateCustomerProfileRequestDTO request) {

        return findByUserId(userId)

                .flatMap(existingCustomer -> {

                    String normalizedEmail =
                            request.email()
                                    .trim()
                                    .toLowerCase();

                    boolean emailUnchanged =
                            existingCustomer.getEmail()
                                    .equalsIgnoreCase(
                                            normalizedEmail
                                    );

                    if (emailUnchanged) {
                        updateCustomerFields(
                                existingCustomer,
                                request,
                                normalizedEmail
                        );

                        return customerRepo.save(
                                existingCustomer
                        );
                    }

                    return customerRepo
                            .existsByEmail(normalizedEmail)

                            .flatMap(emailExists -> {

                                if (emailExists) {
                                    return Mono.error(
                                            new EmailAlreadyExistsException(
                                                    "Email already exists: "
                                                            + normalizedEmail
                                            )
                                    );
                                }

                                updateCustomerFields(
                                        existingCustomer,
                                        request,
                                        normalizedEmail
                                );

                                return customerRepo.save(
                                        existingCustomer
                                );
                            });
                })

                .map(this::convertToProfileResponse);
    }

    public Mono<CustomerClaimSummaryResponseDTO>
    getClaimSummary(Long customerId) {

        return findByCustomerId(customerId)
                .map(customer ->
                        new CustomerClaimSummaryResponseDTO(
                                customer.getCustomerId(),
                                customer.getFullName(),
                                customer.getEmail(),
                                customer.getPhone(),
                                customer.getStatus()
                        )
                );
    }

    public Flux<CustomerResponseDTO> getAllCustomers() {

        return customerRepo.findAll()
                .map(this::convertToCustomerResponse);
    }

    public Mono<CustomerProfileResponseDTO> getCustomerById(
            Long customerId) {

        return findByCustomerId(customerId)
                .map(this::convertToProfileResponse);
    }

    public Mono<CustomerResponseDTO> updateCustomerStatus(
            Long customerId,
            CustomerStatusRequestDTO request) {

        return findByCustomerId(customerId)

                .flatMap(customer -> {

                    customer.setStatus(request.status());

                    customer.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return customerRepo.save(customer);
                })

                .map(this::convertToCustomerResponse);
    }

    public Mono<CustomerValidationResponseDTO>
    validateCustomer(Long customerId) {

        return findByCustomerId(customerId)

                .map(customer -> {

                    boolean valid =
                            customer.getStatus()
                                    == CustomerStatus.ACTIVE;

                    return new CustomerValidationResponseDTO(
                            customer.getCustomerId(),
                            customer.getUserId(),
                            customer.getStatus(),
                            valid
                    );
                });
    }

    private Mono<Customer> findByUserId(
            Long userId) {

        return customerRepo.findByUserId(userId)

                .switchIfEmpty(
                        Mono.error(
                                new CustomerNotFoundException(
                                        "Customer profile not found "
                                                + "for user ID: "
                                                + userId
                                )
                        )
                );
    }

    private Mono<Customer> findByCustomerId(
            Long customerId) {

        return customerRepo.findById(customerId)

                .switchIfEmpty(
                        Mono.error(
                                new CustomerNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                )
                        )
                );
    }

    private Mono<Void> linkCustomerToIdentity(
            Long userId,
            Long customerId,
            String jwtToken) {

        CustomerLinkRequestDTO request =
                new CustomerLinkRequestDTO(customerId);

        return webClientBuilder
                .build()

                .put()

                .uri(
                        "http://IDENTITY-SERVICE"
                                + "/internal/users/"
                                + userId
                                + "/customer-link"
                )

                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )

                .bodyValue(request)

                .retrieve()

                .bodyToMono(Void.class);
    }

    private void updateCustomerFields(
            Customer customer,
            UpdateCustomerProfileRequestDTO request,
            String normalizedEmail) {

        customer.setFullName(
                request.fullName().trim()
        );

        customer.setEmail(normalizedEmail);

        customer.setPhone(
                request.phone().trim()
        );

        customer.setAddress(
                request.address().trim()
        );

        customer.setUpdatedAt(
                LocalDateTime.now()
        );
    }

    private CustomerProfileResponseDTO
    convertToProfileResponse(Customer customer) {

        return new CustomerProfileResponseDTO(
                customer.getCustomerId(),
                customer.getUserId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private CustomerResponseDTO
    convertToCustomerResponse(Customer customer) {

        return new CustomerResponseDTO(
                customer.getCustomerId(),
                customer.getUserId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus()
        );
    }
}