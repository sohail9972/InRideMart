CREATE TABLE customer_profiles (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL UNIQUE,
  full_name VARCHAR(150) NOT NULL,
  phone_number VARCHAR(30),
  address_line1 VARCHAR(255),
  address_line2 VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(100),
  postal_code VARCHAR(20),
  country VARCHAR(100),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_customer_profiles_user_id ON customer_profiles(user_id);
