CREATE TABLE shopping_carts (id UUID PRIMARY KEY,user_id UUID NOT NULL UNIQUE);
CREATE TABLE cart_items (id UUID PRIMARY KEY,cart_id UUID NOT NULL REFERENCES shopping_carts(id),product_id UUID NOT NULL,quantity INTEGER NOT NULL,CONSTRAINT uq_cart_product UNIQUE(cart_id,product_id));
