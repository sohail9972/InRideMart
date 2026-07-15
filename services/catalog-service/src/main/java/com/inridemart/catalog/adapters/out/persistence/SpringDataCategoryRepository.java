package com.inridemart.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByOrderByNameAsc();
}
