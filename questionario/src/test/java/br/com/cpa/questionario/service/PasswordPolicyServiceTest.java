package br.com.cpa.questionario.service;

import br.com.cpa.questionario.exception.PasswordPolicyException;
import br.com.cpa.questionario.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService service = new PasswordPolicyService(10, 128, 3);

    @Test
    void rejeitaSenhaFraca() {
        User user = usuarioBase();

        assertThatThrownBy(() -> service.validar("1234567890", user))
                .isInstanceOf(PasswordPolicyException.class)
                .hasMessageContaining("combinar");
    }

    @Test
    void aceitaSenhaComPoliticaConfigurada() {
        User user = usuarioBase();

        assertThatCode(() -> service.validar("Senha@2026", user))
                .doesNotThrowAnyException();
    }

    @Test
    void geraSenhaTemporariaParaAlunoImportado() {
        assertThat(service.gerarSenhaInicialAluno("123456789", "123.456.789-00"))
                .isEqualTo("12345678900");
    }

    private User usuarioBase() {
        User user = new User();
        user.setUsername("aluno001");
        user.setName("Maria Silva");
        user.setEmail("maria.silva@instituicao.example");
        user.setRa("RA001");
        return user;
    }
}
