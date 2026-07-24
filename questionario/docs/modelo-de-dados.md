# Modelo de Dados

## Entidades principais

- `Instituicao`: dados institucionais, status, contatos, logo e configuracoes.
- `User`: usuario autenticavel, perfil, status, instituicao e controles de seguranca de login.
- `Aluno`: participante academico vinculado a usuario, turma e instituicao.
- `Turma`: curso, semestre, ano e instituicao.
- `Questionnaire`: questionario modelo ou versao congelada.
- `Question`: pergunta vinculada ao questionario.
- `AvaliacaoAplicada`: aplicacao de uma versao de questionario para uma turma.
- `ParticipacaoAvaliacao`: controle de participacao do aluno na avaliacao.
- `RespostaAluno`: envelope de resposta; anonimo por padrao.
- `Answer`: resposta por pergunta.
- `PlanoAcao`: acompanhamento de melhoria institucional.
- `AuditLog`: trilha de auditoria administrativa.

## Regras estruturais

- Todos os dados administrativos devem ter `instituicao_id`.
- `ParticipacaoAvaliacao` possui unicidade por aluno e avaliacao.
- `Questionnaire.bloqueado=true` indica versao historica imutavel.
- `Questionnaire.questionarioBaseId` aponta para o modelo de origem.
- `RespostaAluno.aluno_id` fica nulo quando a avaliacao e anonima.
- `Answer.userUsername` fica nulo quando a avaliacao e anonima.
- `User.failedLoginAttempts` registra falhas consecutivas de login.
- `User.lockedUntil` bloqueia temporariamente a conta quando houver muitas falhas.
- `User.mustChangePassword` obriga troca de credencial temporaria no primeiro acesso.

## Migrations

- `V1__baseline_schema.sql`: schema consolidado para novos ambientes.
- `V2__questionnaire_versioning.sql`: versionamento de questionarios.
- `V3__action_plan.sql`: planos de acao.
- `V4__audit_log.sql`: auditoria.
- `V5__login_security.sql`: politica operacional de login e senha.
