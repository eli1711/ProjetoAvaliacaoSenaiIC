package br.com.cpa.questionario.service;

import br.com.cpa.questionario.exception.ResultadoRestritoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultadoPrivacidadeServiceTest {

    @Test
    void bloqueiaResultadosQuandoGrupoEstaAbaixoDoMinimo() {
        ResultadoPrivacidadeService service = new ResultadoPrivacidadeService(
                5, false, 1.5, 2.5, 3.0, 3.5);

        assertThat(service.podeExibirResultadosAgregados(4)).isFalse();
        assertThat(service.podeExibirResultadosAgregados(5)).isTrue();
    }

    @Test
    void classificaMediaComCriteriosConfigurados() {
        ResultadoPrivacidadeService service = new ResultadoPrivacidadeService(
                5, false, 1.5, 2.5, 3.0, 3.5);

        assertThat(service.classificarMedia(null)).isEqualTo("Sem dados");
        assertThat(service.classificarMedia(1.4)).isEqualTo("Muito insatisfeito");
        assertThat(service.classificarMedia(2.0)).isEqualTo("Insatisfeito");
        assertThat(service.classificarMedia(3.0)).isEqualTo("Neutro");
        assertThat(service.classificarMedia(3.3)).isEqualTo("Satisfeito");
        assertThat(service.classificarMedia(3.8)).isEqualTo("Muito satisfeito");
    }

    @Test
    void bloqueiaExportacaoQuandoGrupoEstaAbaixoDoMinimo() {
        ResultadoPrivacidadeService service = new ResultadoPrivacidadeService(
                5, false, 1.5, 2.5, 3.0, 3.5);

        assertThatThrownBy(() -> service.validarExportacaoPermitida(4))
                .isInstanceOf(ResultadoRestritoException.class)
                .hasMessageContaining("abaixo do minimo");
    }
}
