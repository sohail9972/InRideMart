CREATE TABLE catalog_categories (
    id UUID PRIMARY KEY,
    slug VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE catalog_products (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES catalog_categories(id),
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) NOT NULL,
    image_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_catalog_products_category ON catalog_products(category_id);
CREATE INDEX idx_catalog_products_active_price ON catalog_products(active, price);
