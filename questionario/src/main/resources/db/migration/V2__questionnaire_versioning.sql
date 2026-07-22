ALTER TABLE questionnaire
    ADD COLUMN questionario_base_id BIGINT NULL,
    ADD COLUMN versao INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado BIT NOT NULL DEFAULT 0;

ALTER TABLE questionnaire
    ADD CONSTRAINT fk_questionnaire_base
        FOREIGN KEY (questionario_base_id) REFERENCES questionnaire (id);

CREATE INDEX idx_questionnaire_base ON questionnaire (questionario_base_id);
