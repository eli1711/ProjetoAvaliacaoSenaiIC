# Seguranca e LGPD

## Implementado

- Senhas com BCrypt via Spring Security.
- Politica configuravel de senha.
- Bloqueio temporario apos tentativas invalidas de login.
- Senhas temporarias de importacao com troca obrigatoria no primeiro acesso.
- Usuarios inativos nao autenticam.
- Autorizacao por permissoes no backend.
- CSRF ativo para formularios.
- Rotas administrativas protegidas por permissao.
- Dados segregados por instituicao.
- Cadastro de instituicoes restrito ao painel dev (`ROLE_SUPER_ADMIN`).
- Usuario comum sem instituicao vinculada nao recebe acesso global por fallback.
- Dados sensiveis removidos do `application.properties`.
- Demo data desativado por padrao e condicionado a senhas via variavel.
- Respostas anonimas sem vinculo direto com aluno.
- Controle de participacao separado do conteudo da resposta.
- Bloqueio de resposta duplicada.
- Limite minimo de amostra para resultados segmentados.
- Bloqueio de exportacoes CSV abaixo do minimo de amostra.
- Relatorio individual desativado por padrao.
- Auditoria administrativa com mascaramento basico de segredos.
- Tratamento global de erros com identificador de rastreamento.

## Variaveis de privacidade

- `APP_PRIVACIDADE_MINIMO_RESPOSTAS_GRUPO`: padrao 5.
- `APP_PRIVACIDADE_PERMITE_RELATORIO_INDIVIDUAL`: padrao false.

## Variaveis de seguranca

- `APP_SECURITY_LOGIN_MAX_FAILED_ATTEMPTS`: padrao 5.
- `APP_SECURITY_LOGIN_LOCK_MINUTES`: padrao 15.
- `APP_SECURITY_PASSWORD_MIN_LENGTH`: padrao 10.
- `APP_SECURITY_PASSWORD_MAX_LENGTH`: padrao 128.
- `APP_SECURITY_PASSWORD_MIN_CATEGORIES`: padrao 3.
- `APP_DEV_USER_ENABLED`: cria/atualiza o usuario tecnico dev quando ativo.
- `APP_DEV_PASSWORD`: senha forte obrigatoria quando o usuario dev estiver ativo.

## Cuidados pendentes

- Recuperacao de senha.
- Confirmacao de e-mail.
- Refresh token, caso seja criada API JWT.
- Rate limiting por IP em login e endpoints sensiveis.
- Politica formal de retencao e anonimização.
- Sanitizacao HTML mais robusta para comentarios abertos.
- Controle de upload de evidencias como arquivo.
