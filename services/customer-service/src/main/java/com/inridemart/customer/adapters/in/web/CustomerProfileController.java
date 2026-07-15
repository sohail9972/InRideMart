package com.inridemart.customer.adapters.in.web;

import com.inridemart.customer.application.CreateCustomerProfileCommand;
import com.inridemart.customer.application.CreateCustomerProfileUseCase;
import com.inridemart.customer.application.CustomerProfileQuery;
import com.inridemart.customer.application.UpdateCustomerProfileCommand;
import com.inridemart.customer.application.UpdateCustomerProfileUseCase;
import com.inridemart.customer.domain.Address;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerProfileController {
    private final CreateCustomerProfileUseCase createCustomerProfile;
    private final UpdateCustomerProfileUseCase updateCustomerProfile;
    private final CustomerProfileQuery customerProfiles;

    public CustomerProfileController(CreateCustomerProfileUseCase createCustomerProfile, UpdateCustomerProfileUseCase updateCustomerProfile, CustomerProfileQuery customerProfiles) {
        this.createCustomerProfile = createCustomerProfile;
        this.updateCustomerProfile = updateCustomerProfile;
        this.customerProfiles = customerProfiles;
    }

    @PostMapping
    public ResponseEntity<CustomerProfileResponse> create(Authentication authentication, @Valid @RequestBody CreateCustomerProfileRequest request) {
        UUID userId = requestedOrCurrentUserId(authentication, request.userId());
        CreateCustomerProfileCommand command = new CreateCustomerProfileCommand(userId, request.fullName(), request.phoneNumber(), toAddress(request.address()));
        CustomerProfileResponse response = CustomerProfileResponse.from(createCustomerProfile.create(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public CustomerProfileResponse findById(Authentication authentication, @PathVariable("id") UUID id) {
        return responseForAuthorizedUser(authentication, customerProfiles.findById(id));
    }

    @GetMapping("/by-user/{userId}")
    public CustomerProfileResponse findByUserId(Authentication authentication, @PathVariable("userId") UUID userId) {
        if (!isAdmin(authentication) && !currentUserId(authentication).equals(userId)) {
            throw new AccessDeniedException("You can only access your own customer profile");
        }
        return responseForAuthorizedUser(authentication, customerProfiles.findByUserId(userId));
    }

    @PutMapping("/{id}")
    public CustomerProfileResponse update(Authentication authentication, @PathVariable("id") UUID id, @Valid @RequestBody UpdateCustomerProfileRequest request) {
        responseForAuthorizedUser(authentication, customerProfiles.findById(id));
        UpdateCustomerProfileCommand command = new UpdateCustomerProfileCommand(request.fullName(), request.phoneNumber(), toAddress(request.address()));
        return CustomerProfileResponse.from(updateCustomerProfile.update(id, command));
    }

    private CustomerProfileResponse responseForAuthorizedUser(Authentication authentication, com.inridemart.customer.domain.CustomerProfile profile) {
        if (!isAdmin(authentication) && !currentUserId(authentication).equals(profile.userId())) {
            throw new AccessDeniedException("You can only access your own customer profile");
        }
        return CustomerProfileResponse.from(profile);
    }

    private UUID requestedOrCurrentUserId(Authentication authentication, UUID requestedUserId) {
        UUID currentUserId = currentUserId(authentication);
        if (requestedUserId == null || requestedUserId.equals(currentUserId) || isAdmin(authentication)) {
            return requestedUserId == null ? currentUserId : requestedUserId;
        }
        throw new AccessDeniedException("You can only create your own customer profile");
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private Address toAddress(AddressRequest request) {
        if (request == null) {
            return new Address(null, null, null, null, null, null);
        }
        return new Address(request.addressLine1(), request.addressLine2(), request.city(), request.state(), request.postalCode(), request.country());
    }
}
