package br.com.cpa.questionario.config;

import br.com.cpa.questionario.service.AuditService;
import br.com.cpa.questionario.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAuditListener {

    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationAuditListener(AuditService auditService,
                                       LoginAttemptService loginAttemptService) {
        this.auditService = auditService;
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() != null) {
            loginAttemptService.registrarSucesso(event.getAuthentication().getName());
            auditService.registrarParaUsuario(
                    event.getAuthentication().getName(),
                    "LOGIN_SUCESSO",
                    "User",
                    event.getAuthentication().getName(),
                    null,
                    "Login realizado com sucesso.");
        }
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        if (event.getAuthentication() != null) {
            loginAttemptService.registrarFalha(event.getAuthentication().getName());
            auditService.registrarParaUsuario(
                    event.getAuthentication().getName(),
                    "LOGIN_FALHA",
                    "User",
                    event.getAuthentication().getName(),
                    null,
                    "Falha de autenticacao sem exposicao de credenciais.");
        }
    }
}
