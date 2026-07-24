CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    data_hora DATETIME(6) NOT NULL,
    usuario VARCHAR(255),
    instituicao_id BIGINT,
    operacao VARCHAR(255) NOT NULL,
    entidade VARCHAR(255),
    entidade_id VARCHAR(255),
    ip VARCHAR(255),
    detalhes TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_log_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS cpa_add_index_if_missing;

DELIMITER $$

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

CALL cpa_add_index_if_missing(
    'audit_log',
    'idx_audit_log_instituicao',
    'CREATE INDEX idx_audit_log_instituicao ON audit_log (instituicao_id)'
);

CALL cpa_add_index_if_missing(
    'audit_log',
    'idx_audit_log_data',
    'CREATE INDEX idx_audit_log_data ON audit_log (data_hora)'
);

CALL cpa_add_index_if_missing(
    'audit_log',
    'idx_audit_log_usuario',
    'CREATE INDEX idx_audit_log_usuario ON audit_log (usuario)'
);

DROP PROCEDURE IF EXISTS cpa_add_index_if_missing;
