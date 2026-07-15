package com.inridemart.catalog.domain.ports;

import com.inridemart.catalog.application.ProductPage;
import com.inridemart.catalog.application.ProductSearchCriteria;
import com.inridemart.catalog.domain.Category;
import com.inridemart.catalog.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogRepository {
    List<Category> findAllCategories();

    ProductPage searchProducts(ProductSearchCriteria criteria);

    Optional<Product> findProductById(UUID id);
}
