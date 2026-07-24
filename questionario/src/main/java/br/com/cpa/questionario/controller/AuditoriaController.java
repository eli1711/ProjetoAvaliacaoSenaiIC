package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.repository.AuditLogRepository;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditLogRepository auditLogRepository;
    private final InstituicaoScopeService instituicaoScopeService;

    public AuditoriaController(AuditLogRepository auditLogRepository,
                               InstituicaoScopeService instituicaoScopeService) {
        this.auditLogRepository = auditLogRepository;
        this.instituicaoScopeService = instituicaoScopeService;
    }

    @GetMapping
    public String listar(Model model) {
        var logs = instituicaoScopeService.isSuperAdmin()
                ? auditLogRepository.findTop200ByOrderByDataHoraDesc()
                : instituicaoScopeService.getInstituicaoAtual()
                        .map(instituicao -> auditLogRepository.findTop200ByInstituicaoIdOrderByDataHoraDesc(instituicao.getId()))
                        .orElseGet(List::of);
        model.addAttribute("logs", logs);
        return "auditoria/list";
    }
}
