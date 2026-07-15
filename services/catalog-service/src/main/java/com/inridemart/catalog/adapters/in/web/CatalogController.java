package com.inridemart.catalog.adapters.in.web;

import com.inridemart.catalog.application.CatalogQuery;
import com.inridemart.catalog.application.ProductSearchCriteria;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
public class CatalogController {
    private final CatalogQuery catalog;

    public CatalogController(CatalogQuery catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalog.categories().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/products")
    public ProductPageResponse products(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", defaultValue = "RELEVANCE") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {
        return ProductPageResponse.from(catalog.products(new ProductSearchCriteria(query, category, minPrice, maxPrice, sort, page, size)));
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable("id") UUID id) {
        return ProductResponse.from(catalog.product(id));
    }
}
