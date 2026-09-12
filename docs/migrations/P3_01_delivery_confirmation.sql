-- Additive P3-01 delivery evidence. Historical DELIVERED orders remain unknown (NULL).
ALTER TABLE orders
    ADD COLUMN delivered_at TIMESTAMP(6) NULL,
    ADD COLUMN delivery_confirmed_by_user_id BIGINT NULL,
    ADD COLUMN delivery_confirmation_source VARCHAR(20) NULL;
