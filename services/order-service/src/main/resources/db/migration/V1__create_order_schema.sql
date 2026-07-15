CREATE TABLE orders (id UUID PRIMARY KEY, user_id UUID NOT NULL, idempotency_key VARCHAR(255) NOT NULL, status VARCHAR(30) NOT NULL, total_amount NUMERIC(12,2) NOT NULL, currency VARCHAR(3) NOT NULL, created_at TIMESTAMPTZ NOT NULL, CONSTRAINT uq_orders_user_key UNIQUE(user_id,idempotency_key));
CREATE TABLE order_items (order_id UUID NOT NULL REFERENCES orders(id), product_id UUID NOT NULL, product_name VARCHAR(180) NOT NULL, quantity INTEGER NOT NULL, unit_price NUMERIC(12,2) NOT NULL, currency VARCHAR(3) NOT NULL);
CREATE INDEX idx_orders_user_created ON orders(user_id,created_at DESC);
