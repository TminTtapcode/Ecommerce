-- Apply once before deploying S1-05/06. Do not infer prior status for legacy BANNED shops.
ALTER TABLE shops ADD COLUMN prior_status VARCHAR(20) NULL;
