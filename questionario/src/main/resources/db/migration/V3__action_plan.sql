DROP PROCEDURE IF EXISTS cpa_add_column_if_missing;
DROP PROCEDURE IF EXISTS cpa_add_constraint_if_missing;
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

CREATE PROCEDURE cpa_add_constraint_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND CONSTRAINT_NAME = p_constraint_name
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

CREATE TABLE IF NOT EXISTS instituicao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    identificador_institucional VARCHAR(255) NOT NULL,
    cnpj VARCHAR(255),
    endereco TEXT,
    contatos TEXT,
    responsavel_institucional VARCHAR(255),
    ativo BIT NOT NULL,
    logo_url VARCHAR(255),
    configuracoes TEXT,
    periodo_letivo_atual VARCHAR(255),
    dados_relatorio TEXT,
    PRIMARY KEY (id),
    CONSTRAINT uk_instituicao_identificador UNIQUE (identificador_institucional)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO instituicao (
    nome,
    identificador_institucional,
    ativo,
    dados_relatorio
)
SELECT
    'Instituicao legado',
    'LEGADO',
    1,
    'Instituicao criada automaticamente para dados anteriores ao suporte multi-instituicao.'
WHERE NOT EXISTS (
    SELECT 1
    FROM instituicao
    WHERE identificador_institucional = 'LEGADO'
);

SET @cpa_default_instituicao_id = (
    SELECT id
    FROM instituicao
    WHERE identificador_institucional = 'LEGADO'
    LIMIT 1
);

CALL cpa_add_column_if_missing('turma', 'instituicao_id', 'ALTER TABLE turma ADD COLUMN instituicao_id BIGINT NULL');
CALL cpa_add_column_if_missing('users', 'instituicao_id', 'ALTER TABLE users ADD COLUMN instituicao_id BIGINT NULL');
CALL cpa_add_column_if_missing('aluno', 'instituicao_id', 'ALTER TABLE aluno ADD COLUMN instituicao_id BIGINT NULL');
CALL cpa_add_column_if_missing('questionnaire', 'instituicao_id', 'ALTER TABLE questionnaire ADD COLUMN instituicao_id BIGINT NULL');
CALL cpa_add_column_if_missing('avaliacao_aplicada', 'instituicao_id', 'ALTER TABLE avaliacao_aplicada ADD COLUMN instituicao_id BIGINT NULL');
CALL cpa_add_column_if_missing('avaliacao_aplicada', 'anonima', 'ALTER TABLE avaliacao_aplicada ADD COLUMN anonima BIT NULL');
CALL cpa_add_column_if_missing('resposta_aluno', 'anonima', 'ALTER TABLE resposta_aluno ADD COLUMN anonima BIT NULL');
CALL cpa_add_column_if_missing('resposta_aluno', 'codigo_anonimo', 'ALTER TABLE resposta_aluno ADD COLUMN codigo_anonimo VARCHAR(64) NULL');

UPDATE turma t
LEFT JOIN instituicao i ON t.instituicao_id = i.id
SET t.instituicao_id = @cpa_default_instituicao_id
WHERE t.instituicao_id IS NULL OR i.id IS NULL;

UPDATE users u
LEFT JOIN turma t ON u.turma_id = t.id
LEFT JOIN instituicao i ON COALESCE(t.instituicao_id, u.instituicao_id) = i.id
SET u.instituicao_id = COALESCE(t.instituicao_id, @cpa_default_instituicao_id)
WHERE u.instituicao_id IS NULL OR i.id IS NULL;

UPDATE aluno a
LEFT JOIN turma t ON a.turma_id = t.id
LEFT JOIN instituicao i ON COALESCE(t.instituicao_id, a.instituicao_id) = i.id
SET a.instituicao_id = COALESCE(t.instituicao_id, @cpa_default_instituicao_id)
WHERE a.instituicao_id IS NULL OR i.id IS NULL;

UPDATE questionnaire q
LEFT JOIN instituicao i ON q.instituicao_id = i.id
SET q.instituicao_id = @cpa_default_instituicao_id
WHERE q.instituicao_id IS NULL OR i.id IS NULL;

UPDATE avaliacao_aplicada aa
LEFT JOIN turma t ON aa.turma_id = t.id
LEFT JOIN instituicao i ON COALESCE(t.instituicao_id, aa.instituicao_id) = i.id
SET aa.instituicao_id = COALESCE(t.instituicao_id, @cpa_default_instituicao_id)
WHERE aa.instituicao_id IS NULL OR i.id IS NULL;

UPDATE avaliacao_aplicada
SET anonima = 0
WHERE anonima IS NULL;

UPDATE resposta_aluno
SET anonima = 0
WHERE anonima IS NULL;

ALTER TABLE resposta_aluno MODIFY COLUMN aluno_id BIGINT NULL;
ALTER TABLE answer MODIFY COLUMN user_username VARCHAR(255) NULL;

CALL cpa_add_index_if_missing('turma', 'idx_turma_instituicao', 'CREATE INDEX idx_turma_instituicao ON turma (instituicao_id)');
CALL cpa_add_index_if_missing('users', 'idx_users_instituicao', 'CREATE INDEX idx_users_instituicao ON users (instituicao_id)');
CALL cpa_add_index_if_missing('aluno', 'idx_aluno_instituicao', 'CREATE INDEX idx_aluno_instituicao ON aluno (instituicao_id)');
CALL cpa_add_index_if_missing('questionnaire', 'idx_questionnaire_instituicao', 'CREATE INDEX idx_questionnaire_instituicao ON questionnaire (instituicao_id)');
CALL cpa_add_index_if_missing('avaliacao_aplicada', 'idx_avaliacao_instituicao', 'CREATE INDEX idx_avaliacao_instituicao ON avaliacao_aplicada (instituicao_id)');
CALL cpa_add_index_if_missing('avaliacao_aplicada', 'idx_avaliacao_turma_status', 'CREATE INDEX idx_avaliacao_turma_status ON avaliacao_aplicada (turma_id, status)');
CALL cpa_add_index_if_missing('resposta_aluno', 'idx_resposta_avaliacao', 'CREATE INDEX idx_resposta_avaliacao ON resposta_aluno (avaliacao_aplicada_id)');
CALL cpa_add_index_if_missing('answer', 'idx_answer_resposta', 'CREATE INDEX idx_answer_resposta ON answer (resposta_aluno_id)');
CALL cpa_add_index_if_missing('answer', 'idx_answer_question', 'CREATE INDEX idx_answer_question ON answer (question_id)');

CALL cpa_add_constraint_if_missing('turma', 'fk_turma_instituicao', 'ALTER TABLE turma ADD CONSTRAINT fk_turma_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)');
CALL cpa_add_constraint_if_missing('users', 'fk_users_instituicao', 'ALTER TABLE users ADD CONSTRAINT fk_users_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)');
CALL cpa_add_constraint_if_missing('aluno', 'fk_aluno_instituicao', 'ALTER TABLE aluno ADD CONSTRAINT fk_aluno_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)');
CALL cpa_add_constraint_if_missing('questionnaire', 'fk_questionnaire_instituicao', 'ALTER TABLE questionnaire ADD CONSTRAINT fk_questionnaire_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)');
CALL cpa_add_constraint_if_missing('avaliacao_aplicada', 'fk_avaliacao_instituicao', 'ALTER TABLE avaliacao_aplicada ADD CONSTRAINT fk_avaliacao_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)');

CREATE TABLE IF NOT EXISTS participacao_avaliacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id BIGINT NOT NULL,
    avaliacao_aplicada_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    data_criacao DATETIME(6),
    data_conclusao DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_participacao_aluno_avaliacao UNIQUE (aluno_id, avaliacao_aplicada_id),
    CONSTRAINT fk_participacao_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_participacao_avaliacao FOREIGN KEY (avaliacao_aplicada_id) REFERENCES avaliacao_aplicada (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO participacao_avaliacao (
    aluno_id,
    avaliacao_aplicada_id,
    status,
    data_criacao,
    data_conclusao
)
SELECT
    aluno_id,
    avaliacao_aplicada_id,
    COALESCE(status, 'RESPONDIDO'),
    COALESCE(data_resposta, NOW()),
    data_resposta
FROM resposta_aluno
WHERE aluno_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS plano_acao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instituicao_id BIGINT NOT NULL,
    avaliacao_aplicada_id BIGINT,
    problema_identificado TEXT NOT NULL,
    indicador_relacionado VARCHAR(255),
    acao_proposta TEXT NOT NULL,
    responsavel VARCHAR(255) NOT NULL,
    setor_responsavel VARCHAR(255),
    data_inicio DATE,
    prazo DATE NOT NULL,
    prioridade VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    percentual_conclusao INTEGER NOT NULL,
    evidencias TEXT,
    observacoes TEXT,
    resultado_esperado TEXT,
    resultado_alcancado TEXT,
    criado_em DATETIME(6),
    atualizado_em DATETIME(6),
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_plano_acao_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT fk_plano_acao_avaliacao FOREIGN KEY (avaliacao_aplicada_id) REFERENCES avaliacao_aplicada (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL cpa_add_index_if_missing('plano_acao', 'idx_plano_acao_instituicao', 'CREATE INDEX idx_plano_acao_instituicao ON plano_acao (instituicao_id)');
CALL cpa_add_index_if_missing('plano_acao', 'idx_plano_acao_avaliacao', 'CREATE INDEX idx_plano_acao_avaliacao ON plano_acao (avaliacao_aplicada_id)');
CALL cpa_add_index_if_missing('plano_acao', 'idx_plano_acao_status', 'CREATE INDEX idx_plano_acao_status ON plano_acao (status)');
CALL cpa_add_index_if_missing('plano_acao', 'idx_plano_acao_prazo', 'CREATE INDEX idx_plano_acao_prazo ON plano_acao (prazo)');

DROP PROCEDURE IF EXISTS cpa_add_column_if_missing;
DROP PROCEDURE IF EXISTS cpa_add_constraint_if_missing;
DROP PROCEDURE IF EXISTS cpa_add_index_if_missing;
