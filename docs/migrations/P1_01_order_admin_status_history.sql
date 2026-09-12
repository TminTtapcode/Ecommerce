-- Additive migration. Keep this table on application rollback; do not backfill history.
CREATE TABLE order_admin_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    old_status VARCHAR(255) NOT NULL,
    new_status VARCHAR(255) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_admin_history_order_id (order_id, id)
) ENGINE=InnoDB;
