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

CREATE TABLE IF NOT EXISTS turma (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255),
    curso VARCHAR(255),
    semestre INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    instituicao_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_turma_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    ra VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    turma_id BIGINT,
    instituicao_id BIGINT,
    PRIMARY KEY (username),
    CONSTRAINT uk_users_ra UNIQUE (ra),
    CONSTRAINT fk_users_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_users_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS aluno (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    ra VARCHAR(255) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    user_username VARCHAR(255) NOT NULL,
    turma_id BIGINT,
    instituicao_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_aluno_ra UNIQUE (ra),
    CONSTRAINT uk_aluno_cpf UNIQUE (cpf),
    CONSTRAINT fk_aluno_user FOREIGN KEY (user_username) REFERENCES users (username),
    CONSTRAINT fk_aluno_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_aluno_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS questionnaire (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    description TEXT,
    semester INTEGER NOT NULL,
    `year` INTEGER NOT NULL,
    status VARCHAR(255),
    instituicao_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_questionnaire_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    text VARCHAR(255),
    type VARCHAR(255),
    score INTEGER,
    option1label VARCHAR(255),
    option2label VARCHAR(255),
    option3label VARCHAR(255),
    option4label VARCHAR(255),
    option5label VARCHAR(255),
    questionnaire_id BIGINT,
    item_avaliacao VARCHAR(255) NOT NULL,
    grau_importancia_modelo VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaire (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS avaliacao_aplicada (
    id BIGINT NOT NULL AUTO_INCREMENT,
    turma_id BIGINT NOT NULL,
    questionnaire_id BIGINT NOT NULL,
    instituicao_id BIGINT,
    data_inicio DATETIME(6),
    data_fim DATETIME(6),
    status VARCHAR(255),
    anonima BIT,
    PRIMARY KEY (id),
    CONSTRAINT fk_avaliacao_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_avaliacao_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaire (id),
    CONSTRAINT fk_avaliacao_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resposta_aluno (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id BIGINT,
    avaliacao_aplicada_id BIGINT NOT NULL,
    data_resposta DATETIME(6),
    status VARCHAR(255),
    anonima BIT,
    codigo_anonimo VARCHAR(64),
    PRIMARY KEY (id),
    CONSTRAINT fk_resposta_aluno_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_resposta_aluno_avaliacao FOREIGN KEY (avaliacao_aplicada_id) REFERENCES avaliacao_aplicada (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    response VARCHAR(255) NOT NULL,
    question_id BIGINT NOT NULL,
    user_username VARCHAR(255),
    resposta_aluno_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_answer_resposta_aluno FOREIGN KEY (resposta_aluno_id) REFERENCES resposta_aluno (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS importancia_questao_resposta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    grau_importancia VARCHAR(255) NOT NULL,
    resposta_aluno_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_importancia_questao_resposta FOREIGN KEY (resposta_aluno_id) REFERENCES resposta_aluno (id),
    CONSTRAINT fk_importancia_questao_question FOREIGN KEY (question_id) REFERENCES question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS importancia_item_resposta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    grau_importancia VARCHAR(255) NOT NULL,
    item VARCHAR(255) NOT NULL,
    resposta_aluno_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_importancia_item_resposta FOREIGN KEY (resposta_aluno_id) REFERENCES resposta_aluno (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE INDEX idx_turma_instituicao ON turma (instituicao_id);
CREATE INDEX idx_users_instituicao ON users (instituicao_id);
CREATE INDEX idx_aluno_instituicao ON aluno (instituicao_id);
CREATE INDEX idx_questionnaire_instituicao ON questionnaire (instituicao_id);
CREATE INDEX idx_avaliacao_instituicao ON avaliacao_aplicada (instituicao_id);
CREATE INDEX idx_avaliacao_turma_status ON avaliacao_aplicada (turma_id, status);
CREATE INDEX idx_resposta_avaliacao ON resposta_aluno (avaliacao_aplicada_id);
CREATE INDEX idx_answer_resposta ON answer (resposta_aluno_id);
CREATE INDEX idx_answer_question ON answer (question_id);
