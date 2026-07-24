# Arquitetura

## Visao geral

A aplicacao e um monolito Spring Boot com renderizacao server-side via Thymeleaf. A estrutura atual foi preservada e endurecida incrementalmente, sem reescrever o sistema.

## Camadas

- Controller: recebe requisicoes web, prepara modelos de tela e delega regras compartilhadas.
- Service: concentra regras de escopo institucional, versionamento, auditoria e privacidade.
- Repository: acesso a dados via Spring Data JPA.
- Model: entidades JPA e enums de dominio.
- Templates: telas Thymeleaf renderizadas no servidor.
- Static: camada visual compartilhada em `static/css/cpa-ui.css` e comportamentos em `static/js/cpa-ui.js`.

## Servicos adicionados

- `InstituicaoScopeService`: valida isolamento multi-instituicao.
- `QuestionnaireVersioningService`: cria versoes imutaveis de questionarios publicados.
- `ResultadoPrivacidadeService`: centraliza minimo de amostra e classificacao de resultados.
- `AuditService`: registra eventos administrativos sem armazenar senhas, tokens ou segredos.

## Frontend

- Layout responsivo com menu mobile.
- Tabelas administrativas adaptadas para leitura em celulares.
- Formularios com labels, estados de foco e mensagens de erro/sucesso padronizadas.
- Tela de resposta com indicador de progresso.
- CSS proprio para reduzir repeticao visual e diminuir dependencia de estilos inline.

## Decisoes importantes

- Questionarios modelo continuam editaveis.
- Ao criar avaliacao, uma copia congelada do questionario e criada.
- Avaliacoes antigas apontam para a versao congelada, mantendo historico consistente.
- Avaliacoes anonimas nao gravam aluno nem username no conteudo da resposta.
- A participacao registra que o aluno respondeu, sem conter as respostas.
- Exclusao fisica de plano de acao foi evitada; usa-se cancelamento logico.
