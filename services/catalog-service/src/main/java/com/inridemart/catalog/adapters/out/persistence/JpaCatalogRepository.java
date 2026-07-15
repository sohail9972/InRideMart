package com.inridemart.catalog.adapters.out.persistence;

import com.inridemart.catalog.application.ProductPage;
import com.inridemart.catalog.application.ProductSearchCriteria;
import com.inridemart.catalog.domain.Category;
import com.inridemart.catalog.domain.Product;
import com.inridemart.catalog.domain.ports.CatalogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCatalogRepository implements CatalogRepository {
    private final SpringDataCategoryRepository categories;
    private final SpringDataProductRepository products;

    public JpaCatalogRepository(SpringDataCategoryRepository categories, SpringDataProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    public List<Category> findAllCategories() {
        return categories.findAllByOrderByNameAsc().stream().map(this::toCategory).toList();
    }

    @Override
    public ProductPage searchProducts(ProductSearchCriteria criteria) {
        Page<ProductEntity> page = products.search(criteria.query(), criteria.category(), criteria.minPrice(), criteria.maxPrice(),
                PageRequest.of(criteria.page(), criteria.size(), sortFor(criteria.sort())));
        return new ProductPage(page.getContent().stream().map(this::toProduct).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Product> findProductById(UUID id) {
        return products.findActiveById(id).map(this::toProduct);
    }

    private Sort sortFor(String sort) {
        return switch (sort) {
            case "PRICE_ASC" -> Sort.by("price").ascending();
            case "PRICE_DESC" -> Sort.by("price").descending();
            case "NEWEST" -> Sort.by("createdAt").descending();
            default -> Sort.by("name").ascending();
        };
    }

    private Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getSku(), entity.getName(), entity.getDescription(), entity.getPrice(), entity.getCurrency(), entity.getImageUrl(), toCategory(entity.getCategory()), entity.getCreatedAt());
    }

    private Category toCategory(CategoryEntity entity) {
        return new Category(entity.getId(), entity.getSlug(), entity.getName(), entity.getDescription());
    }
}
