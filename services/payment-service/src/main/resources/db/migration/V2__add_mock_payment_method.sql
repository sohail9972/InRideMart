ALTER TABLE payments ADD COLUMN payment_method VARCHAR(30);

UPDATE payments SET payment_method = 'UPI' WHERE payment_method IS NULL;

ALTER TABLE payments ALTER COLUMN payment_method SET NOT NULL;
