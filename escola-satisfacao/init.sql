-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS PesquisaSenai;

-- Selecionar o banco de dados
USE PesquisaSenai;

-- Criar tabela 'Aluno'
CREATE TABLE IF NOT EXISTS Aluno (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    turma VARCHAR(255) NOT NULL,
    matricula VARCHAR(255) NOT NULL UNIQUE
);

-- Criar tabela 'Usuario'
CREATE TABLE IF NOT EXISTS Usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    cargo VARCHAR(255) NOT NULL
);

-- Criar tabela 'Pesquisa' para armazenar as respostas da pesquisa
CREATE TABLE IF NOT EXISTS Pesquisa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT NOT NULL,
    usuario_id INT NOT NULL,
    resposta_1 INT, -- Resposta numérica (1 a 5)
    resposta_2 INT, -- Resposta numérica (1 a 5)
    resposta_3 INT, -- Resposta numérica (1 a 5)
    resposta_4 INT, -- Resposta numérica (1 a 5)
    resposta_5 INT, -- Resposta numérica (1 a 5)
    resposta_6 TEXT, -- Resposta textual
    resposta_7 TEXT, -- Resposta textual
    FOREIGN KEY (aluno_id) REFERENCES Aluno(id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);
