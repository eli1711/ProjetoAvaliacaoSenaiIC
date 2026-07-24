package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.*;
import br.com.cpa.questionario.repository.*;
import br.com.cpa.questionario.service.AvaliacaoEmailService;
import br.com.cpa.questionario.service.AuditService;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import br.com.cpa.questionario.service.QuestionnaireVersioningService;
import br.com.cpa.questionario.service.ResultadoPrivacidadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoAplicadaController {

    private static final Logger log = LoggerFactory.getLogger(AvaliacaoAplicadaController.class);

    private final AvaliacaoAplicadaRepository avaliacaoAplicadaRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final TurmaRepository turmaRepository;
    private final RespostaAlunoRepository respostaAlunoRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AvaliacaoEmailService avaliacaoEmailService;
    private final AlunoRepository alunoRepository;
    private final InstituicaoScopeService instituicaoScopeService;
    private final ParticipacaoAvaliacaoRepository participacaoAvaliacaoRepository;
    private final QuestionnaireVersioningService questionnaireVersioningService;
    private final AuditService auditService;
    private final ResultadoPrivacidadeService resultadoPrivacidadeService;

    // existente (por ITEM)
    private final ImportanciaItemRespostaRepository importanciaItemRespostaRepository;

    // NOVO (por QUESTÃO)
    private final ImportanciaQuestaoRespostaRepository importanciaQuestaoRespostaRepository;

    public AvaliacaoAplicadaController(AvaliacaoAplicadaRepository avaliacaoAplicadaRepository,
                                       QuestionnaireRepository questionnaireRepository,
                                       QuestionRepository questionRepository,
                                       TurmaRepository turmaRepository,
                                       RespostaAlunoRepository respostaAlunoRepository,
                                       AnswerRepository answerRepository,
                                       UserRepository userRepository,
                                       AvaliacaoEmailService avaliacaoEmailService,
                                       AlunoRepository alunoRepository,
                                       InstituicaoScopeService instituicaoScopeService,
                                       ParticipacaoAvaliacaoRepository participacaoAvaliacaoRepository,
                                       QuestionnaireVersioningService questionnaireVersioningService,
                                       AuditService auditService,
                                       ResultadoPrivacidadeService resultadoPrivacidadeService,
                                       ImportanciaItemRespostaRepository importanciaItemRespostaRepository,
                                       ImportanciaQuestaoRespostaRepository importanciaQuestaoRespostaRepository) {
        this.avaliacaoAplicadaRepository = avaliacaoAplicadaRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.turmaRepository = turmaRepository;
        this.respostaAlunoRepository = respostaAlunoRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.avaliacaoEmailService = avaliacaoEmailService;
        this.alunoRepository = alunoRepository;
        this.instituicaoScopeService = instituicaoScopeService;
        this.participacaoAvaliacaoRepository = participacaoAvaliacaoRepository;
        this.questionnaireVersioningService = questionnaireVersioningService;
        this.auditService = auditService;
        this.resultadoPrivacidadeService = resultadoPrivacidadeService;
        this.importanciaItemRespostaRepository = importanciaItemRespostaRepository;
        this.importanciaQuestaoRespostaRepository = importanciaQuestaoRespostaRepository;
    }

    // ============================================================
    // LISTAGEM (Admin / Gestor)
    // ============================================================
    @GetMapping
    public String list(Model model) {
        List<AvaliacaoAplicada> avaliacoes = instituicaoScopeService.isSuperAdmin()
                ? avaliacaoAplicadaRepository.findAll()
                : instituicaoScopeService.getInstituicaoAtual()
                        .map(instituicao -> avaliacaoAplicadaRepository.findByInstituicaoId(instituicao.getId()))
                        .orElseGet(List::of);

        // Atualiza status automaticamente (ABERTA -> ENCERRADA) se dataFim já passou
        LocalDateTime agora = LocalDateTime.now();
        for (AvaliacaoAplicada a : avaliacoes) {
            atualizarStatusAutomaticamente(a, agora);
        }
        avaliacaoAplicadaRepository.saveAll(avaliacoes);

        model.addAttribute("avaliacoes", avaliacoes);
        return "avaliacao/list";
    }

    private void atualizarStatusAutomaticamente(AvaliacaoAplicada a, LocalDateTime agora) {
        if (a.getDataFim() != null
                && agora.isAfter(a.getDataFim())
                && a.getStatus() == StatusAvaliacao.ABERTA) {
            a.setStatus(StatusAvaliacao.ENCERRADA);
        }
    }

    // ============================================================
    // LISTAR AVALIAÇÕES DISPONÍVEIS PARA O ALUNO LOGADO
    // ============================================================
    @GetMapping("/disponiveis")
    public String avaliacoesDisponiveisParaAluno(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Aluno aluno = alunoRepository.findByUserUsername(user.getUsername())
                .orElse(null);
        if (aluno == null) {
            model.addAttribute("error", "Seu usuário não está vinculado a um registro de aluno.");
            model.addAttribute("avaliacoes", List.of());
            return "avaliacao/disponiveis";
        }

        List<AvaliacaoAplicada> disponiveis = List.of();

        if (aluno.getTurma() != null) {
            Instituicao instituicaoAluno = aluno.getInstituicao() != null
                    ? aluno.getInstituicao()
                    : aluno.getTurma().getInstituicao();
            if (instituicaoAluno != null) {
                disponiveis = avaliacaoAplicadaRepository
                        .findByTurmaIdAndStatusAndInstituicaoId(
                                aluno.getTurma().getId(), StatusAvaliacao.ABERTA, instituicaoAluno.getId());
            }

            LocalDateTime agora = LocalDateTime.now();

            disponiveis = disponiveis.stream()
                    // 1) dentro do período
                    .filter(a -> (a.getDataInicio() == null || !agora.isBefore(a.getDataInicio())) &&
                                 (a.getDataFim() == null || !agora.isAfter(a.getDataFim())))
                    // 2) ainda não respondeu
                    .filter(a -> !jaRespondeu(aluno, a))
                    .toList();
        }

        model.addAttribute("avaliacoes", disponiveis);
        model.addAttribute("aluno", aluno);
        return "avaliacao/disponiveis";
    }

    // ============================================================
    // FORM NOVA AVALIAÇÃO APLICADA
    // ============================================================
    @GetMapping("/new")
    public String newAvaliacao(Model model) {
        model.addAttribute("avaliacao", new AvaliacaoAplicada());
        model.addAttribute("turmas", getTurmasPermitidas());
        model.addAttribute("questionnaires", getQuestionariosPermitidos());
        return "avaliacao/edit";
    }

    // ============================================================
    // CRIA AVALIAÇÃO APLICADA + ENVIA E-MAILS
    // ============================================================
    @PostMapping
    @Transactional
    public String createAvaliacao(@RequestParam Long turmaId,
                                  @RequestParam Long questionnaireId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataFim,
                                  RedirectAttributes redirectAttributes) {

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));
        instituicaoScopeService.validarAcesso(turma.getInstituicao());
        instituicaoScopeService.validarAcesso(questionnaire.getInstituicao());
        validarMesmoEscopo(turma, questionnaire);
        Questionnaire questionarioPublicado = questionnaireVersioningService.criarVersaoPublicada(questionnaire);

        AvaliacaoAplicada avaliacao = new AvaliacaoAplicada();
        avaliacao.setTurma(turma);
        avaliacao.setQuestionario(questionarioPublicado);
        avaliacao.setInstituicao(instituicaoScopeService.instituicaoParaNovoRegistro(turma.getInstituicao()));
        avaliacao.setDataInicio(dataInicio);
        avaliacao.setDataFim(dataFim);
        avaliacao.setStatus(StatusAvaliacao.ABERTA);

        avaliacao = avaliacaoAplicadaRepository.save(avaliacao);

        // envia os e-mails para todos os alunos ativos da turma
        int convitesEnviados = avaliacaoEmailService.enviarConvites(avaliacao);
        auditService.registrar(
                "AVALIACAO_CRIADA",
                "AvaliacaoAplicada",
                avaliacao.getId(),
                avaliacao.getInstituicao(),
                "turmaId=" + turma.getId()
                        + "; questionarioVersaoId=" + questionarioPublicado.getId()
                        + "; convitesEnviados=" + convitesEnviados);

        String mensagem = convitesEnviados > 0
                ? "Avaliação criada. Convites enviados: " + convitesEnviados + "."
                : "Avaliação criada. Nenhum e-mail foi enviado porque o envio esta desativado ou nao ha alunos ativos.";
        redirectAttributes.addFlashAttribute("success", mensagem);
        return "redirect:/avaliacoes";
    }

    // ============================================================
    // EDITAR AVALIAÇÃO APLICADA
    // ============================================================
    @GetMapping("/{id}/edit")
    public String editAvaliacao(@PathVariable Long id, Model model) {
        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("turmas", getTurmasPermitidas());
        model.addAttribute("questionnaires", getQuestionariosPermitidos());

        return "avaliacao/edit";
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public String updateAvaliacao(@PathVariable Long id,
                                  @RequestParam Long turmaId,
                                  @RequestParam Long questionnaireId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataFim,
                                  @RequestParam StatusAvaliacao status,
                                  RedirectAttributes redirectAttributes) {

        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));
        instituicaoScopeService.validarAcesso(turma.getInstituicao());
        instituicaoScopeService.validarAcesso(questionnaire.getInstituicao());
        validarMesmoEscopo(turma, questionnaire);
        Questionnaire questionarioPublicado = obterQuestionarioPublicadoParaAtualizacao(avaliacao, questionnaire);

        avaliacao.setTurma(turma);
        avaliacao.setQuestionario(questionarioPublicado);
        avaliacao.setInstituicao(instituicaoScopeService.instituicaoParaNovoRegistro(turma.getInstituicao()));
        avaliacao.setDataInicio(dataInicio);
        avaliacao.setDataFim(dataFim);
        avaliacao.setStatus(status);

        avaliacaoAplicadaRepository.save(avaliacao);
        auditService.registrar(
                "AVALIACAO_ATUALIZADA",
                "AvaliacaoAplicada",
                avaliacao.getId(),
                avaliacao.getInstituicao(),
                "status=" + status + "; turmaId=" + turma.getId() + "; questionarioVersaoId=" + questionarioPublicado.getId());

        redirectAttributes.addFlashAttribute("success", "Avaliação atualizada com sucesso.");
        return "redirect:/avaliacoes";
    }

    // ============================================================
    // APAGAR AVALIAÇÃO APLICADA (GERANDO CSV ANTES)
    // ============================================================
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteAvaliacao(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        try {
            // 1) Gera CSV com as respostas desta avaliação
            String csvPath = exportarRespostasParaCsv(avaliacao);

            // 2) Remove respostas (RespostaAluno -> Answer em cascata) e depois a avaliação
            respostaAlunoRepository.deleteByAvaliacaoAplicada(avaliacao);
            avaliacaoAplicadaRepository.delete(avaliacao);
            auditService.registrar(
                    "AVALIACAO_REMOVIDA_COM_EXPORTACAO",
                    "AvaliacaoAplicada",
                    avaliacao.getId(),
                    avaliacao.getInstituicao(),
                    "csv=" + csvPath);

            String msg = "Avaliação apagada com sucesso. "
                    + "Um arquivo CSV com as respostas foi gerado em: " + csvPath;
            redirectAttributes.addFlashAttribute("success", msg);

        } catch (Exception e) {
            log.warn("Erro ao gerar CSV antes de apagar avaliacao id={}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erro ao gerar o CSV e apagar a avaliação: " + e.getMessage());
        }

        return "redirect:/avaliacoes";
    }

    private String exportarRespostasParaCsv(AvaliacaoAplicada avaliacao) throws Exception {
        Long avaliacaoId = avaliacao.getId();

        List<RespostaAluno> respostasAluno = respostaAlunoRepository.findByAvaliacaoAplicadaId(avaliacaoId);
        resultadoPrivacidadeService.validarExportacaoPermitida(respostasAluno.size());

        StringBuilder sb = new StringBuilder();
        sb.append("avaliacao_id;turma;questionario;aluno_ra;aluno_nome;data_resposta;")
          .append("pergunta_id;pergunta_texto;tipo_pergunta;resposta\n");

        for (RespostaAluno ra : respostasAluno) {
            Aluno aluno = ra.getAluno();
            String turmaNome = avaliacao.getTurma() != null ? avaliacao.getTurma().getNome() : "";
            String questionarioNome = avaliacao.getQuestionario() != null ? avaliacao.getQuestionario().getName() : "";

            String alunoRa = (ra.isAnonima() || aluno == null) ? "ANONIMO" : aluno.getRa();
            String alunoNome = (ra.isAnonima() || aluno == null) ? "ANONIMO" : aluno.getNome();
            String dataRespostaStr = ra.getDataResposta() != null ? ra.getDataResposta().toString() : "";

            List<Answer> answers = ra.getRespostas();
            if (answers == null || answers.isEmpty()) {
                sb.append(avaliacaoId).append(";")
                  .append(escapeCsv(turmaNome)).append(";")
                  .append(escapeCsv(questionarioNome)).append(";")
                  .append(escapeCsv(alunoRa)).append(";")
                  .append(escapeCsv(alunoNome)).append(";")
                  .append(escapeCsv(dataRespostaStr)).append(";")
                  .append(";;;;\n");
                continue;
            }

            for (Answer a : answers) {
                Question q = a.getQuestion();
                String perguntaId = q != null ? String.valueOf(q.getId()) : "";
                String perguntaTexto = q != null ? q.getText() : "";
                String tipoPergunta = q != null && q.getType() != null ? q.getType().name() : "";
                String resposta = a.getResponse() != null ? a.getResponse() : "";

                sb.append(avaliacaoId).append(";")
                  .append(escapeCsv(turmaNome)).append(";")
                  .append(escapeCsv(questionarioNome)).append(";")
                  .append(escapeCsv(alunoRa)).append(";")
                  .append(escapeCsv(alunoNome)).append(";")
                  .append(escapeCsv(dataRespostaStr)).append(";")
                  .append(escapeCsv(perguntaId)).append(";")
                  .append(escapeCsv(perguntaTexto)).append(";")
                  .append(escapeCsv(tipoPergunta)).append(";")
                  .append(escapeCsv(resposta))
                  .append("\n");
            }
        }

        Path dir = Paths.get("exports");
        Files.createDirectories(dir);

        String filename = "avaliacao_" + avaliacaoId + "_respostas.csv";
        Path filePath = dir.resolve(filename);

        Files.writeString(filePath, sb.toString(), StandardCharsets.UTF_8);

        return filePath.toAbsolutePath().toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\r", " ").replace("\n", " ");
        return v.replace(";", ",");
    }

    // ============================================================
    // ALUNO RESPONDE AVALIAÇÃO (GET/POST)
    // ============================================================
    @GetMapping("/{id}/responder")
    public String responder(@PathVariable Long id, Model model) {
        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Aluno aluno = alunoRepository.findByUserUsername(user.getUsername())
                .orElse(null);
        if (aluno == null) {
            model.addAttribute("error", "Seu usuário não está vinculado a um registro de aluno.");
            return "avaliacao/ja_respondida";
        }
        try {
            validarAlunoPodeResponder(aluno, avaliacao);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "avaliacao/ja_respondida";
        }

        if (jaRespondeu(aluno, avaliacao)) {
            model.addAttribute("error", "Você já respondeu esta avaliação.");
            return "avaliacao/ja_respondida";
        }

        Questionnaire q = avaliacao.getQuestionario();
        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("questionnaire", q);
        model.addAttribute("questions", questionRepository.findByQuestionnaireId(q.getId()));
        model.addAttribute("aluno", aluno);

        // Se você quiser usar enums na tela:
        model.addAttribute("grausImportancia", GrauImportancia.values());

        return "avaliacao/responder";
    }

    @PostMapping("/{id}/responder")
    @Transactional
    public String salvarRespostas(@PathVariable Long id,
                                  @RequestParam Map<String, String> params,
                                  RedirectAttributes redirectAttributes) {

        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Aluno aluno = alunoRepository.findByUserUsername(user.getUsername())
                .orElse(null);
        if (aluno == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Seu usuário não está vinculado a um registro de aluno.");
            return "redirect:/avaliacoes/" + id + "/responder";
        }
        try {
            validarAlunoPodeResponder(aluno, avaliacao);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/avaliacoes/disponiveis";
        }

        if (jaRespondeu(aluno, avaliacao)) {
            redirectAttributes.addFlashAttribute("error", "Você já respondeu esta avaliação.");
            return "redirect:/avaliacoes/" + id + "/responder";
        }

        // 0) Extrai respostas e importâncias da requisição
        Map<Long, String> responses = extractResponses(params);
        Map<Long, GrauImportancia> importanciasQuestao = extractImportanciasQuestao(params);

        // 0.1) Validação: para cada questão QUANTITATIVA respondida,
        // exige importância preenchida.
        // (E evita salvar meia coisa)
        for (Map.Entry<Long, String> e : responses.entrySet()) {
            Long qid = e.getKey();
            String value = e.getValue();
            if (value == null || value.isBlank()) continue;

            Question question = questionRepository.findById(qid)
                    .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada: " + qid));

            if (question.getType() == QuestionType.QUANTITATIVA) {
                GrauImportancia gi = importanciasQuestao.get(qid);
                if (gi == null) {
                    redirectAttributes.addFlashAttribute("error",
                            "Faltou escolher a IMPORTÂNCIA da questão: " + question.getText());
                    return "redirect:/avaliacoes/" + id + "/responder";
                }
            }
        }

        ParticipacaoAvaliacao participacao = participacaoAvaliacaoRepository
                .findByAlunoAndAvaliacaoAplicada(aluno, avaliacao)
                .orElseGet(() -> {
                    ParticipacaoAvaliacao nova = new ParticipacaoAvaliacao();
                    nova.setAluno(aluno);
                    nova.setAvaliacaoAplicada(avaliacao);
                    nova.setStatus(StatusResposta.PENDENTE);
                    return nova;
                });

        boolean anonima = avaliacao.isAnonima();

        // 1) Cria o envelope de respostas. Em avaliacao anonima, nao grava aluno.
        RespostaAluno respostaAluno = new RespostaAluno();
        respostaAluno.setAluno(anonima ? null : aluno);
        respostaAluno.setAvaliacaoAplicada(avaliacao);
        respostaAluno.setDataResposta(LocalDateTime.now());
        respostaAluno.setStatusResposta(StatusResposta.RESPONDIDO);
        respostaAluno.setAnonima(anonima);
        respostaAluno.setCodigoAnonimo(UUID.randomUUID().toString());

        RespostaAluno respostaAlunoSalvo = respostaAlunoRepository.save(respostaAluno);

        // 2) Salva respostas (Answer) + importância por questão (novo)
        for (Map.Entry<Long, String> entry : responses.entrySet()) {
            Long qid = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isBlank()) {
                continue;
            }

            Question question = questionRepository.findById(qid)
                    .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada: " + qid));

            Answer a = new Answer();
            a.setQuestion(question);
            a.setResponse(value);
            a.setRespostaAluno(respostaAlunoSalvo);
            a.setUserUsername(anonima ? null : user.getUsername());
            answerRepository.save(a);

            // NOVO: se for QUANTITATIVA, salvar importância por questão
            if (question.getType() == QuestionType.QUANTITATIVA) {
                GrauImportancia gi = importanciasQuestao.get(qid);
                ImportanciaQuestaoResposta imp = new ImportanciaQuestaoResposta();
                imp.setRespostaAluno(respostaAlunoSalvo);
                imp.setQuestion(question);
                imp.setGrauImportancia(gi);
                importanciaQuestaoRespostaRepository.save(imp);
            }
        }

        // 3) (Opcional / legado) Importância por ItemAvaliacao — mantém como você já tinha
        Map<ItemAvaliacao, GrauImportancia> importanciasItem = extractImportanciasItem(params);
        importanciasItem.forEach((item, grau) -> {
            ImportanciaItemResposta imp = new ImportanciaItemResposta();
            imp.setItem(item);
            imp.setGrauImportancia(grau);
            imp.setRespostaAluno(respostaAlunoSalvo);
            importanciaItemRespostaRepository.save(imp);
        });

        participacao.setStatus(StatusResposta.RESPONDIDO);
        participacao.setDataConclusao(LocalDateTime.now());
        participacaoAvaliacaoRepository.save(participacao);

        redirectAttributes.addFlashAttribute("success", "Respostas enviadas com sucesso!");
        return "redirect:/home";
    }

    // ============================================================
    // ADMIN VER RESPOSTAS DE UMA AVALIAÇÃO ESPECÍFICA
    // ============================================================
    @GetMapping("/{id}/respostas")
    public String verRespostasAvaliacao(@PathVariable Long id, Model model) {
        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        Questionnaire questionnaire = avaliacao.getQuestionario();
        var questions = questionRepository.findByQuestionnaireId(questionnaire.getId());

        var answers = answerRepository.findByRespostaAlunoAvaliacaoAplicadaId(id);

        Map<Long, List<Answer>> answersByQuestion = new HashMap<>();
        for (Question q : questions) {
            answersByQuestion.put(q.getId(), new ArrayList<>());
        }
        for (Answer a : answers) {
            Long qid = a.getQuestion().getId();
            answersByQuestion.computeIfAbsent(qid, k -> new ArrayList<>()).add(a);
        }

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("questions", questions);
        model.addAttribute("answersByQuestion", answersByQuestion);

        return "avaliacao/respostas";
    }

    // ============================================================
    // AUXILIARES
    // ============================================================
    private Map<Long, String> extractResponses(Map<String, String> params) {
        Map<Long, String> out = new HashMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("responses[")) {
                String idStr = k.substring("responses[".length(), k.length() - 1);
                try {
                    Long qId = Long.parseLong(idStr);
                    out.put(qId, v);
                } catch (NumberFormatException ignored) {
                }
            }
        });
        return out;
    }

    // NOVO: pega importanciaQuestao[123]=ALTA|MEDIA|BAIXA
    private Map<Long, GrauImportancia> extractImportanciasQuestao(Map<String, String> params) {
        Map<Long, GrauImportancia> out = new HashMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("importanciaQuestao[")) {
                String idStr = k.substring("importanciaQuestao[".length(), k.length() - 1);
                try {
                    Long qId = Long.parseLong(idStr);
                    out.put(qId, GrauImportancia.valueOf(v));
                } catch (Exception ignored) {
                }
            }
        });
        return out;
    }

    // mantém seu legado: importancia[ITEM]=ALTA
    private Map<ItemAvaliacao, GrauImportancia> extractImportanciasItem(Map<String, String> params) {
        Map<ItemAvaliacao, GrauImportancia> out = new HashMap<>();

        params.forEach((k, v) -> {
            if (k.startsWith("importancia[")) {
                String itemName = k.substring("importancia[".length(), k.length() - 1);
                try {
                    ItemAvaliacao item = ItemAvaliacao.valueOf(itemName);
                    GrauImportancia grau = GrauImportancia.valueOf(v);
                    out.put(item, grau);
                } catch (IllegalArgumentException ignored) {
                }
            }
        });

        return out;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return userRepository.findByUsername(auth.getName());
    }

    private List<Turma> getTurmasPermitidas() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return turmaRepository.findAll();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> turmaRepository.findByInstituicaoId(instituicao.getId()))
                .orElseGet(List::of);
    }

    private List<Questionnaire> getQuestionariosPermitidos() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return questionnaireRepository.findModelosEditaveisByStatus(StatusDisponibilidade.DISPONIVEL);
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> questionnaireRepository.findModelosEditaveisByStatusAndInstituicaoId(
                        StatusDisponibilidade.DISPONIVEL, instituicao.getId()))
                .orElseGet(List::of);
    }

    private void validarMesmoEscopo(Turma turma, Questionnaire questionnaire) {
        if (turma.getInstituicao() == null || questionnaire.getInstituicao() == null) {
            return;
        }
        if (!turma.getInstituicao().getId().equals(questionnaire.getInstituicao().getId())) {
            throw new IllegalArgumentException("Turma e questionario pertencem a instituicoes diferentes.");
        }
    }

    private Questionnaire obterQuestionarioPublicadoParaAtualizacao(AvaliacaoAplicada avaliacao,
                                                                    Questionnaire selecionado) {
        Questionnaire atual = avaliacao.getQuestionario();
        if (atual != null && atual.isBloqueado()) {
            boolean mesmoModeloBase = Objects.equals(atual.getQuestionarioBaseId(), selecionado.getId());
            boolean mesmaVersaoPublicada = Objects.equals(atual.getId(), selecionado.getId());
            if (mesmoModeloBase || mesmaVersaoPublicada) {
                return atual;
            }
        }
        return questionnaireVersioningService.criarVersaoPublicada(selecionado);
    }

    private void validarAlunoPodeResponder(Aluno aluno, AvaliacaoAplicada avaliacao) {
        if (avaliacao.getStatus() != StatusAvaliacao.ABERTA) {
            throw new IllegalStateException("Esta avaliacao nao esta aberta para respostas.");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (avaliacao.getDataInicio() != null && agora.isBefore(avaliacao.getDataInicio())) {
            throw new IllegalStateException("Esta avaliacao ainda nao iniciou.");
        }
        if (avaliacao.getDataFim() != null && agora.isAfter(avaliacao.getDataFim())) {
            throw new IllegalStateException("Esta avaliacao ja foi encerrada.");
        }
        if (aluno.getTurma() == null || avaliacao.getTurma() == null
                || !aluno.getTurma().getId().equals(avaliacao.getTurma().getId())) {
            throw new IllegalStateException("Esta avaliacao nao esta disponivel para a sua turma.");
        }
        if (aluno.getInstituicao() != null && avaliacao.getInstituicao() != null
                && !aluno.getInstituicao().getId().equals(avaliacao.getInstituicao().getId())) {
            throw new IllegalStateException("Esta avaliacao pertence a outra instituicao.");
        }
    }

    private boolean jaRespondeu(Aluno aluno, AvaliacaoAplicada avaliacao) {
        return participacaoAvaliacaoRepository.existsByAlunoAndAvaliacaoAplicadaAndStatus(
                aluno, avaliacao, StatusResposta.RESPONDIDO)
                || respostaAlunoRepository.existsByAlunoAndAvaliacaoAplicada(aluno, avaliacao);
    }
}
