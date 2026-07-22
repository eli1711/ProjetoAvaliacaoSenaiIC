package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InstituicaoScopeService {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final UserRepository userRepository;

    public InstituicaoScopeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findByUsername(auth.getName()));
    }

    public Optional<Instituicao> getInstituicaoAtual() {
        return getUsuarioAtual().map(User::getInstituicao);
    }

    public boolean isSuperAdmin() {
        return getUsuarioAtual()
                .map(User::getRole)
                .map(ROLE_SUPER_ADMIN::equals)
                .orElse(false);
    }

    public boolean podeAcessar(Instituicao instituicao) {
        if (isSuperAdmin()) {
            return true;
        }
        Optional<Instituicao> atual = getInstituicaoAtual();
        if (atual.isEmpty()) {
            return true;
        }
        return mesmaInstituicao(atual.get(), instituicao);
    }

    public void validarAcesso(Instituicao instituicao) {
        if (!podeAcessar(instituicao)) {
            throw new AccessDeniedException("Acesso negado para dados de outra instituicao.");
        }
    }

    public Instituicao instituicaoParaNovoRegistro(Instituicao preferida) {
        if (isSuperAdmin()) {
            return preferida;
        }
        return getInstituicaoAtual().orElse(preferida);
    }

    private boolean mesmaInstituicao(Instituicao a, Instituicao b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null) {
            return a == b;
        }
        return a.getId().equals(b.getId());
    }
}
