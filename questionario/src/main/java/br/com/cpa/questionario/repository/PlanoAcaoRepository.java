package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.PlanoAcao;
import br.com.cpa.questionario.model.StatusPlanoAcao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanoAcaoRepository extends JpaRepository<PlanoAcao, Long> {

    List<PlanoAcao> findByInstituicaoIdOrderByPrazoAsc(Long instituicaoId);

    List<PlanoAcao> findByStatusAndInstituicaoIdOrderByPrazoAsc(StatusPlanoAcao status, Long instituicaoId);

    List<PlanoAcao> findByAvaliacaoAplicadaIdAndInstituicaoIdOrderByPrazoAsc(Long avaliacaoId, Long instituicaoId);

    List<PlanoAcao> findByStatusOrderByPrazoAsc(StatusPlanoAcao status);

    List<PlanoAcao> findByAvaliacaoAplicadaIdOrderByPrazoAsc(Long avaliacaoId);
}
