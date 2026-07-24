# Sistema de Avaliacao CPA

Sistema web em Java/Spring Boot para apoiar a Comissao Propria de Avaliacao (CPA) em processos de autoavaliacao institucional. A aplicacao permite cadastrar instituicoes, usuarios, turmas, questionarios, avaliacoes aplicadas, respostas anonimas, indicadores, planos de acao e auditoria administrativa.

## Objetivo

Apoiar instituicoes de ensino na coleta, consolidacao e acompanhamento dos resultados da CPA, produzindo evidencias para melhoria institucional e para processos de avaliacao ligados ao SINAES, MEC e INEP.

## Tecnologias

- Java 21
- Spring Boot 3.5.7
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf
- CSS e JavaScript proprios para interface responsiva
- MySQL 8
- H2 para testes
- Flyway
- Spring Boot Actuator
- Docker e Docker Compose
- Maven

## Arquitetura

O projeto segue uma arquitetura MVC simples:

- `model`: entidades JPA e enums de dominio.
- `repository`: interfaces Spring Data JPA.
- `service`: regras de negocio compartilhadas, escopo institucional, versionamento, auditoria e privacidade.
- `controller`: fluxos web com Thymeleaf.
- `templates`: telas HTML.
- `db/migration`: migrations Flyway.

## Funcionalidades principais

- Multi-instituicao com isolamento de dados por instituicao.
- Controle de perfis e permissoes no backend.
- Painel dev em `/dev` para cadastro de instituicoes.
- Cadastro institucional de usuarios, turmas e alunos dentro do escopo da propria instituicao.
- Interface responsiva com menu mobile, tabelas adaptaveis e formularios acessiveis.
- Politica configuravel de senha e troca obrigatoria para credenciais temporarias.
- Bloqueio temporario apos tentativas invalidas de login.
- Questionarios reutilizaveis.
- Versionamento imutavel de questionarios ao publicar uma avaliacao.
- Avaliacoes aplicadas por turma.
- Respostas anonimas com separacao entre participacao e conteudo da resposta.
- Bloqueio de resposta duplicada por aluno/avaliacao.
- Indicadores com limite minimo de amostra para proteger grupos pequenos.
- Exportacoes CSV bloqueadas quando o grupo esta abaixo do minimo LGPD.
- Plano de acao vinculado a avaliacao ou instituicao.
- Auditoria de login e operacoes administrativas.
- Tratamento global de erros com identificador de rastreamento.
- Actuator para health check.
- Docker Compose com variaveis de ambiente.

## Perfis

- `ROLE_SUPER_ADMIN`
- `ROLE_ADMIN`
- `ROLE_GESTOR`
- `ROLE_COORDENADOR`
- `ROLE_PROFESSOR`
- `ROLE_COLABORADOR`
- `ROLE_ALUNO`
- `ROLE_AVALIADOR_EXTERNO`
- `ROLE_USER`

As permissoes sao derivadas em `PerfilSistema` e aplicadas no backend por Spring Security.

`ROLE_SUPER_ADMIN` e o perfil tecnico do dev. Ele acessa `/dev`, cadastra instituicoes e pode ver dados globais para suporte. `ROLE_ADMIN` administra somente usuarios, turmas, questionarios, avaliacoes, resultados e planos da instituicao vinculada ao proprio usuario.

Para criar ou atualizar o usuario dev automaticamente no boot, ative:

```properties
APP_DEV_USER_ENABLED=true
APP_DEV_USERNAME=dev
APP_DEV_PASSWORD=<senha-forte>
APP_DEV_EMAIL=dev@sistema.local
APP_DEV_NAME=Desenvolvedor do Sistema
```

## Como executar localmente

Requisitos:

- Java 21
- Maven
- MySQL 8 em execucao

Configure as variaveis necessarias ou ajuste `application.properties` para desenvolvimento.

```bash
mvn spring-boot:run
```

Por padrao, a aplicacao sobe em:

```text
http://localhost:8080
```

## Como executar com Docker

Crie um arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Altere as senhas antes de subir:

```bash
docker compose up --build
```

Aplicacao:

```text
http://localhost:8098
```

Health check:

```text
http://localhost:8098/actuator/health
```

O Compose usa o health check do MySQL para validar o banco e garantir que o usuario configurado em `MYSQL_USER` tenha permissao para acessar o MySQL pela rede interna do Docker antes da aplicacao iniciar. Isso evita falhas como:

```text
Access denied for user 'cpa_user'@'172.x.x.x'
```

Se o volume do MySQL ja foi criado com outra senha de root, mantenha no `.env` a senha original usada na primeira inicializacao. Caso seja um ambiente descartavel de desenvolvimento e os dados possam ser perdidos, recrie o volume antes de subir novamente.

Quando o `db` ficar `unhealthy` com mensagem dizendo que `MYSQL_ROOT_PASSWORD` nao autentica no volume atual, o banco persistido foi inicializado com outra senha. Nesse caso, ajuste o `.env` para a senha original ou recrie o volume somente se os dados forem descartaveis.

## Variaveis principais

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `APP_PORT`
- `SPRING_PROFILES_ACTIVE`
- `SPRING_FLYWAY_ENABLED`
- `APP_DEMO_DATA_ENABLED`
- `APP_DEV_USER_ENABLED`
- `APP_DEV_USERNAME`
- `APP_DEV_PASSWORD`
- `APP_DEV_EMAIL`
- `APP_DEV_NAME`
- `APP_PRIVACIDADE_MINIMO_RESPOSTAS_GRUPO`
- `APP_PRIVACIDADE_PERMITE_RELATORIO_INDIVIDUAL`
- `APP_SECURITY_LOGIN_MAX_FAILED_ATTEMPTS`
- `APP_SECURITY_LOGIN_LOCK_MINUTES`
- `APP_SECURITY_PASSWORD_MIN_LENGTH`
- `APP_SECURITY_PASSWORD_MAX_LENGTH`
- `APP_SECURITY_PASSWORD_MIN_CATEGORIES`
- `APP_EMAIL_CONVITES_ENABLED`
- `APP_EMAIL_BASE_URL`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

## Migrations

Em producao, o profile `prod` usa:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

As migrations ficam em:

```text
src/main/resources/db/migration
```

Se o Docker parar com:

```text
Detected failed migration to version 3 (action plan)
```

o banco ficou com uma tentativa falhada registrada no historico do Flyway. Depois de atualizar o projeto, remova somente o registro falhado e suba novamente:

```bash
docker compose up -d db
docker compose exec db sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "DELETE FROM flyway_schema_history WHERE success = 0;"'
docker compose up --build
```

Esse comando nao apaga tabelas nem dados da aplicacao; ele apenas permite que o Flyway reexecute a migration corrigida.

## Testes

```bash
mvn test
```

A suite atual cobre inicializacao do contexto, privacidade de indicadores, politica de senha e regras do plano de acao.

## Fluxo de avaliacao

1. Entrar como dev e cadastrar a instituicao em `/dev/instituicoes`.
2. Cadastrar usuarios, alunos e turmas vinculados a essa instituicao.
3. Criar questionario modelo.
4. Publicar avaliacao para uma turma.
5. O sistema cria uma versao congelada do questionario.
6. Alunos respondem uma unica vez.
7. Em avaliacao anonima, a resposta nao guarda aluno nem username.
8. A participacao guarda apenas que o aluno concluiu.
9. Resultados respeitam o limite minimo de amostra.
10. Exportacoes tambem respeitam o limite minimo de amostra.
11. Planos de acao acompanham melhorias decorrentes dos indicadores.

## Politica de anonimato

Avaliacoes sao anonimas por padrao. A identidade do aluno fica no controle de participacao, enquanto o conteudo da resposta fica em `RespostaAluno` sem vinculo direto com aluno ou username.

Resultados segmentados sao bloqueados quando o grupo possui menos respostas que `APP_PRIVACIDADE_MINIMO_RESPOSTAS_GRUPO`, com padrao 5.

Exportacoes CSV de resultados seguem a mesma regra e retornam erro controlado quando houver risco de identificacao indireta.

## Auditoria

Eventos administrativos sao registrados em `audit_log`, incluindo:

- login com sucesso
- falha de login
- bloqueio temporario de login
- alteracao de senha pelo proprio usuario
- criacao/atualizacao/remocao de avaliacao
- exportacao CSV de resultados
- criacao/atualizacao/cancelamento de plano de acao

Detalhes passam por mascaramento simples de campos como senha, token e secret.

## Documentacao adicional

- [Arquitetura](docs/arquitetura.md)
- [Seguranca e LGPD](docs/seguranca-lgpd.md)
- [Modelo de dados](docs/modelo-de-dados.md)
- [Plano de melhorias](docs/plano-de-melhorias.md)
