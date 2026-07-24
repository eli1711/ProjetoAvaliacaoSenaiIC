DROP PROCEDURE IF EXISTS cpa_add_column_if_missing;
DROP PROCEDURE IF EXISTS cpa_add_index_if_missing;

DELIMITER $$

CREATE PROCEDURE cpa_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = p_statement;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE cpa_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = p_statement;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL cpa_add_column_if_missing(
    'users',
    'failed_login_attempts',
    'ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0'
);

CALL cpa_add_column_if_missing(
    'users',
    'locked_until',
    'ALTER TABLE users ADD COLUMN locked_until DATETIME(6)'
);

CALL cpa_add_column_if_missing(
    'users',
    'password_changed_at',
    'ALTER TABLE users ADD COLUMN password_changed_at DATETIME(6)'
);

CALL cpa_add_column_if_missing(
    'users',
    'must_change_password',
    'ALTER TABLE users ADD COLUMN must_change_password BIT NOT NULL DEFAULT 0'
);

CALL cpa_add_index_if_missing(
    'users',
    'idx_users_locked_until',
    'CREATE INDEX idx_users_locked_until ON users (locked_until)'
);

DROP PROCEDURE IF EXISTS cpa_add_column_if_missing;
DROP PROCEDURE IF EXISTS cpa_add_index_if_missing;
