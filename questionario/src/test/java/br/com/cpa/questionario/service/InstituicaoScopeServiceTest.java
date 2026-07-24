package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstituicaoScopeServiceTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void superAdminPodeAcessarQualquerInstituicao() {
        InstituicaoScopeService service = new InstituicaoScopeService(userRepository);
        User dev = usuario("dev", "ROLE_SUPER_ADMIN", null);
        Instituicao instituicao = instituicao(1L);

        autenticar("dev");
        when(userRepository.findByUsername("dev")).thenReturn(dev);

        assertThat(service.isSuperAdmin()).isTrue();
        assertThat(service.getInstituicaoAtual()).isEmpty();
        assertThat(service.podeAcessar(instituicao)).isTrue();
        assertThat(service.instituicaoParaNovoRegistro(null)).isNull();
    }

    @Test
    void usuarioSemInstituicaoNaoRecebeAcessoGlobal() {
        InstituicaoScopeService service = new InstituicaoScopeService(userRepository);
        User admin = usuario("admin", "ROLE_ADMIN", null);
        Instituicao instituicao = instituicao(1L);

        autenticar("admin");
        when(userRepository.findByUsername("admin")).thenReturn(admin);

        assertThat(service.isSuperAdmin()).isFalse();
        assertThat(service.podeAcessar(instituicao)).isFalse();
        assertThatThrownBy(() -> service.validarAcesso(instituicao))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.instituicaoParaNovoRegistro(instituicao))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void usuarioInstitucionalAcessaSomenteSuaInstituicao() {
        InstituicaoScopeService service = new InstituicaoScopeService(userRepository);
        Instituicao propria = instituicao(1L);
        Instituicao outra = instituicao(2L);
        User admin = usuario("admin", "ROLE_ADMIN", propria);

        autenticar("admin");
        when(userRepository.findByUsername("admin")).thenReturn(admin);

        assertThat(service.podeAcessar(propria)).isTrue();
        assertThat(service.podeAcessar(outra)).isFalse();
        assertThat(service.instituicaoParaNovoRegistro(outra)).isSameAs(propria);
    }

    private void autenticar(String username) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, "senha", List.of()));
    }

    private User usuario(String username, String role, Instituicao instituicao) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@teste.local");
        user.setName(username);
        user.setRole(role);
        user.setInstituicao(instituicao);
        return user;
    }

    private Instituicao instituicao(Long id) {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(id);
        instituicao.setNome("Instituicao " + id);
        instituicao.setIdentificadorInstitucional("INST-" + id);
        return instituicao;
    }
}
