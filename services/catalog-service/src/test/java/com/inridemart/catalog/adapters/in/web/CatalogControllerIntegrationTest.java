package com.inridemart.catalog.adapters.in.web;

import com.inridemart.catalog.adapters.out.persistence.CategoryEntity;
import com.inridemart.catalog.adapters.out.persistence.ProductEntity;
import com.inridemart.catalog.adapters.out.persistence.SpringDataCategoryRepository;
import com.inridemart.catalog.adapters.out.persistence.SpringDataProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private SpringDataProductRepository products;
    @Autowired private SpringDataCategoryRepository categories;

    private UUID productId;

    @BeforeEach
    void setUp() {
        products.deleteAll();
        categories.deleteAll();
        UUID categoryId = UUID.randomUUID();
        entityManager.createNativeQuery("insert into catalog_categories (id, slug, name, description) values (?, ?, ?, ?)")
                .setParameter(1, categoryId).setParameter(2, "tech").setParameter(3, "Travel tech").setParameter(4, "Devices for the ride").executeUpdate();
        productId = UUID.randomUUID();
        entityManager.createNativeQuery("insert into catalog_products (id, category_id, sku, name, description, price, currency, active, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .setParameter(1, productId).setParameter(2, categoryId).setParameter(3, "IRM-CHARGE-001").setParameter(4, "Dual USB-C Charger").setParameter(5, "Fast charging for your ride").setParameter(6, new BigDecimal("799.00")).setParameter(7, "INR").setParameter(8, true).setParameter(9, Instant.parse("2026-07-10T00:00:00Z")).executeUpdate();
    }

    @Test
    void listsCategoriesAndSearchesProducts() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("tech"));

        mockMvc.perform(get("/api/v1/catalog/products?query=charger&category=tech&maxPrice=800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Dual USB-C Charger"));
    }

    @Test
    void findsProductAndRejectsInvalidBudget() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("IRM-CHARGE-001"));

        mockMvc.perform(get("/api/v1/catalog/products?minPrice=900&maxPrice=100"))
                .andExpect(status().isBadRequest());
    }
}
