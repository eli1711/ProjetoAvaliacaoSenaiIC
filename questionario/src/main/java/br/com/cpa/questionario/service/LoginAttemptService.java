package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final int maxAttempts;
    private final int lockMinutes;

    public LoginAttemptService(UserRepository userRepository,
                               AuditService auditService,
                               @Value("${app.security.login.max-failed-attempts:5}") int maxAttempts,
                               @Value("${app.security.login.lock-minutes:15}") int lockMinutes) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.maxAttempts = maxAttempts;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public void registrarSucesso(String username) {
        User user = findUser(username);
        if (user == null) {
            return;
        }
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.resetLoginFailures();
            userRepository.save(user);
        }
    }

    @Transactional
    public void registrarFalha(String username) {
        User user = findUser(username);
        if (user == null || user.isTemporarilyLocked()) {
            return;
        }

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            auditService.registrarParaUsuario(
                    user.getUsername(),
                    "LOGIN_BLOQUEADO_TEMPORARIAMENTE",
                    "User",
                    user.getUsername(),
                    user.getInstituicao(),
                    "Conta bloqueada temporariamente apos tentativas invalidas.");
        }

        userRepository.save(user);
    }

    private User findUser(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(username);
    }
}
