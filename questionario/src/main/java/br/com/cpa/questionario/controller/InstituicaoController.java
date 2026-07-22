package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.repository.InstituicaoRepository;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/instituicoes")
public class InstituicaoController {

    private final InstituicaoRepository instituicaoRepository;
    private final InstituicaoScopeService instituicaoScopeService;

    public InstituicaoController(InstituicaoRepository instituicaoRepository,
                                 InstituicaoScopeService instituicaoScopeService) {
        this.instituicaoRepository = instituicaoRepository;
        this.instituicaoScopeService = instituicaoScopeService;
    }

    @GetMapping
    public String list(Model model) {
        List<Instituicao> instituicoes = instituicaoScopeService.getInstituicaoAtual()
                .map(List::of)
                .orElseGet(instituicaoRepository::findAll);
        model.addAttribute("instituicoes", instituicoes);
        return "instituicao/list";
    }

    @GetMapping("/new")
    public String newInstituicao(Model model) {
        model.addAttribute("instituicao", new Instituicao());
        return "instituicao/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada"));
        instituicaoScopeService.validarAcesso(instituicao);
        model.addAttribute("instituicao", instituicao);
        return "instituicao/form";
    }

    @PostMapping
    public String save(@ModelAttribute Instituicao instituicao,
                       RedirectAttributes redirectAttributes) {
        if (instituicao.getId() != null) {
            Instituicao existente = instituicaoRepository.findById(instituicao.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada"));
            instituicaoScopeService.validarAcesso(existente);
        }

        Instituicao atual = instituicaoScopeService.getInstituicaoAtual().orElse(null);
        if (atual != null && !instituicaoScopeService.isSuperAdmin()) {
            instituicao.setId(atual.getId());
            instituicao.setIdentificadorInstitucional(atual.getIdentificadorInstitucional());
        }

        instituicaoRepository.save(instituicao);
        redirectAttributes.addFlashAttribute("success", "Instituicao salva com sucesso.");
        return "redirect:/instituicoes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada"));
        instituicaoScopeService.validarAcesso(instituicao);

        try {
            instituicaoRepository.delete(instituicao);
            redirectAttributes.addFlashAttribute("success", "Instituicao removida com sucesso.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "Nao foi possivel remover a instituicao porque existem dados vinculados.");
        }

        return "redirect:/instituicoes";
    }
}
