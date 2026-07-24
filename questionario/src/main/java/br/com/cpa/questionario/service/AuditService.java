package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.AuditLog;
import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuditService(AuditLogRepository auditLogRepository,
                        ObjectProvider<HttpServletRequest> requestProvider) {
        this.auditLogRepository = auditLogRepository;
        this.requestProvider = requestProvider;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String operacao,
                          String entidade,
                          Object entidadeId,
                          Instituicao instituicao,
                          String detalhes) {
        registrarParaUsuario(usuarioAtual(), operacao, entidade, entidadeId, instituicao, detalhes);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarParaUsuario(String usuario,
                                     String operacao,
                                     String entidade,
                                     Object entidadeId,
                                     Instituicao instituicao,
                                     String detalhes) {
        AuditLog log = new AuditLog();
        log.setUsuario(usuario);
        log.setOperacao(operacao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId != null ? String.valueOf(entidadeId) : null);
        log.setInstituicao(instituicao);
        log.setDetalhes(mascararSegredos(detalhes));
        log.setIp(ipAtual());
        auditLogRepository.save(log);
    }

    private String usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    private String ipAtual() {
        HttpServletRequest request;
        try {
            request = requestProvider.getIfAvailable();
        } catch (RuntimeException ex) {
            return null;
        }
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String mascararSegredos(String detalhes) {
        if (detalhes == null) {
            return null;
        }
        return detalhes
                .replaceAll("(?i)(senha|password|token|secret)=([^;\\s]+)", "$1=***")
                .replaceAll("(?i)(senha|password|token|secret):([^;\\s]+)", "$1:***");
    }
}
