package br.com.cpa.questionario.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PlanoAcaoTest {

    @Test
    void limitaPercentualConclusaoEntreZeroECem() {
        PlanoAcao plano = new PlanoAcao();

        plano.setPercentualConclusao(-10);
        assertThat(plano.getPercentualConclusao()).isZero();

        plano.setPercentualConclusao(120);
        assertThat(plano.getPercentualConclusao()).isEqualTo(100);
    }

    @Test
    void identificaPlanoAtrasadoQuandoPrazoVenceuENaoEstaFinalizado() {
        PlanoAcao plano = new PlanoAcao();
        plano.setPrazo(LocalDate.now().minusDays(1));
        plano.setStatus(StatusPlanoAcao.EM_ANDAMENTO);

        assertThat(plano.isAtrasada()).isTrue();
        assertThat(plano.getStatusEfetivo()).isEqualTo(StatusPlanoAcao.ATRASADA);

        plano.setStatus(StatusPlanoAcao.CONCLUIDA);
        assertThat(plano.isAtrasada()).isFalse();
    }
}
