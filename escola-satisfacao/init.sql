CREATE DATABASE IF NOT EXISTS escola_db;

USE escola_db;

-- Tabela de administradores
CREATE TABLE IF NOT EXISTS administradores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    cargo VARCHAR(50) NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de alunos
CREATE TABLE IF NOT EXISTS alunos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Senha: admin123 (hash BCrypt)
INSERT INTO administradores (nome, email, senha_hash, cargo, telefone)
VALUES (
    'Administrador Principal',
    'admin@escola.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq5XkYI1Jq3JY1VzR3Jf6QYQ5JZ5W',
    'Diretor',
    '(11) 99999-9999'
);

CREATE TABLE avaliacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT,
    satisfacao INT,
    satisfacao_infra INT,
    comentario TEXT,
    data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);