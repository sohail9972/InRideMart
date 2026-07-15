package com.inridemart.catalog.adapters.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {
    @Query("""
            select p from ProductEntity p join fetch p.category c
            where p.active = true
              and (coalesce(:query, '') = '' or lower(p.name) like lower(concat('%', :query, '%')) or lower(p.description) like lower(concat('%', :query, '%')))
              and coalesce(:category, c.slug) = c.slug
              and p.price >= coalesce(:minPrice, p.price)
              and p.price <= coalesce(:maxPrice, p.price)
            """)
    Page<ProductEntity> search(@Param("query") String query, @Param("category") String category,
                               @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);

    @Query("select p from ProductEntity p join fetch p.category where p.id = :id and p.active = true")
    Optional<ProductEntity> findActiveById(@Param("id") UUID id);
}
