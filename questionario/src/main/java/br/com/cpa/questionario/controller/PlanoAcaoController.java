package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.AvaliacaoAplicada;
import br.com.cpa.questionario.model.PlanoAcao;
import br.com.cpa.questionario.model.PrioridadePlanoAcao;
import br.com.cpa.questionario.model.StatusPlanoAcao;
import br.com.cpa.questionario.repository.AvaliacaoAplicadaRepository;
import br.com.cpa.questionario.repository.PlanoAcaoRepository;
import br.com.cpa.questionario.service.AuditService;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/planos-acao")
public class PlanoAcaoController {

    private final PlanoAcaoRepository planoAcaoRepository;
    private final AvaliacaoAplicadaRepository avaliacaoAplicadaRepository;
    private final InstituicaoScopeService instituicaoScopeService;
    private final AuditService auditService;

    public PlanoAcaoController(PlanoAcaoRepository planoAcaoRepository,
                               AvaliacaoAplicadaRepository avaliacaoAplicadaRepository,
                               InstituicaoScopeService instituicaoScopeService,
                               AuditService auditService) {
        this.planoAcaoRepository = planoAcaoRepository;
        this.avaliacaoAplicadaRepository = avaliacaoAplicadaRepository;
        this.instituicaoScopeService = instituicaoScopeService;
        this.auditService = auditService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusPlanoAcao status,
                         @RequestParam(required = false) Long avaliacaoId,
                         Model model) {
        List<PlanoAcao> planos = buscarPlanos(status, avaliacaoId);
        long atrasadas = planos.stream().filter(PlanoAcao::isAtrasada).count();

        model.addAttribute("planos", planos);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("avaliacaoSelecionadaId", avaliacaoId);
        model.addAttribute("statusPlanos", StatusPlanoAcao.values());
        model.addAttribute("avaliacoes", getAvaliacoesPermitidas());
        model.addAttribute("totalPlanos", planos.size());
        model.addAttribute("totalAtrasadas", atrasadas);
        return "plano-acao/list";
    }

    @GetMapping("/new")
    public String novo(@RequestParam(required = false) Long avaliacaoId, Model model) {
        PlanoAcao plano = new PlanoAcao();
        if (avaliacaoId != null) {
            AvaliacaoAplicada avaliacao = getAvaliacaoPermitida(avaliacaoId);
            plano.setAvaliacaoAplicada(avaliacao);
            plano.setInstituicao(avaliacao.getInstituicao());
        }
        prepararFormulario(model, plano);
        return "plano-acao/form";
    }

    @PostMapping
    @Transactional
    public String criar(@ModelAttribute PlanoAcao plano,
                        @RequestParam(required = false) Long avaliacaoId,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {
        aplicarEscopo(plano, avaliacaoId);
        plano.setCriadoPor(principal != null ? principal.getName() : null);
        plano.setAtualizadoPor(plano.getCriadoPor());
        plano.normalizar();
        plano = planoAcaoRepository.save(plano);
        auditService.registrar(
                "PLANO_ACAO_CRIADO",
                "PlanoAcao",
                plano.getId(),
                plano.getInstituicao(),
                "prioridade=" + plano.getPrioridade() + "; prazo=" + plano.getPrazo());
        redirectAttributes.addFlashAttribute("success", "Plano de acao criado com sucesso.");
        return "redirect:/planos-acao";
    }

    @GetMapping("/{id}/edit")
    public String editar(@PathVariable Long id, Model model) {
        prepararFormulario(model, getPlanoPermitido(id));
        return "plano-acao/form";
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public String atualizar(@PathVariable Long id,
                            @ModelAttribute PlanoAcao dados,
                            @RequestParam(required = false) Long avaliacaoId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        PlanoAcao plano = getPlanoPermitido(id);
        copiarDadosEditaveis(dados, plano);
        aplicarEscopo(plano, avaliacaoId);
        plano.setAtualizadoPor(principal != null ? principal.getName() : null);
        plano.normalizar();
        planoAcaoRepository.save(plano);
        auditService.registrar(
                "PLANO_ACAO_ATUALIZADO",
                "PlanoAcao",
                plano.getId(),
                plano.getInstituicao(),
                "status=" + plano.getStatus() + "; percentual=" + plano.getPercentualConclusao());
        redirectAttributes.addFlashAttribute("success", "Plano de acao atualizado com sucesso.");
        return "redirect:/planos-acao";
    }

    @PostMapping("/{id}/cancelar")
    @Transactional
    public String cancelar(@PathVariable Long id,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        PlanoAcao plano = getPlanoPermitido(id);
        plano.setStatus(StatusPlanoAcao.CANCELADA);
        plano.setAtualizadoPor(principal != null ? principal.getName() : null);
        planoAcaoRepository.save(plano);
        auditService.registrar(
                "PLANO_ACAO_CANCELADO",
                "PlanoAcao",
                plano.getId(),
                plano.getInstituicao(),
                "Cancelamento logico do plano de acao.");
        redirectAttributes.addFlashAttribute("success", "Plano de acao cancelado.");
        return "redirect:/planos-acao";
    }

    private List<PlanoAcao> buscarPlanos(StatusPlanoAcao status, Long avaliacaoId) {
        if (avaliacaoId != null) {
            AvaliacaoAplicada avaliacao = getAvaliacaoPermitida(avaliacaoId);
            Long instituicaoId = avaliacao.getInstituicao() != null ? avaliacao.getInstituicao().getId() : null;
            if (instituicaoId != null) {
                return planoAcaoRepository
                        .findByAvaliacaoAplicadaIdAndInstituicaoIdOrderByPrazoAsc(avaliacaoId, instituicaoId);
            }
            return planoAcaoRepository.findByAvaliacaoAplicadaIdOrderByPrazoAsc(avaliacaoId);
        }

        if (status != null) {
            if (instituicaoScopeService.isSuperAdmin()) {
                return planoAcaoRepository.findByStatusOrderByPrazoAsc(status);
            }
            return instituicaoScopeService.getInstituicaoAtual()
                    .map(instituicao -> planoAcaoRepository
                            .findByStatusAndInstituicaoIdOrderByPrazoAsc(status, instituicao.getId()))
                    .orElseGet(List::of);
        }

        if (instituicaoScopeService.isSuperAdmin()) {
            return planoAcaoRepository.findAll().stream()
                        .sorted(Comparator.comparing(PlanoAcao::getPrazo,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> planoAcaoRepository.findByInstituicaoIdOrderByPrazoAsc(instituicao.getId()))
                .orElseGet(List::of);
    }

    private void prepararFormulario(Model model, PlanoAcao plano) {
        model.addAttribute("plano", plano);
        model.addAttribute("avaliacoes", getAvaliacoesPermitidas());
        model.addAttribute("prioridades", PrioridadePlanoAcao.values());
        model.addAttribute("statusPlanos", StatusPlanoAcao.values());
    }

    private List<AvaliacaoAplicada> getAvaliacoesPermitidas() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return avaliacaoAplicadaRepository.findAll();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> avaliacaoAplicadaRepository.findByInstituicaoId(instituicao.getId()))
                .orElseGet(List::of);
    }

    private PlanoAcao getPlanoPermitido(Long id) {
        PlanoAcao plano = planoAcaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano de acao nao encontrado"));
        instituicaoScopeService.validarAcesso(plano.getInstituicao());
        return plano;
    }

    private AvaliacaoAplicada getAvaliacaoPermitida(Long id) {
        AvaliacaoAplicada avaliacao = avaliacaoAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));
        instituicaoScopeService.validarAcesso(avaliacao.getInstituicao());
        return avaliacao;
    }

    private void aplicarEscopo(PlanoAcao plano, Long avaliacaoId) {
        if (avaliacaoId != null) {
            AvaliacaoAplicada avaliacao = getAvaliacaoPermitida(avaliacaoId);
            plano.setAvaliacaoAplicada(avaliacao);
            plano.setInstituicao(instituicaoScopeService.instituicaoParaNovoRegistro(avaliacao.getInstituicao()));
            return;
        }
        plano.setAvaliacaoAplicada(null);
        plano.setInstituicao(instituicaoScopeService.instituicaoParaNovoRegistro(plano.getInstituicao()));
        if (plano.getInstituicao() == null) {
            throw new IllegalArgumentException("Plano de acao deve estar vinculado a uma instituicao ou avaliacao.");
        }
    }

    private void copiarDadosEditaveis(PlanoAcao origem, PlanoAcao destino) {
        destino.setProblemaIdentificado(origem.getProblemaIdentificado());
        destino.setIndicadorRelacionado(origem.getIndicadorRelacionado());
        destino.setAcaoProposta(origem.getAcaoProposta());
        destino.setResponsavel(origem.getResponsavel());
        destino.setSetorResponsavel(origem.getSetorResponsavel());
        destino.setDataInicio(origem.getDataInicio());
        destino.setPrazo(origem.getPrazo());
        destino.setPrioridade(origem.getPrioridade());
        destino.setStatus(origem.getStatus());
        destino.setPercentualConclusao(origem.getPercentualConclusao());
        destino.setEvidencias(origem.getEvidencias());
        destino.setObservacoes(origem.getObservacoes());
        destino.setResultadoEsperado(origem.getResultadoEsperado());
        destino.setResultadoAlcancado(origem.getResultadoAlcancado());
    }
}
