package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.Answer;
import br.com.cpa.questionario.model.AvaliacaoAplicada;
import br.com.cpa.questionario.model.RespostaAluno;
import br.com.cpa.questionario.repository.AvaliacaoAplicadaRepository;
import br.com.cpa.questionario.repository.RespostaAlunoRepository;
import br.com.cpa.questionario.service.AuditService;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import br.com.cpa.questionario.service.ResultadoPrivacidadeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoCsvController {

    private final RespostaAlunoRepository respostaAlunoRepo;
    private final AvaliacaoAplicadaRepository avaliacaoAplicadaRepository;
    private final InstituicaoScopeService instituicaoScopeService;
    private final ResultadoPrivacidadeService resultadoPrivacidadeService;
    private final AuditService auditService;

    public AvaliacaoCsvController(RespostaAlunoRepository respostaAlunoRepo,
                                  AvaliacaoAplicadaRepository avaliacaoAplicadaRepository,
                                  InstituicaoScopeService instituicaoScopeService,
                                  ResultadoPrivacidadeService resultadoPrivacidadeService,
                                  AuditService auditService) {
        this.respostaAlunoRepo = respostaAlunoRepo;
        this.avaliacaoAplicadaRepository = avaliacaoAplicadaRepository;
        this.instituicaoScopeService = instituicaoScopeService;
        this.resultadoPrivacidadeService = resultadoPrivacidadeService;
        this.auditService = auditService;
    }

    @GetMapping("/{avaliacaoId}/csv-template")
    public ResponseEntity<byte[]> baixarCsvTemplate(@PathVariable Long avaliacaoId) {
        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(avaliacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());

        List<RespostaAluno> respostas = respostaAlunoRepo.findByAvaliacaoAplicadaId(avaliacaoId);
        resultadoPrivacidadeService.validarExportacaoPermitida(respostas.size());

        // Ordena pra ficar bem legível
        respostas.sort(Comparator.comparing(ra -> {
            if (ra.isAnonima() || ra.getAluno() == null || ra.getAluno().getNome() == null) {
                return "ANONIMO";
            }
            return ra.getAluno().getNome();
        }, String.CASE_INSENSITIVE_ORDER));

        StringBuilder csv = new StringBuilder();

        // Cabeçalho novo:
        csv.append("aluno_nome;questao_texto;resposta\n");

        for (RespostaAluno ra : respostas) {
            String alunoNome = ra.isAnonima() ? "ANONIMO" : safe(ra.getAluno() != null ? ra.getAluno().getNome() : null);

            List<Answer> ansList = ra.getRespostas();
            if (ansList == null || ansList.isEmpty()) {
                // se você quiser listar mesmo sem resposta, descomente:
                // csv.append(alunoNome).append(";;\n");
                continue;
            }

            // ordena por id da questão (opcional)
            ansList.sort(Comparator.comparing(a -> a.getQuestion() != null ? a.getQuestion().getId() : 0L));

            for (Answer ans : ansList) {
                String questaoTexto = safe(ans.getQuestion() != null ? ans.getQuestion().getText() : null);
                String resposta = safe(ans.getResponse());

                csv.append(alunoNome).append(";")
                   .append(questaoTexto).append(";")
                   .append(resposta).append("\n");
            }
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        auditService.registrar(
                "EXPORTACAO_CSV_AVALIACAO",
                "AvaliacaoAplicada",
                avaliacao.getId(),
                avaliacao.getInstituicao(),
                "totalEnvios=" + respostas.size());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"avaliacao-" + avaliacaoId + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private static String safe(String s) {
        if (s == null) return "";
        // CSV com ;: precisamos evitar quebrar linha e ; no conteúdo
        return s.replace("\n", " ")
                .replace("\r", " ")
                .replace(";", ",")
                .trim();
    }
}
