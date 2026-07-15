package com.inridemart.catalog.application;

import com.inridemart.catalog.domain.Category;
import com.inridemart.catalog.domain.Product;
import com.inridemart.catalog.domain.ports.CatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogQuery {
    private final CatalogRepository catalog;

    public CatalogQuery(CatalogRepository catalog) {
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public List<Category> categories() {
        return catalog.findAllCategories();
    }

    @Transactional(readOnly = true)
    public ProductPage products(ProductSearchCriteria criteria) {
        return catalog.searchProducts(criteria);
    }

    @Transactional(readOnly = true)
    public Product product(UUID id) {
        return catalog.findProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }
}
