CREATE DATABASE customerdb;

-- Create subscriptionDB database
CREATE DATABASE subscriptiondb;

\c subscriptiondb;
-- Create the 'offers' table
CREATE TABLE IF NOT EXISTS offers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    price NUMERIC(38, 2),
    period VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- Insert the test offer that our E2E test needs
INSERT INTO offers (id, name, description, price, period, created_at, updated_at)
VALUES (1, 'Premium Plan', 'Monthly Premium Access', 99.99, 'MONTHLY', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;