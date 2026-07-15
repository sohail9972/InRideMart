package com.inridemart.customer.adapters.out.persistence;

import com.inridemart.customer.domain.Address;
import com.inridemart.customer.domain.CustomerProfile;
import com.inridemart.customer.domain.ports.CustomerProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCustomerProfileRepository implements CustomerProfileRepository {
    private final SpringDataCustomerProfileRepository repository;

    public JpaCustomerProfileRepository(SpringDataCustomerProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public Optional<CustomerProfile> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CustomerProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public CustomerProfile save(CustomerProfile profile) {
        return toDomain(repository.save(toEntity(profile)));
    }

    private CustomerProfileEntity toEntity(CustomerProfile profile) {
        Address address = profile.address();
        return new CustomerProfileEntity(
                profile.id(),
                profile.userId(),
                profile.fullName(),
                profile.phoneNumber(),
                address.addressLine1(),
                address.addressLine2(),
                address.city(),
                address.state(),
                address.postalCode(),
                address.country(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }

    private CustomerProfile toDomain(CustomerProfileEntity entity) {
        Address address = new Address(
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry()
        );
        return CustomerProfile.rehydrate(entity.getId(), entity.getUserId(), entity.getFullName(), entity.getPhoneNumber(), address, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
