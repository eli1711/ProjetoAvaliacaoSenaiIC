# Plano de Melhorias

## Concluido nesta rodada

- Remocao de senha fixa do datasource.
- Dados de demonstracao desativados por padrao.
- Multi-instituicao com escopo nos principais fluxos.
- Permissoes centralizadas por perfil.
- Protecao de respostas anonimas.
- Controle de resposta duplicada.
- Migrations Flyway.
- Versionamento imutavel de questionarios publicados.
- Limite minimo de amostra para resultados.
- Bloqueio de exportacoes CSV abaixo do minimo de amostra.
- Politica configuravel de senha.
- Bloqueio temporario apos tentativas invalidas de login.
- Troca obrigatoria de senhas temporarias.
- Plano de acao institucional.
- Auditoria administrativa.
- Tratamento global de excecoes.
- Docker Compose com variaveis de ambiente.
- Inicializacao Docker com validacao do usuario MySQL da aplicacao no health check do banco.
- Actuator health check.
- Migrations incrementais idempotentes para evitar falhas quando a baseline ja contem estruturas recentes.
- Base visual responsiva com CSS/JS compartilhado.
- Reformulacao de login, painel, avaliacoes, resposta, analise, usuarios, alunos e planos de acao.
- Documentacao inicial.

## Critico

- Criar recuperacao de senha segura.
- Revisar sanitizacao de campos textuais abertos.

## Alta

- Criar DTOs para reduzir exposicao de entidades.
- Criar testes de controller e autorizacao.
- Criar importacao Excel alem de CSV.
- Criar relatorios PDF/Excel.
- Implementar convites com token individual e expiracao.

## Media

- Criar dashboards com indicadores historicos.
- Criar comparacao entre periodos.
- Criar modulo de cursos e periodos letivos separado de turmas.
- Adicionar filtros avancados em planos de acao e auditoria.
- Adicionar exportacao de dados do titular.

## Baixa

- Migrar os templates secundarios restantes para a base visual compartilhada.
- Substituir Tailwind CDN por build frontend versionado.
- Adicionar i18n.
